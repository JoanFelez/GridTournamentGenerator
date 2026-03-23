package com.gridpadel.domain.model.vo;

import com.gridpadel.domain.exception.DomainException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MatchIdTest {

    @Test
    void shouldCreateMatchIdWithGeneratedUUID() {
        MatchId id = MatchId.generate();
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void shouldCreateMatchIdFromExistingValue() {
        String uuid = "660e8400-e29b-41d4-a716-446655440000";
        MatchId id = MatchId.of(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> MatchId.of(null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> MatchId.of(""))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        MatchId id1 = MatchId.of("660e8400-e29b-41d4-a716-446655440000");
        MatchId id2 = MatchId.of("660e8400-e29b-41d4-a716-446655440000");
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
