package com.verifyhub.mockprovider.application;

import com.verifyhub.mockprovider.domain.MockProviderScenario;
import com.verifyhub.verification.domain.ProviderType;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MockProviderScenarioService {

    private final Map<ProviderType, MockProviderScenario> scenarios = new EnumMap<>(ProviderType.class);

    public MockProviderScenarioService() {
        for (ProviderType provider : ProviderType.values()) {
            scenarios.put(provider, MockProviderScenario.SUCCESS);
        }
    }

    public synchronized MockProviderScenario setScenario(ProviderType provider, MockProviderScenario scenario) {
        scenarios.put(provider, scenario);
        return scenario;
    }

    public synchronized MockProviderScenario getScenario(ProviderType provider) {
        return scenarios.getOrDefault(provider, MockProviderScenario.SUCCESS);
    }
}
