package com.verifyhub.mockprovider.adapter.in.web;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MockPhoneAuthProviderController.class)
class MockPhoneAuthProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
}
