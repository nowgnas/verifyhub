package com.verifyhub.mockprovider.adapter.in.web;

import com.verifyhub.mockprovider.application.MockProviderScenarioService;
import com.verifyhub.mockprovider.domain.MockProviderScenario;
import com.verifyhub.verification.domain.AuthEntryType;
import com.verifyhub.verification.domain.ProviderAuthEntry;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderRequestResultType;
import com.verifyhub.verification.domain.ProviderResult;
import com.verifyhub.verification.domain.ProviderResultRequest;
import com.verifyhub.verification.domain.ProviderResultStatus;
import com.verifyhub.verification.domain.ProviderType;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock/providers")
public class MockPhoneAuthProviderController {

    private static final long TIMEOUT_DELAY_MILLIS = 2_500L;

    private final MockProviderScenarioService mockProviderScenarioService;

    public MockPhoneAuthProviderController(MockProviderScenarioService mockProviderScenarioService) {
        this.mockProviderScenarioService = mockProviderScenarioService;
    }

    @PostMapping("/{provider}/scenario")
    public MockProviderScenarioResponse changeScenario(
            @PathVariable ProviderType provider,
            @Valid @RequestBody MockProviderScenarioRequest request
    ) {
        MockProviderScenario scenario = mockProviderScenarioService.setScenario(provider, request.scenario());
        return new MockProviderScenarioResponse(provider, scenario);
    }

    @PostMapping("/{provider}/verifications")
    public ProviderRequestResult requestVerification(
            @PathVariable ProviderType provider,
            @RequestBody ProviderRequest request
    ) {
        MockProviderScenario scenario = mockProviderScenarioService.getScenario(provider);
        if (scenario == MockProviderScenario.HTTP_500) {
            throw new MockProviderHttpException();
        }
        if (scenario == MockProviderScenario.TIMEOUT) {
            delayForTimeout();
            return new ProviderRequestResult(
                    provider,
                    null,
                    request.providerRequestNo(),
                    null,
                    ProviderRequestResultType.TIMEOUT,
                    "{\"resultType\":\"TIMEOUT\"}",
                    504,
                    0L,
                    "mock provider timeout"
            );
        }
        if (scenario == MockProviderScenario.FAIL) {
            return new ProviderRequestResult(
                    provider,
                    provider.name().toLowerCase() + "-tx-" + request.providerRequestNo(),
                    request.providerRequestNo(),
                    null,
                    ProviderRequestResultType.FAIL,
                    "{\"resultType\":\"FAIL\"}",
                    200,
                    0L,
                    null
            );
        }
        return acceptedProviderRequestResult(provider, request);
    }

    @PostMapping("/{provider}/results")
    public ProviderResult requestResult(
            @PathVariable ProviderType provider,
            @RequestBody ProviderResultRequest request
    ) {
        MockProviderScenario scenario = mockProviderScenarioService.getScenario(provider);
        if (scenario == MockProviderScenario.HTTP_500) {
            throw new MockProviderHttpException();
        }
        if (scenario == MockProviderScenario.TIMEOUT) {
            delayForTimeout();
        }
        if (scenario == MockProviderScenario.FAIL) {
            return new ProviderResult(
                    provider,
                    request.providerTransactionId(),
                    request.verificationId(),
                    ProviderResultStatus.FAIL,
                    true,
                    "{\"result\":\"FAIL\"}"
            );
        }
        boolean integrityVerified = scenario != MockProviderScenario.INVALID_INTEGRITY_RESULT;
        return new ProviderResult(
                provider,
                request.providerTransactionId(),
                request.verificationId(),
                ProviderResultStatus.SUCCESS,
                integrityVerified,
                "{\"result\":\"SUCCESS\",\"integrityVerified\":" + integrityVerified + "}"
        );
    }

    @GetMapping("/{provider}/returns")
    public MockProviderReturnResponse getReturn(@PathVariable ProviderType provider) {
        return new MockProviderReturnResponse(provider, mockProviderScenarioService.getScenario(provider));
    }

    @PostMapping("/{provider}/returns")
    public MockProviderReturnResponse postReturn(@PathVariable ProviderType provider) {
        return new MockProviderReturnResponse(provider, mockProviderScenarioService.getScenario(provider));
    }

    @PostMapping("/NICE/ido/intc/{version}/auth/token")
    public NiceTokenResponse issueNiceToken(
            @PathVariable String version,
            @RequestBody NiceTokenRequest request
    ) {
        applyHttpScenario(ProviderType.NICE);
        return new NiceTokenResponse(
                "0000",
                "응답성공",
                request.request_no(),
                "mock-access-token-" + request.request_no(),
                1762940529000L,
                "Bearer",
                66,
                "mock-ticket-" + request.request_no()
        );
    }

    @PostMapping("/NICE/ido/intc/{version}/auth/url")
    public NiceAuthUrlResponse issueNiceAuthUrl(
            @PathVariable String version,
            @RequestBody NiceAuthUrlRequest request
    ) {
        applyHttpScenario(ProviderType.NICE);
        return new NiceAuthUrlResponse(
                "0000",
                "응답성공",
                request.request_no(),
                "http://localhost:8080/mock/providers/NICE/standard-window?request_no=" + request.request_no(),
                "nice-tx-" + request.request_no()
        );
    }

    @PostMapping("/NICE/ido/intc/{version}/auth/result")
    public NiceAuthResultResponse issueNiceAuthResult(
            @PathVariable String version,
            @RequestBody NiceAuthResultRequest request
    ) {
        applyHttpScenario(ProviderType.NICE);
        return new NiceAuthResultResponse(
                "0000",
                "응답성공",
                "mock-enc-data-" + request.web_transaction_id(),
                "mock-integrity-value"
        );
    }

    @PostMapping("/KG/goCashMain.mcash")
    public ProviderRequestResult issueKgFormEntry(@RequestBody ProviderRequest request) {
        return requestVerification(ProviderType.KG, request);
    }

    @PostMapping("/KG/noti")
    public String acceptKgNoti(@RequestBody Map<String, Object> payload) {
        return "SUCCESS";
    }

    private ProviderRequestResult acceptedProviderRequestResult(ProviderType provider, ProviderRequest request) {
        return new ProviderRequestResult(
                provider,
                provider.name().toLowerCase() + "-tx-" + request.providerRequestNo(),
                request.providerRequestNo(),
                authEntry(provider, request),
                ProviderRequestResultType.ACCEPTED,
                "{\"resultType\":\"ACCEPTED\"}",
                200,
                0L,
                null
        );
    }

    private ProviderAuthEntry authEntry(ProviderType provider, ProviderRequest request) {
        if (provider == ProviderType.KG) {
            return new ProviderAuthEntry(
                    ProviderType.KG,
                    AuthEntryType.FORM_POST,
                    "https://auth.mobilians.co.kr/goCashMain.mcash",
                    "POST",
                    "EUC-KR",
                    Map.of(
                            "Tradeid", request.providerRequestNo(),
                            "Notiurl", request.returnUrl(),
                            "Okurl", request.closeUrl() == null ? "" : request.closeUrl(),
                            "CALL_TYPE", "SELF",
                            "Sendtype", "SMS"
                    )
            );
        }
        return new ProviderAuthEntry(
                provider,
                AuthEntryType.REDIRECT_URL,
                "http://localhost:8080/mock/providers/" + provider.name() + "/standard-window?request_no=" + request.providerRequestNo(),
                "GET",
                "UTF-8",
                Map.of()
        );
    }

    private void delayForTimeout() {
        try {
            Thread.sleep(TIMEOUT_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new MockProviderHttpException();
        }
    }

    private void applyHttpScenario(ProviderType provider) {
        MockProviderScenario scenario = mockProviderScenarioService.getScenario(provider);
        if (scenario == MockProviderScenario.HTTP_500) {
            throw new MockProviderHttpException();
        }
        if (scenario == MockProviderScenario.TIMEOUT) {
            delayForTimeout();
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private static class MockProviderHttpException extends RuntimeException {
    }

    public record MockProviderScenarioRequest(
            @NotNull MockProviderScenario scenario
    ) {
    }

    public record MockProviderScenarioResponse(
            ProviderType provider,
            MockProviderScenario scenario
    ) {
    }

    public record MockProviderReturnResponse(
            ProviderType provider,
            MockProviderScenario scenario
    ) {
    }

    public record NiceTokenRequest(
            String grant_type,
            String request_no
    ) {
    }

    public record NiceTokenResponse(
            String result_code,
            String result_message,
            String request_no,
            String access_token,
            Long expires_in,
            String token_type,
            Integer iterators,
            String ticket
    ) {
    }

    public record NiceAuthUrlRequest(
            String request_no,
            String return_url,
            String close_url,
            List<String> svc_types,
            String method_type
    ) {
    }

    public record NiceAuthUrlResponse(
            String result_code,
            String result_message,
            String request_no,
            String auth_url,
            String transaction_id
    ) {
    }

    public record NiceAuthResultRequest(
            String request_no,
            String transaction_id,
            String web_transaction_id
    ) {
    }

    public record NiceAuthResultResponse(
            String result_code,
            String result_message,
            String enc_data,
            String integrity_value
    ) {
    }
}
