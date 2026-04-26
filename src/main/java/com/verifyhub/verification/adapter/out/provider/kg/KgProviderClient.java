package com.verifyhub.verification.adapter.out.provider.kg;

import com.verifyhub.verification.adapter.out.provider.MockProviderHttpClient;
import com.verifyhub.verification.domain.ProviderType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

@Component
public class KgProviderClient extends MockProviderHttpClient {

    public KgProviderClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${verifyhub.provider.kg.base-url}") String baseUrl
    ) {
        super(ProviderType.KG, restTemplateBuilder, baseUrl);
    }
}
