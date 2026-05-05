package com.verifyhub.routing.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = "delete from provider_routing_policy where version > 1", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(statements = "delete from provider_routing_policy where version > 1", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdminRoutingPolicyIntegrationTest {

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
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void getsLatestRoutingPolicies() throws Exception {
        mockMvc.perform(get("/admin/v1/routing-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version", is(1)))
                .andExpect(jsonPath("$.data.policies[*].provider", containsInAnyOrder("KG", "NICE")))
                .andExpect(jsonPath("$.data.policies[?(@.provider == 'KG')].weight", containsInAnyOrder(10)))
                .andExpect(jsonPath("$.data.policies[?(@.provider == 'NICE')].weight", containsInAnyOrder(90)))
                .andExpect(jsonPath("$.data.policies[*].enabled", containsInAnyOrder(true, true)));
    }

    @Test
    void updatesRoutingPoliciesByInsertingNextVersion() throws Exception {
        mockMvc.perform(put("/admin/v1/routing-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "policies": [
                                    { "provider": "KG", "weight": 0, "enabled": false },
                                    { "provider": "NICE", "weight": 100, "enabled": true }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version", is(2)))
                .andExpect(jsonPath("$.data.policies[?(@.provider == 'KG')].enabled", containsInAnyOrder(false)))
                .andExpect(jsonPath("$.data.policies[?(@.provider == 'NICE')].weight", containsInAnyOrder(100)));

        assertThat(policyCountByVersion(1L)).isEqualTo(2);
        assertThat(policyCountByVersion(2L)).isEqualTo(2);
        assertThat(enabledPolicyCountByVersion(2L)).isOne();

        mockMvc.perform(get("/admin/v1/routing-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version", is(2)))
                .andExpect(jsonPath("$.data.policies[?(@.provider == 'KG')].enabled", containsInAnyOrder(false)))
                .andExpect(jsonPath("$.data.policies[?(@.provider == 'NICE')].enabled", containsInAnyOrder(true)));
    }

    @Test
    void rejectsIncompleteProviderPolicySet() throws Exception {
        mockMvc.perform(put("/admin/v1/routing-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "policies": [
                                    { "provider": "NICE", "weight": 100, "enabled": true }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST")));
    }

    private int policyCountByVersion(long version) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from provider_routing_policy where version = ?",
                Integer.class,
                version
        );
        return count == null ? 0 : count;
    }

    private int enabledPolicyCountByVersion(long version) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from provider_routing_policy where version = ? and enabled = true",
                Integer.class,
                version
        );
        return count == null ? 0 : count;
    }
}
