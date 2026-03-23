package com.gridpadel.domain.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PlayerNameTest {

    @Test
    void shouldCreatePlayerName() {
        PlayerName name = PlayerName.of("Carlos Pérez");
        assertThat(name.value()).isEqualTo("Carlos Pérez");
    }

    @Test
    void shouldTrimWhitespace() {
        PlayerName name = PlayerName.of("  Carlos Pérez  ");
        assertThat(name.value()).isEqualTo("Carlos Pérez");
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> PlayerName.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> PlayerName.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNameExceeding100Characters() {
        String longName = "A".repeat(101);
        assertThatThrownBy(() -> PlayerName.of(longName))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAcceptNameOf100Characters() {
        String name = "A".repeat(100);
        assertThatNoException().isThrownBy(() -> PlayerName.of(name));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        PlayerName name1 = PlayerName.of("Carlos");
        PlayerName name2 = PlayerName.of("Carlos");
        assertThat(name1).isEqualTo(name2);
        assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
    }

    @Test
    void shouldReturnValueAsToString() {
        PlayerName name = PlayerName.of("Carlos");
        assertThat(name.toString()).contains("Carlos");
    }
}
