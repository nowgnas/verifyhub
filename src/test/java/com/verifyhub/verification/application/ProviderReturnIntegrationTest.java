package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import com.verifyhub.verification.domain.ProviderResult;
import com.verifyhub.verification.domain.ProviderResultRequest;
import com.verifyhub.verification.domain.ProviderResultStatus;
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
class ProviderReturnIntegrationTest {

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
    void providerReturnSuccessCompletesVerificationAndStoresHistoryAndOutbox() throws Exception {
        when(providerClientResilienceDecorator.requestVerification(any(), any()))
                .thenAnswer(this::acceptedProviderResult);
        when(providerClientResilienceDecorator.requestResult(any(), any()))
                .thenAnswer(invocation -> providerResult(invocation, ProviderResultStatus.SUCCESS, true));

        CreatedVerification created = createVerification("return-success");

        mockMvc.perform(post("/api/v1/providers/{provider}/returns", created.provider())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnPayload(created.verificationId(), "web-return-success")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationId", is(created.verificationId())))
                .andExpect(jsonPath("$.data.status", is("SUCCESS")))
                .andExpect(jsonPath("$.data.result", is("SUCCESS")))
                .andExpect(jsonPath("$.data.integrityVerified", is(true)));

        assertThat(verificationStatus(created.verificationId())).isEqualTo("SUCCESS");
        assertThat(verificationWebTransactionId(created.verificationId())).isEqualTo("web-return-success");
        assertThat(historyEventCount(created.verificationId(), "CALLBACK_SUCCESS")).isOne();
        assertThat(historyEventCount(created.verificationId(), "PROVIDER_CALL_SUCCEEDED")).isOne();
        assertThat(outboxEventCount(created.verificationId(), "VERIFICATION_SUCCEEDED")).isOne();
    }

    @Test
    void providerReturnIntegrityFailureMarksVerificationFailAndStoresFailureOutbox() throws Exception {
        when(providerClientResilienceDecorator.requestVerification(any(), any()))
                .thenAnswer(this::acceptedProviderResult);
        when(providerClientResilienceDecorator.requestResult(any(), any()))
                .thenAnswer(invocation -> providerResult(invocation, ProviderResultStatus.SUCCESS, false));

        CreatedVerification created = createVerification("return-integrity-fail");

        mockMvc.perform(post("/api/v1/providers/{provider}/returns", created.provider())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnPayload(created.verificationId(), "web-return-integrity-fail")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("FAIL")))
                .andExpect(jsonPath("$.data.result", is("SUCCESS")))
                .andExpect(jsonPath("$.data.integrityVerified", is(false)));

        assertThat(verificationStatus(created.verificationId())).isEqualTo("FAIL");
        assertThat(historyEventCount(created.verificationId(), "CALLBACK_FAIL")).isOne();
        assertThat(historyEventCount(created.verificationId(), "PROVIDER_CALL_FAILED")).isOne();
        assertThat(outboxEventCount(created.verificationId(), "VERIFICATION_FAILED")).isOne();
    }

    @Test
    void duplicateProviderReturnAfterSuccessStoresLateCallbackWithoutDuplicateOutbox() throws Exception {
        when(providerClientResilienceDecorator.requestVerification(any(), any()))
                .thenAnswer(this::acceptedProviderResult);
        when(providerClientResilienceDecorator.requestResult(any(), any()))
                .thenAnswer(invocation -> providerResult(invocation, ProviderResultStatus.SUCCESS, true));

        CreatedVerification created = createVerification("return-duplicate");
        String webTransactionId = "web-return-duplicate";

        mockMvc.perform(post("/api/v1/providers/{provider}/returns", created.provider())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnPayload(created.verificationId(), webTransactionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("SUCCESS")));

        mockMvc.perform(post("/api/v1/providers/{provider}/returns", created.provider())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnPayload(created.verificationId(), webTransactionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("SUCCESS")))
                .andExpect(jsonPath("$.data.result", is("SUCCESS")));

        assertThat(verificationStatus(created.verificationId())).isEqualTo("SUCCESS");
        assertThat(lateCallbackCount(created.verificationId(), true)).isOne();
        assertThat(outboxEventCount(created.verificationId(), "VERIFICATION_SUCCEEDED")).isOne();
    }

    private CreatedVerification createVerification(String suffix) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/verifications")
                        .header("Idempotency-Key", "idem-" + suffix)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "req-%s",
                                  "purpose": "LOGIN",
                                  "returnUrl": "https://client.example/return",
                                  "closeUrl": "https://client.example/close",
                                  "svcTypes": ["M"]
                                }
                                """.formatted(suffix)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andReturn();
        String body = createResult.getResponse().getContentAsString();
        return new CreatedVerification(
                JsonPath.read(body, "$.data.verificationId"),
                JsonPath.read(body, "$.data.provider")
        );
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

    private ProviderResult providerResult(
            InvocationOnMock invocation,
            ProviderResultStatus resultStatus,
            boolean integrityVerified
    ) {
        ProviderResultRequest request = invocation.getArgument(1);
        return new ProviderResult(
                request.provider(),
                request.providerTransactionId(),
                request.verificationId(),
                resultStatus,
                integrityVerified,
                "{\"result\":\"" + resultStatus.name() + "\",\"integrityVerified\":" + integrityVerified + "}"
        );
    }

    private String returnPayload(String verificationId, String webTransactionId) {
        return """
                {
                  "verificationId": "%s",
                  "webTransactionId": "%s"
                }
                """.formatted(verificationId, webTransactionId);
    }

    private String verificationStatus(String verificationId) {
        return jdbcTemplate.queryForObject(
                "select status from verification_request where verification_id = ?",
                String.class,
                verificationId
        );
    }

    private String verificationWebTransactionId(String verificationId) {
        return jdbcTemplate.queryForObject(
                "select web_transaction_id from verification_request where verification_id = ?",
                String.class,
                verificationId
        );
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

    private int outboxEventCount(String verificationId, String eventType) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from outbox_event where aggregate_id = ? and event_type = ?",
                Integer.class,
                verificationId,
                eventType
        );
        return count == null ? 0 : count;
    }

    private int lateCallbackCount(String verificationId, boolean duplicate) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from late_callback_history where verification_id = ? and duplicate = ?",
                Integer.class,
                verificationId,
                duplicate
        );
        return count == null ? 0 : count;
    }

    private record CreatedVerification(String verificationId, String provider) {
    }
}
