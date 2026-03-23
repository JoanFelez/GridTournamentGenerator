package com.gridpadel.domain.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LocationTest {

    @Test
    void shouldCreateLocation() {
        Location location = Location.of("Pista Central");
        assertThat(location.value()).isEqualTo("Pista Central");
    }

    @Test
    void shouldTrimWhitespace() {
        Location location = Location.of("  Pista Central  ");
        assertThat(location.value()).isEqualTo("Pista Central");
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> Location.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> Location.of("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        Location loc1 = Location.of("Pista 1");
        Location loc2 = Location.of("Pista 1");
        assertThat(loc1).isEqualTo(loc2);
        assertThat(loc1.hashCode()).isEqualTo(loc2.hashCode());
    }
}
