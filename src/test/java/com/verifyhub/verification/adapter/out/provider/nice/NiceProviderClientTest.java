package com.verifyhub.verification.adapter.out.provider.nice;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
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
        niceProviderClient = new NiceProviderClient(new RestTemplateBuilder(), wireMockServer.baseUrl());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void requestsVerificationAsNiceProvider() {
        wireMockServer.stubFor(post(urlEqualTo("/verifications"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                  "providerTransactionId": "nice-tx-1",
                                  "providerRequestNo": "nice-req-1",
                                  "authUrl": "https://nice.example/auth",
                                  "resultType": "ACCEPTED"
                                }
                                """)));

        ProviderRequestResult result = niceProviderClient.requestVerification(new ProviderRequest(
                "ver_123",
                "req_123",
                "tester",
                "01012345678",
                "19900101",
                "LOGIN"
        ));

        assertThat(niceProviderClient.providerType()).isEqualTo(ProviderType.NICE);
        assertThat(result.provider()).isEqualTo(ProviderType.NICE);
        assertThat(result.providerTransactionId()).isEqualTo("nice-tx-1");
        assertThat(result.providerRequestNo()).isEqualTo("nice-req-1");
        assertThat(result.authUrl()).isEqualTo("https://nice.example/auth");
        assertThat(result.resultType()).isEqualTo(ProviderRequestResultType.ACCEPTED);
    }
}
