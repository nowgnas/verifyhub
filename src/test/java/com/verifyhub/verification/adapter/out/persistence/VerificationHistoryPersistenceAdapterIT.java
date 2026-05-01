package com.verifyhub.verification.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.verifyhub.verification.domain.VerificationEvent;
import com.verifyhub.verification.domain.VerificationHistory;
import com.verifyhub.verification.domain.VerificationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class VerificationHistoryPersistenceAdapterIT {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("verifyhub")
            .withUsername("verifyhub")
            .withPassword("verifyhub");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private VerificationHistoryPersistenceAdapter verificationHistoryPersistenceAdapter;

    @Test
    void findsHistoriesByVerificationIdOrderedByCreatedAtAscending() {
        VerificationHistory later = VerificationHistory.of(
                "verif-history-1",
                VerificationStatus.REQUESTED,
                VerificationStatus.ROUTED,
                VerificationEvent.ROUTE_SELECTED,
                "routed",
                null,
                null,
                LocalDateTime.of(2026, 5, 1, 12, 1)
        );
        VerificationHistory earlier = VerificationHistory.of(
                "verif-history-1",
                null,
                VerificationStatus.REQUESTED,
                VerificationEvent.VERIFICATION_REQUESTED,
                "requested",
                null,
                null,
                LocalDateTime.of(2026, 5, 1, 12, 0)
        );
        VerificationHistory otherVerification = VerificationHistory.of(
                "verif-history-2",
                null,
                VerificationStatus.REQUESTED,
                VerificationEvent.VERIFICATION_REQUESTED,
                "requested",
                null,
                null,
                LocalDateTime.of(2026, 5, 1, 11, 59)
        );
        verificationHistoryPersistenceAdapter.save(later);
        verificationHistoryPersistenceAdapter.save(otherVerification);
        verificationHistoryPersistenceAdapter.save(earlier);

        List<VerificationHistory> histories = verificationHistoryPersistenceAdapter
                .findByVerificationIdOrderByCreatedAtAsc("verif-history-1");

        assertThat(histories)
                .extracting(VerificationHistory::eventType)
                .containsExactly(
                        VerificationEvent.VERIFICATION_REQUESTED,
                        VerificationEvent.ROUTE_SELECTED
                );
    }
}
