package com.gridpadel.domain.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MatchResultTest {

    @Test
    void shouldCreateMatchResult() {
        MatchResult result = MatchResult.of(2, 1);
        assertThat(result.setsWonByPair1()).isEqualTo(2);
        assertThat(result.setsWonByPair2()).isEqualTo(1);
    }

    @Test
    void shouldDetermineWinnerIsPair1() {
        MatchResult result = MatchResult.of(2, 0);
        assertThat(result.winnerPosition()).isEqualTo(1);
        assertThat(result.loserPosition()).isEqualTo(2);
    }

    @Test
    void shouldDetermineWinnerIsPair2() {
        MatchResult result = MatchResult.of(0, 2);
        assertThat(result.winnerPosition()).isEqualTo(2);
        assertThat(result.loserPosition()).isEqualTo(1);
    }

    @Test
    void shouldRejectNegativeSets() {
        assertThatThrownBy(() -> MatchResult.of(-1, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MatchResult.of(2, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectDrawResult() {
        assertThatThrownBy(() -> MatchResult.of(1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("draw");
    }

    @Test
    void shouldRejectBothZeroSets() {
        assertThatThrownBy(() -> MatchResult.of(0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualWhenSameScore() {
        MatchResult r1 = MatchResult.of(2, 1);
        MatchResult r2 = MatchResult.of(2, 1);
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentScore() {
        MatchResult r1 = MatchResult.of(2, 1);
        MatchResult r2 = MatchResult.of(2, 0);
        assertThat(r1).isNotEqualTo(r2);
    }
}
