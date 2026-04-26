package com.verifyhub.verification.adapter.out.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.domain.VerificationStatus;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import org.junit.jupiter.api.Test;

class VerificationEntityMappingTest {

    @Test
    void mapsVerificationRequestColumns() throws Exception {
        Class<VerificationEntity> type = VerificationEntity.class;

        assertThat(type.isAnnotationPresent(javax.persistence.Entity.class)).isTrue();
        assertThat(type.getAnnotation(Table.class).name()).isEqualTo("verification_request");

        List<String> fieldNames = Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());

        assertThat(fieldNames).contains(
                "id",
                "verificationId",
                "userId",
                "purpose",
                "idempotencyKey",
                "provider",
                "status",
                "providerTransactionId",
                "providerRequestNo",
                "webTransactionId",
                "routingPolicyVersion",
                "requestedAt",
                "routedAt",
                "providerCalledAt",
                "completedAt",
                "version",
                "createdAt",
                "updatedAt"
        );
        assertThat(fieldNames).doesNotContain("authUrl", "name", "phoneNumber", "birthDate");

        assertThat(type.getDeclaredField("id").isAnnotationPresent(Id.class)).isTrue();
        assertThat(type.getDeclaredField("version").isAnnotationPresent(Version.class)).isTrue();
        assertThat(type.getDeclaredField("purpose").isAnnotationPresent(Enumerated.class)).isTrue();
        assertThat(type.getDeclaredField("provider").isAnnotationPresent(Enumerated.class)).isTrue();
        assertThat(type.getDeclaredField("status").isAnnotationPresent(Enumerated.class)).isTrue();
        assertThat(type.getDeclaredField("verificationId").getAnnotation(Column.class).name()).isEqualTo("verification_id");
        assertThat(type.getDeclaredField("providerRequestNo").getAnnotation(Column.class).name()).isEqualTo("provider_request_no");
        assertThat(type.getDeclaredField("webTransactionId").getAnnotation(Column.class).name()).isEqualTo("web_transaction_id");
        assertThat(type.getDeclaredField("purpose").getType()).isEqualTo(VerificationPurpose.class);
        assertThat(type.getDeclaredField("provider").getType()).isEqualTo(ProviderType.class);
        assertThat(type.getDeclaredField("status").getType()).isEqualTo(VerificationStatus.class);
    }
}
