package com.verifyhub.routing.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import com.verifyhub.verification.domain.ProviderType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class ProviderRoutingPolicyPersistenceAdapterIT {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("verifyhub")
            .withUsername("verifyhub")
            .withPassword("verifyhub");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private ProviderRoutingPolicyPersistenceAdapter providerRoutingPolicyPersistenceAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findsLatestEnabledPolicies() {
        List<ProviderRoutingPolicy> policies = providerRoutingPolicyPersistenceAdapter.findLatestEnabledPolicies();

        assertThat(policies).hasSize(2);
        assertThat(policies).extracting(ProviderRoutingPolicy::provider)
                .containsExactlyInAnyOrder(ProviderType.KG, ProviderType.NICE);
        assertThat(policies).extracting(ProviderRoutingPolicy::weight)
                .containsExactlyInAnyOrder(10, 90);
        assertThat(policies).allMatch(ProviderRoutingPolicy::enabled);
        assertThat(policies).extracting(ProviderRoutingPolicy::version).containsOnly(1L);
    }

    @Test
    @Sql(statements = "delete from provider_routing_policy where version = 2", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "delete from provider_routing_policy where version = 2", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void returnsEmptyWhenLatestPolicyVersionDisablesAllProviders() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 26, 14, 30);
        insertProviderRoutingPolicy(ProviderType.KG, 10, false, 2L, now);
        insertProviderRoutingPolicy(ProviderType.NICE, 90, false, 2L, now);

        List<ProviderRoutingPolicy> policies = providerRoutingPolicyPersistenceAdapter.findLatestEnabledPolicies();

        assertThat(policies).isEmpty();
    }

    private void insertProviderRoutingPolicy(
            ProviderType provider,
            int weight,
            boolean enabled,
            long version,
            LocalDateTime now
    ) {
        jdbcTemplate.update("""
                        insert into provider_routing_policy (
                            provider,
                            weight,
                            enabled,
                            version,
                            created_at,
                            updated_at
                        ) values (?, ?, ?, ?, ?, ?)
                        """,
                provider.name(),
                weight,
                enabled,
                version,
                now,
                now
        );
    }
}
