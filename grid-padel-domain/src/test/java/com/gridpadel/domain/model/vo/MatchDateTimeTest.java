package com.gridpadel.domain.model.vo;

import com.gridpadel.domain.exception.DomainException;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class MatchDateTimeTest {

    @Test
    void shouldCreateMatchDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 15, 10, 30);
        MatchDateTime mdt = MatchDateTime.of(dateTime);
        assertThat(mdt.value()).isEqualTo(dateTime);
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> MatchDateTime.of(null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 15, 10, 30);
        MatchDateTime mdt1 = MatchDateTime.of(dateTime);
        MatchDateTime mdt2 = MatchDateTime.of(dateTime);
        assertThat(mdt1).isEqualTo(mdt2);
        assertThat(mdt1.hashCode()).isEqualTo(mdt2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentValue() {
        MatchDateTime mdt1 = MatchDateTime.of(LocalDateTime.of(2026, 4, 15, 10, 0));
        MatchDateTime mdt2 = MatchDateTime.of(LocalDateTime.of(2026, 4, 15, 11, 0));
        assertThat(mdt1).isNotEqualTo(mdt2);
    }
}
