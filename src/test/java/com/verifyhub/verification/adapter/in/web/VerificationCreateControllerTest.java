package com.verifyhub.verification.adapter.in.web;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.verifyhub.verification.application.ProviderVerificationResult;
import com.verifyhub.verification.application.VerificationCreateService;
import com.verifyhub.verification.domain.AuthEntryType;
import com.verifyhub.verification.domain.ProviderAuthEntry;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationStatus;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VerificationCreateController.class)
class VerificationCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VerificationCreateService verificationCreateService;

    @Test
    void createsVerification() throws Exception {
        when(verificationCreateService.create(any())).thenReturn(new ProviderVerificationResult(
                "verif-1",
                ProviderType.NICE,
                VerificationStatus.IN_PROGRESS,
                new ProviderAuthEntry(
                        ProviderType.NICE,
                        AuthEntryType.REDIRECT_URL,
                        "https://nice.example/auth",
                        "GET",
                        "UTF-8",
                        Map.of()
                )
        ));

        mockMvc.perform(post("/api/v1/verifications")
                        .header("Idempotency-Key", "idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "req-1",
                                  "purpose": "LOGIN",
                                  "returnUrl": "https://client.example/return",
                                  "closeUrl": "https://client.example/close",
                                  "svcTypes": ["M"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationId", is("verif-1")))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.provider", is("NICE")))
                .andExpect(jsonPath("$.data.authEntry.type", is("REDIRECT_URL")))
                .andExpect(jsonPath("$.data.authEntry.url", is("https://nice.example/auth")));
    }

    @Test
    void rejectsMissingIdempotencyKey() throws Exception {
        mockMvc.perform(post("/api/v1/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "req-1",
                                  "purpose": "LOGIN",
                                  "returnUrl": "https://client.example/return",
                                  "svcTypes": ["M"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST")))
                .andExpect(jsonPath("$.message", is("Idempotency-Key header is required")));
    }

    @Test
    void rejectsInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/verifications")
                        .header("Idempotency-Key", "idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "",
                                  "purpose": "LOGIN",
                                  "returnUrl": "https://client.example/return",
                                  "svcTypes": ["M"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST")));
    }
}
