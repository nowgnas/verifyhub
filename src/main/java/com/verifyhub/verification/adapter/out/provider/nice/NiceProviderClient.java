package com.verifyhub.verification.adapter.out.provider.nice;

import com.verifyhub.verification.adapter.out.provider.MockProviderHttpClient;
import com.verifyhub.verification.domain.AuthEntryType;
import com.verifyhub.verification.domain.ProviderAuthEntry;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderRequestResultType;
import com.verifyhub.verification.domain.ProviderType;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class NiceProviderClient extends MockProviderHttpClient {

    public NiceProviderClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${verifyhub.provider.nice.base-url}") String baseUrl
    ) {
        super(ProviderType.NICE, restTemplateBuilder, baseUrl);
    }

    @Override
    public ProviderRequestResult requestVerification(ProviderRequest request) {
        Instant startedAt = clock.instant();
        NiceTokenResponse tokenResponse = restTemplate.postForObject(
                baseUrl + "/ido/intc/v1.0/auth/token",
                new NiceTokenRequest("client_credentials", request.providerRequestNo()),
                NiceTokenResponse.class
        );
        ResponseEntity<NiceAuthUrlResponse> response = restTemplate.postForEntity(
                baseUrl + "/ido/intc/v1.0/auth/url",
                new NiceAuthUrlRequest(
                        request.providerRequestNo(),
                        request.returnUrl(),
                        request.closeUrl(),
                        request.svcTypes(),
                        "GET"
                ),
                NiceAuthUrlResponse.class
        );
        NiceAuthUrlResponse body = response.getBody();
        return new ProviderRequestResult(
                ProviderType.NICE,
                body == null ? null : body.transactionId(),
                body == null ? null : body.requestNo(),
                body == null ? null : new ProviderAuthEntry(
                        ProviderType.NICE,
                        AuthEntryType.REDIRECT_URL,
                        body.authUrl(),
                        "GET",
                        "UTF-8",
                        Map.of()
                ),
                ProviderRequestResultType.ACCEPTED,
                toRawResponse(body),
                response.getStatusCodeValue(),
                elapsedMillis(startedAt),
                tokenResponse == null ? "NICE token response is empty" : null
        );
    }

    private record NiceTokenRequest(
            String grant_type,
            String request_no
    ) {
    }

    private record NiceTokenResponse(
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

    private record NiceAuthUrlRequest(
            String request_no,
            String return_url,
            String close_url,
            java.util.List<String> svc_types,
            String method_type
    ) {
    }

    private record NiceAuthUrlResponse(
            String result_code,
            String result_message,
            String request_no,
            String auth_url,
            String transaction_id
    ) {

        String requestNo() {
            return request_no;
        }

        String authUrl() {
            return auth_url;
        }

        String transactionId() {
            return transaction_id;
        }
    }
}
