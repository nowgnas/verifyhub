package com.verifyhub.routing.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import com.verifyhub.verification.domain.ProviderType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
}
