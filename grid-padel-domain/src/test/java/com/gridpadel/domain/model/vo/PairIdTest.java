package com.gridpadel.domain.model.vo;

import com.gridpadel.domain.exception.DomainException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PairIdTest {

    @Test
    void shouldCreatePairIdWithGeneratedUUID() {
        PairId id = PairId.generate();
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void shouldCreatePairIdFromExistingValue() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        PairId id = PairId.of(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> PairId.of(null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> PairId.of("  "))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        PairId id1 = PairId.of("550e8400-e29b-41d4-a716-446655440000");
        PairId id2 = PairId.of("550e8400-e29b-41d4-a716-446655440000");
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentValue() {
        PairId id1 = PairId.generate();
        PairId id2 = PairId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }
}
