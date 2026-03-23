package com.gridpadel.domain.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BracketTypeTest {

    @Test
    void shouldHaveMainType() {
        assertThat(BracketType.MAIN).isNotNull();
    }

    @Test
    void shouldHaveConsolationType() {
        assertThat(BracketType.CONSOLATION).isNotNull();
    }

    @Test
    void shouldHaveExactlyTwoValues() {
        assertThat(BracketType.values()).hasSize(2);
    }
}
