package com.verifyhub.verification.adapter.out.provider.nice;

import com.verifyhub.verification.adapter.out.provider.MockProviderHttpClient;
import com.verifyhub.verification.domain.ProviderType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

@Component
public class NiceProviderClient extends MockProviderHttpClient {

    public NiceProviderClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${verifyhub.provider.nice.base-url}") String baseUrl
    ) {
        super(ProviderType.NICE, restTemplateBuilder, baseUrl);
    }
}
