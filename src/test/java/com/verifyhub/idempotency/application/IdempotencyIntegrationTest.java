package com.verifyhub.idempotency.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationPurpose;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class IdempotencyIntegrationTest {

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
    private IdempotencyService idempotencyService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void returnsSameVerificationForSameRequestPurposeAndIdempotencyKey() {
        AtomicInteger creatorCalls = new AtomicInteger();

        Verification first = idempotencyService.getOrCreate(
                "req_same",
                VerificationPurpose.LOGIN,
                "idem-same",
                () -> requested("verif_same_1", "req_same", VerificationPurpose.LOGIN, "idem-same", creatorCalls)
        );

        Verification second = idempotencyService.getOrCreate(
                "req_same",
                VerificationPurpose.LOGIN,
                "idem-same",
                () -> requested("verif_same_2", "req_same", VerificationPurpose.LOGIN, "idem-same", creatorCalls)
        );

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(second.getVerificationId()).isEqualTo(first.getVerificationId());
        assertThat(creatorCalls).hasValue(1);
        assertThat(countVerificationRows("req_same", VerificationPurpose.LOGIN)).isOne();
    }

    @Test
    void createsNewVerificationForDifferentIdempotencyKey() {
        AtomicInteger creatorCalls = new AtomicInteger();

        Verification first = idempotencyService.getOrCreate(
                "req_different_key",
                VerificationPurpose.SIGN_UP,
                "idem-1",
                () -> requested("verif_diff_1", "req_different_key", VerificationPurpose.SIGN_UP, "idem-1", creatorCalls)
        );

        Verification second = idempotencyService.getOrCreate(
                "req_different_key",
                VerificationPurpose.SIGN_UP,
                "idem-2",
                () -> requested("verif_diff_2", "req_different_key", VerificationPurpose.SIGN_UP, "idem-2", creatorCalls)
        );

        assertThat(second.getId()).isNotEqualTo(first.getId());
        assertThat(second.getVerificationId()).isNotEqualTo(first.getVerificationId());
        assertThat(creatorCalls).hasValue(2);
        assertThat(countVerificationRows("req_different_key", VerificationPurpose.SIGN_UP)).isEqualTo(2);
    }

    private static Verification requested(
            String verificationId,
            String requestId,
            VerificationPurpose purpose,
            String idempotencyKey,
            AtomicInteger creatorCalls
    ) {
        creatorCalls.incrementAndGet();
        return Verification.requested(
                verificationId,
                requestId,
                purpose,
                idempotencyKey,
                LocalDateTime.of(2026, 4, 26, 15, 30).plusSeconds(creatorCalls.get())
        );
    }

    private int countVerificationRows(String requestId, VerificationPurpose purpose) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from verification_request
                        where request_id = ?
                          and purpose = ?
                        """,
                Integer.class,
                requestId,
                purpose.name()
        );
        return count == null ? 0 : count;
    }
}
