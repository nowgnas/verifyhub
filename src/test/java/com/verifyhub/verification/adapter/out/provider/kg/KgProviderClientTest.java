package com.verifyhub.verification.adapter.out.provider.kg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderRequestResultType;
import com.verifyhub.verification.domain.ProviderResult;
import com.verifyhub.verification.domain.ProviderResultRequest;
import com.verifyhub.verification.domain.ProviderResultStatus;
import com.verifyhub.verification.domain.ProviderType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

class KgProviderClientTest {

    private WireMockServer wireMockServer;
    private KgProviderClient kgProviderClient;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        kgProviderClient = new KgProviderClient(new RestTemplateBuilder(), wireMockServer.baseUrl());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void requestsVerification() {
        wireMockServer.stubFor(post(urlEqualTo("/verifications"))
                .withRequestBody(equalToJson("""
                        {
                          "verificationId": "ver_123",
                          "requestId": "req_123",
                          "name": "tester",
                          "phoneNumber": "01012345678",
                          "birthDate": "19900101",
                          "purpose": "LOGIN"
                        }
                        """))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                  "providerTransactionId": "kg-tx-1",
                                  "providerRequestNo": "kg-req-1",
                                  "authUrl": "https://kg.example/auth",
                                  "resultType": "ACCEPTED"
                                }
                                """)));

        ProviderRequestResult result = kgProviderClient.requestVerification(new ProviderRequest(
                "ver_123",
                "req_123",
                "tester",
                "01012345678",
                "19900101",
                "LOGIN"
        ));

        assertThat(result.provider()).isEqualTo(ProviderType.KG);
        assertThat(result.providerTransactionId()).isEqualTo("kg-tx-1");
        assertThat(result.providerRequestNo()).isEqualTo("kg-req-1");
        assertThat(result.authUrl()).isEqualTo("https://kg.example/auth");
        assertThat(result.resultType()).isEqualTo(ProviderRequestResultType.ACCEPTED);
        assertThat(result.httpStatus()).isEqualTo(200);
        assertThat(result.latencyMs()).isNotNegative();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void requestsResult() {
        wireMockServer.stubFor(post(urlEqualTo("/results"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                  "providerTransactionId": "kg-tx-1",
                                  "verificationId": "ver_123",
                                  "result": "SUCCESS",
                                  "integrityVerified": true
                                }
                                """)));

        ProviderResult result = kgProviderClient.requestResult(new ProviderResultRequest(
                ProviderType.KG,
                "kg-tx-1",
                "kg-req-1",
                "ver_123",
                "web-tx-1"
        ));

        assertThat(result.provider()).isEqualTo(ProviderType.KG);
        assertThat(result.providerTransactionId()).isEqualTo("kg-tx-1");
        assertThat(result.verificationId()).isEqualTo("ver_123");
        assertThat(result.result()).isEqualTo(ProviderResultStatus.SUCCESS);
        assertThat(result.integrityVerified()).isTrue();

        wireMockServer.verify(postRequestedFor(urlEqualTo("/results"))
                .withRequestBody(equalToJson("""
                        {
                          "provider": "KG",
                          "providerTransactionId": "kg-tx-1",
                          "providerRequestNo": "kg-req-1",
                          "verificationId": "ver_123",
                          "webTransactionId": "web-tx-1"
                        }
                        """)));
    }
}
