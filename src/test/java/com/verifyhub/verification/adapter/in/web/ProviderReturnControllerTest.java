package com.verifyhub.verification.adapter.in.web;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.verifyhub.verification.application.ProviderReturnCommand;
import com.verifyhub.verification.application.ProviderReturnResult;
import com.verifyhub.verification.application.ProviderReturnService;
import com.verifyhub.verification.domain.ProviderResultStatus;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProviderReturnController.class)
class ProviderReturnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProviderReturnService providerReturnService;

    @Test
    void handlesProviderReturnByGet() throws Exception {
        when(providerReturnService.handleReturn(new ProviderReturnCommand(
                ProviderType.NICE,
                "verif-1",
                "web-tx-1"
        ))).thenReturn(successResult());

        mockMvc.perform(get("/api/v1/providers/NICE/returns")
                        .param("verificationId", "verif-1")
                        .param("webTransactionId", "web-tx-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationId", is("verif-1")))
                .andExpect(jsonPath("$.data.provider", is("NICE")))
                .andExpect(jsonPath("$.data.status", is("SUCCESS")))
                .andExpect(jsonPath("$.data.result", is("SUCCESS")))
                .andExpect(jsonPath("$.data.integrityVerified", is(true)));
    }

    @Test
    void handlesProviderReturnByPost() throws Exception {
        when(providerReturnService.handleReturn(new ProviderReturnCommand(
                ProviderType.KG,
                "verif-2",
                "web-tx-2"
        ))).thenReturn(new ProviderReturnResult(
                "verif-2",
                ProviderType.KG,
                VerificationStatus.FAIL,
                ProviderResultStatus.FAIL,
                true
        ));

        mockMvc.perform(post("/api/v1/providers/KG/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "verificationId": "verif-2",
                                  "webTransactionId": "web-tx-2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationId", is("verif-2")))
                .andExpect(jsonPath("$.data.provider", is("KG")))
                .andExpect(jsonPath("$.data.status", is("FAIL")))
                .andExpect(jsonPath("$.data.result", is("FAIL")));
    }

    @Test
    void rejectsInvalidPostBody() throws Exception {
        mockMvc.perform(post("/api/v1/providers/NICE/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "verificationId": "",
                                  "webTransactionId": "web-tx-1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST")));
    }

    private ProviderReturnResult successResult() {
        return new ProviderReturnResult(
                "verif-1",
                ProviderType.NICE,
                VerificationStatus.SUCCESS,
                ProviderResultStatus.SUCCESS,
                true
        );
    }
}
