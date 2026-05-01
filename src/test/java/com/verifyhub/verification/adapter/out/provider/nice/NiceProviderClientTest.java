package com.verifyhub.verification.adapter.out.provider.nice;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.verifyhub.verification.domain.AuthEntryType;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderRequestResultType;
import com.verifyhub.verification.domain.ProviderType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

class NiceProviderClientTest {

    private WireMockServer wireMockServer;
    private NiceProviderClient niceProviderClient;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        niceProviderClient = new NiceProviderClient(new RestTemplateBuilder(), wireMockServer.baseUrl());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void requestsVerificationAsNiceProvider() {
        wireMockServer.stubFor(post(urlEqualTo("/ido/intc/v1.0/auth/token"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                  "result_code": "0000",
                                  "result_message": "응답성공",
                                  "request_no": "nice-req-1",
                                  "access_token": "access-token-1",
                                  "expires_in": 1762940529000,
                                  "token_type": "Bearer",
                                  "iterators": 66,
                                  "ticket": "ticket-1"
                                }
                                """)));
        wireMockServer.stubFor(post(urlEqualTo("/ido/intc/v1.0/auth/url"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                  "result_code": "0000",
                                  "result_message": "응답성공",
                                  "request_no": "nice-req-1",
                                  "auth_url": "https://nice.example/auth",
                                  "transaction_id": "nice-tx-1"
                                }
                                """)));

        ProviderRequestResult result = niceProviderClient.requestVerification(new ProviderRequest(
                "ver_123",
                "req_123",
                "nice-req-1",
                "https://verifyhub.example/api/v1/providers/NICE/returns",
                "https://verifyhub.example/api/v1/providers/NICE/close",
                "LOGIN",
                java.util.List.of("M")
        ));

        assertThat(niceProviderClient.providerType()).isEqualTo(ProviderType.NICE);
        assertThat(result.provider()).isEqualTo(ProviderType.NICE);
        assertThat(result.providerTransactionId()).isEqualTo("nice-tx-1");
        assertThat(result.providerRequestNo()).isEqualTo("nice-req-1");
        assertThat(result.authEntry().type()).isEqualTo(AuthEntryType.REDIRECT_URL);
        assertThat(result.authEntry().url()).isEqualTo("https://nice.example/auth");
        assertThat(result.authEntry().method()).isEqualTo("GET");
        assertThat(result.resultType()).isEqualTo(ProviderRequestResultType.ACCEPTED);
        wireMockServer.verify(postRequestedFor(urlEqualTo("/ido/intc/v1.0/auth/token"))
                .withRequestBody(equalToJson("""
                        {
                          "grant_type": "client_credentials",
                          "request_no": "nice-req-1"
                        }
                        """)));
        wireMockServer.verify(postRequestedFor(urlEqualTo("/ido/intc/v1.0/auth/url"))
                .withRequestBody(equalToJson("""
                        {
                          "request_no": "nice-req-1",
                          "return_url": "https://verifyhub.example/api/v1/providers/NICE/returns",
                          "close_url": "https://verifyhub.example/api/v1/providers/NICE/close",
                          "svc_types": ["M"],
                          "method_type": "GET"
                        }
                        """)));
    }
}
