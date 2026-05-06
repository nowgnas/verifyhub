package com.verifyhub.mockprovider.adapter.in.web;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.verifyhub.mockprovider.application.MockProviderScenarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MockPhoneAuthProviderController.class)
@Import(MockProviderScenarioService.class)
class MockPhoneAuthProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void resetScenarios() throws Exception {
        setScenario("KG", "SUCCESS");
        setScenario("NICE", "SUCCESS");
    }

    @Test
    void issuesNiceAuthUrlWithNiceCompatibleEndpoint() throws Exception {
        mockMvc.perform(post("/mock/providers/NICE/ido/intc/v1.0/auth/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "request_no": "nice-req-1",
                                  "return_url": "https://verifyhub.example/api/v1/providers/NICE/returns",
                                  "close_url": "https://verifyhub.example/api/v1/providers/NICE/close",
                                  "svc_types": ["M"],
                                  "method_type": "GET"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result_code", is("0000")))
                .andExpect(jsonPath("$.request_no", is("nice-req-1")))
                .andExpect(jsonPath("$.transaction_id", is("nice-tx-nice-req-1")))
                .andExpect(jsonPath("$.auth_url", is("http://localhost:8080/mock/providers/NICE/standard-window?request_no=nice-req-1")));
    }

    @Test
    void issuesKgFormEntryWithKgCompatibleEndpoint() throws Exception {
        mockMvc.perform(post("/mock/providers/KG/goCashMain.mcash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "verificationId": "verif-1",
                                  "requestId": "req-1",
                                  "providerRequestNo": "kg-trade-1",
                                  "returnUrl": "https://verifyhub.example/api/v1/providers/KG/noti",
                                  "closeUrl": "https://verifyhub.example/api/v1/providers/KG/close",
                                  "purpose": "LOGIN",
                                  "svcTypes": ["M"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerTransactionId", is("kg-tx-kg-trade-1")))
                .andExpect(jsonPath("$.providerRequestNo", is("kg-trade-1")))
                .andExpect(jsonPath("$.authEntry.type", is("FORM_POST")))
                .andExpect(jsonPath("$.authEntry.charset", is("EUC-KR")))
                .andExpect(jsonPath("$.authEntry.fields.Tradeid", is("kg-trade-1")))
                .andExpect(jsonPath("$.resultType", is("ACCEPTED")));
    }

    @Test
    void acceptsKgNotiUrlTemporarily() throws Exception {
        mockMvc.perform(post("/mock/providers/KG/noti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "Tradeid": "kg-trade-1",
                                  "Resultcd": "0000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("SUCCESS"));
    }

    @Test
    void changesScenarioAndAppliesItToCommonVerificationAndResultEndpoints() throws Exception {
        mockMvc.perform(post("/mock/providers/NICE/scenario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scenario": "INVALID_INTEGRITY_RESULT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider", is("NICE")))
                .andExpect(jsonPath("$.scenario", is("INVALID_INTEGRITY_RESULT")));

        mockMvc.perform(post("/mock/providers/NICE/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "verificationId": "verif-1",
                                  "requestId": "req-1",
                                  "providerRequestNo": "nice-req-1",
                                  "returnUrl": "https://verifyhub.example/api/v1/providers/NICE/returns",
                                  "closeUrl": "https://verifyhub.example/api/v1/providers/NICE/close",
                                  "purpose": "LOGIN",
                                  "svcTypes": ["M"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerTransactionId", is("nice-tx-nice-req-1")))
                .andExpect(jsonPath("$.providerRequestNo", is("nice-req-1")))
                .andExpect(jsonPath("$.authEntry.type", is("REDIRECT_URL")))
                .andExpect(jsonPath("$.resultType", is("ACCEPTED")));

        mockMvc.perform(post("/mock/providers/NICE/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "NICE",
                                  "providerTransactionId": "nice-tx-nice-req-1",
                                  "providerRequestNo": "nice-req-1",
                                  "verificationId": "verif-1",
                                  "webTransactionId": "web-tx-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerTransactionId", is("nice-tx-nice-req-1")))
                .andExpect(jsonPath("$.verificationId", is("verif-1")))
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.integrityVerified", is(false)));
    }

    @Test
    void appliesScenarioToKgCompatibleVerificationEndpoint() throws Exception {
        mockMvc.perform(post("/mock/providers/KG/scenario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scenario": "FAIL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenario", is("FAIL")));

        mockMvc.perform(post("/mock/providers/KG/goCashMain.mcash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "verificationId": "verif-1",
                                  "requestId": "req-1",
                                  "providerRequestNo": "kg-trade-fail",
                                  "returnUrl": "https://verifyhub.example/api/v1/providers/KG/noti",
                                  "closeUrl": "https://verifyhub.example/api/v1/providers/KG/close",
                                  "purpose": "LOGIN",
                                  "svcTypes": ["M"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerTransactionId", is("kg-tx-kg-trade-fail")))
                .andExpect(jsonPath("$.providerRequestNo", is("kg-trade-fail")))
                .andExpect(jsonPath("$.authEntry").doesNotExist())
                .andExpect(jsonPath("$.resultType", is("FAIL")));
    }

    @Test
    void rejectsUnsupportedScenario() throws Exception {
        mockMvc.perform(post("/mock/providers/KG/scenario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scenario": "UNKNOWN"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private void setScenario(String provider, String scenario) throws Exception {
        mockMvc.perform(post("/mock/providers/{provider}/scenario", provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "scenario": "%s"
                        }
                        """.formatted(scenario)));
    }
}
