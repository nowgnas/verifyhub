package com.verifyhub.verification.adapter.in.web;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.verifyhub.common.exception.VerificationNotFoundException;
import com.verifyhub.verification.application.VerificationQueryService;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationEvent;
import com.verifyhub.verification.domain.VerificationHistory;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.domain.VerificationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VerificationQueryController.class)
class VerificationQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VerificationQueryService verificationQueryService;

    @Test
    void getsVerificationStatus() throws Exception {
        Verification verification = Verification.rehydrate(
                1L,
                "verif-1",
                "req-1",
                VerificationPurpose.LOGIN,
                "idem-1",
                ProviderType.NICE,
                VerificationStatus.SUCCESS,
                "nice-tx-1",
                "nice-req-1",
                null,
                1L,
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 1, 10, 1),
                LocalDateTime.of(2026, 5, 1, 10, 2),
                LocalDateTime.of(2026, 5, 1, 10, 3),
                0L
        );
        when(verificationQueryService.getVerification("verif-1")).thenReturn(verification);

        mockMvc.perform(get("/api/v1/verifications/verif-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationId", is("verif-1")))
                .andExpect(jsonPath("$.data.status", is("SUCCESS")))
                .andExpect(jsonPath("$.data.provider", is("NICE")))
                .andExpect(jsonPath("$.data.purpose", is("LOGIN")))
                .andExpect(jsonPath("$.data.requestedAt", is("2026-05-01T10:00:00")))
                .andExpect(jsonPath("$.data.completedAt", is("2026-05-01T10:03:00")));
    }

    @Test
    void getsVerificationHistories() throws Exception {
        VerificationHistory requested = VerificationHistory.of(
                "verif-2",
                null,
                VerificationStatus.REQUESTED,
                VerificationEvent.VERIFICATION_REQUESTED,
                "requested",
                null,
                null,
                LocalDateTime.of(2026, 5, 1, 11, 0)
        );
        VerificationHistory routed = VerificationHistory.of(
                "verif-2",
                VerificationStatus.REQUESTED,
                VerificationStatus.ROUTED,
                VerificationEvent.ROUTE_SELECTED,
                "routed",
                ProviderType.KG,
                null,
                LocalDateTime.of(2026, 5, 1, 11, 1)
        );
        when(verificationQueryService.getHistories("verif-2")).thenReturn(List.of(requested, routed));

        mockMvc.perform(get("/api/v1/verifications/verif-2/histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationId", is("verif-2")))
                .andExpect(jsonPath("$.data.histories[0].toStatus", is("REQUESTED")))
                .andExpect(jsonPath("$.data.histories[0].eventType", is("VERIFICATION_REQUESTED")))
                .andExpect(jsonPath("$.data.histories[0].createdAt", is("2026-05-01T11:00:00")))
                .andExpect(jsonPath("$.data.histories[1].fromStatus", is("REQUESTED")))
                .andExpect(jsonPath("$.data.histories[1].toStatus", is("ROUTED")))
                .andExpect(jsonPath("$.data.histories[1].provider", is("KG")));
    }

    @Test
    void returnsNotFoundWhenVerificationDoesNotExist() throws Exception {
        when(verificationQueryService.getVerification("missing"))
                .thenThrow(new VerificationNotFoundException("missing"));

        mockMvc.perform(get("/api/v1/verifications/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("VERIFICATION_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Verification request was not found: missing")));
    }
}
