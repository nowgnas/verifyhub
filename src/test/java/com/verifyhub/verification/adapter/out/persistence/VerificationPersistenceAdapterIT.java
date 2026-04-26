package com.verifyhub.verification.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.verifyhub.verification.adapter.out.persistence.entity.VerificationEntity;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.domain.VerificationStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
class VerificationPersistenceAdapterIT {

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
    private VerificationPersistenceAdapter verificationPersistenceAdapter;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void savesAndFindsVerificationByVerificationId() {
        Verification verification = Verification.requested(
                "verif_123",
                "req_123",
                VerificationPurpose.SIGN_UP,
                "idem-123",
                LocalDateTime.of(2026, 4, 26, 10, 0)
        );

        Verification saved = verificationPersistenceAdapter.save(verification);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVerificationId()).isEqualTo("verif_123");
        assertThat(saved.getStatus()).isEqualTo(VerificationStatus.REQUESTED);

        Optional<Verification> found = verificationPersistenceAdapter.findByVerificationId("verif_123");
        assertThat(found).isPresent();
        assertThat(found.orElseThrow().getVerificationId()).isEqualTo("verif_123");
        assertThat(found.orElseThrow().getRequestId()).isEqualTo("req_123");
        assertThat(found.orElseThrow().getPurpose()).isEqualTo(VerificationPurpose.SIGN_UP);
    }

    @Test
    void findsVerificationByRequestIdPurposeAndIdempotencyKey() {
        Verification verification = Verification.requested(
                "verif_456",
                "req_456",
                VerificationPurpose.LOGIN,
                "idem-456",
                LocalDateTime.of(2026, 4, 26, 11, 0)
        );
        verificationPersistenceAdapter.save(verification);

        Optional<Verification> found = verificationPersistenceAdapter.findByRequestIdAndPurposeAndIdempotencyKey(
                "req_456",
                VerificationPurpose.LOGIN,
                "idem-456"
        );

        assertThat(found).isPresent();
        assertThat(found.orElseThrow().getVerificationId()).isEqualTo("verif_456");
    }

    @Test
    void updatesOptimisticVersionWhenSavingExistingVerification() {
        Verification verification = Verification.requested(
                "verif_789",
                "req_789",
                VerificationPurpose.PASSWORD_RESET,
                "idem-789",
                LocalDateTime.of(2026, 4, 26, 12, 0)
        );
        Verification saved = verificationPersistenceAdapter.save(verification);

        entityManager.clear();

        Verification reloaded = verificationPersistenceAdapter.findByVerificationId("verif_789").orElseThrow();
        reloaded.routeTo(ProviderType.NICE, 1L, LocalDateTime.of(2026, 4, 26, 12, 1));
        Verification updated = verificationPersistenceAdapter.save(reloaded);

        assertThat(updated.getVersion()).isGreaterThan(saved.getVersion());
    }
}
