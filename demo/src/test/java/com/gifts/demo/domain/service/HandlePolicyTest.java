package com.gifts.demo.domain.service;

import com.gifts.demo.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandlePolicyTest {

    @Test
    void shouldNormalizeOuterSpacesAndCase() {
        assertThat(HandlePolicy.normalize("  Ana.Garcia  "))
                .isEqualTo("ana.garcia");
    }

    @Test
    void shouldAcceptMinimumAndMaximumLengths() {
        assertThat(HandlePolicy.normalize("abc")).isEqualTo("abc");
        assertThat(HandlePolicy.normalize("a".repeat(30))).hasSize(30);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ab", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", "   ", "ana garcia", "ana-á",
            "ana@garcia", "ana😀"
    })
    void shouldRejectInvalidHandles(String handle) {
        assertThatThrownBy(() -> HandlePolicy.normalize(handle))
                .isInstanceOf(ValidationException.class);
    }

    @ParameterizedTest
    @NullSource
    void shouldRejectMissingHandle(String handle) {
        assertThatThrownBy(() -> HandlePolicy.normalize(handle))
                .isInstanceOf(ValidationException.class);
    }
}
