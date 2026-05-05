package com.verifyhub.verification.adapter.out.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.junit.jupiter.api.Test;

class ProviderRoutingPolicyEntityMappingTest {

    @Test
    void mapsProviderRoutingPolicyColumns() throws Exception {
        Class<ProviderRoutingPolicyEntity> type = ProviderRoutingPolicyEntity.class;

        assertThat(type.isAnnotationPresent(Entity.class)).isTrue();
        assertThat(type.getAnnotation(Table.class).name()).isEqualTo("provider_routing_policy");

        List<String> fieldNames = Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());

        assertThat(fieldNames).contains(
                "id",
                "provider",
                "weight",
                "enabled",
                "version",
                "createdAt",
                "updatedAt"
        );

        assertThat(type.getDeclaredField("id").isAnnotationPresent(Id.class)).isTrue();
        assertThat(type.getDeclaredField("version").getAnnotation(Column.class).nullable()).isFalse();
        assertThat(type.getDeclaredField("provider").getAnnotation(Column.class).nullable()).isFalse();
        assertThat(type.getDeclaredField("weight").getAnnotation(Column.class).nullable()).isFalse();
        assertThat(type.getDeclaredField("enabled").getAnnotation(Column.class).nullable()).isFalse();
    }
}
