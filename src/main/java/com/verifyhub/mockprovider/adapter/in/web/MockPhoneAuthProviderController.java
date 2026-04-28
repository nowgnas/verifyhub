package com.verifyhub.mockprovider.adapter.in.web;

import com.verifyhub.verification.domain.AuthEntryType;
import com.verifyhub.verification.domain.ProviderAuthEntry;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderRequestResultType;
import com.verifyhub.verification.domain.ProviderType;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock/providers")
public class MockPhoneAuthProviderController {

    @PostMapping("/NICE/ido/intc/{version}/auth/token")
    public NiceTokenResponse issueNiceToken(
            @PathVariable String version,
            @RequestBody NiceTokenRequest request
    ) {
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
        return new NiceAuthResultResponse(
                "0000",
                "응답성공",
                "mock-enc-data-" + request.web_transaction_id(),
                "mock-integrity-value"
        );
    }

    @PostMapping("/KG/goCashMain.mcash")
    public ProviderRequestResult issueKgFormEntry(@RequestBody ProviderRequest request) {
        return new ProviderRequestResult(
                ProviderType.KG,
                "kg-tx-" + request.providerRequestNo(),
                request.providerRequestNo(),
                new ProviderAuthEntry(
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
                ),
                ProviderRequestResultType.ACCEPTED,
                "{\"resultType\":\"ACCEPTED\"}",
                200,
                0L,
                null
        );
    }

    @PostMapping("/KG/noti")
    public String acceptKgNoti(@RequestBody Map<String, Object> payload) {
        return "SUCCESS";
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
