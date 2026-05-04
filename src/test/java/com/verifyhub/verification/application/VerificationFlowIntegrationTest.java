package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.verifyhub.verification.adapter.out.provider.ProviderClientResilienceDecorator;
import com.verifyhub.verification.domain.AuthEntryType;
import com.verifyhub.verification.domain.ProviderAuthEntry;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderRequestResultType;
import com.verifyhub.verification.domain.ProviderType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class VerificationFlowIntegrationTest {

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

    @MockBean
    private ProviderClientResilienceDecorator providerClientResilienceDecorator;

    @Test
    void createsVerificationAndReadsStatusAndHistoriesWithoutRepeatingProviderCallOnReplay() throws Exception {
        when(providerClientResilienceDecorator.requestVerification(any(), any()))
                .thenAnswer(this::acceptedProviderResult);

        MvcResult createResult = mockMvc.perform(post("/api/v1/verifications")
                        .header("Idempotency-Key", "idem-flow-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "req-flow-1",
                                  "purpose": "LOGIN",
                                  "returnUrl": "https://client.example/return",
                                  "closeUrl": "https://client.example/close",
                                  "svcTypes": ["M"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.authEntry.type", is("REDIRECT_URL")))
                .andReturn();
        String verificationId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.verificationId");
        String provider = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.provider");

        assertThat(verificationStatus(verificationId)).isEqualTo("IN_PROGRESS");
        assertThat(providerCallHistoryCount(verificationId)).isOne();
        assertThat(historyEventCount(verificationId, "ROUTE_SELECTED")).isOne();
        assertThat(historyEventCount(verificationId, "PROVIDER_CALL_STARTED")).isOne();

        mockMvc.perform(get("/api/v1/verifications/{verificationId}", verificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationId", is(verificationId)))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.provider", is(provider)))
                .andExpect(jsonPath("$.data.purpose", is("LOGIN")));

        mockMvc.perform(get("/api/v1/verifications/{verificationId}/histories", verificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationId", is(verificationId)))
                .andExpect(jsonPath("$.data.histories[0].eventType", is("ROUTE_SELECTED")))
                .andExpect(jsonPath("$.data.histories[1].eventType", is("PROVIDER_CALL_STARTED")));

        mockMvc.perform(post("/api/v1/verifications")
                        .header("Idempotency-Key", "idem-flow-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "req-flow-1",
                                  "purpose": "LOGIN",
                                  "returnUrl": "https://client.example/return",
                                  "closeUrl": "https://client.example/close",
                                  "svcTypes": ["M"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationId", is(verificationId)))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.provider", is(provider)));

        assertThat(providerCallHistoryCount(verificationId)).isOne();
    }

    private ProviderRequestResult acceptedProviderResult(InvocationOnMock invocation) {
        ProviderRequest request = invocation.getArgument(1);
        ProviderType provider = invocation.getArgument(0, com.verifyhub.verification.port.out.ProviderClientPort.class).providerType();
        return new ProviderRequestResult(
                provider,
                provider.name().toLowerCase() + "-tx-" + request.providerRequestNo(),
                request.providerRequestNo(),
                new ProviderAuthEntry(
                        provider,
                        AuthEntryType.REDIRECT_URL,
                        "https://provider.example/auth/" + request.providerRequestNo(),
                        "GET",
                        "UTF-8",
                        Map.of()
                ),
                ProviderRequestResultType.ACCEPTED,
                "{\"resultType\":\"ACCEPTED\"}",
                200,
                100L,
                null
        );
    }

    private String verificationStatus(String verificationId) {
        return jdbcTemplate.queryForObject(
                "select status from verification_request where verification_id = ?",
                String.class,
                verificationId
        );
    }

    private int providerCallHistoryCount(String verificationId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from provider_call_history where verification_id = ?",
                Integer.class,
                verificationId
        );
        return count == null ? 0 : count;
    }

    private int historyEventCount(String verificationId, String eventType) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from verification_history where verification_id = ? and event_type = ?",
                Integer.class,
                verificationId,
                eventType
        );
        return count == null ? 0 : count;
    }
}
