package com.gridpadel.domain.model.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class SetResultTest {

    @ParameterizedTest
    @CsvSource({
            "6, 0", "6, 1", "6, 2", "6, 3", "6, 4",
            "0, 6", "1, 6", "2, 6", "3, 6", "4, 6",
            "7, 5", "5, 7",
            "7, 6", "6, 7"
    })
    void shouldAcceptValidSetScores(int pair1Games, int pair2Games) {
        SetResult set = SetResult.of(pair1Games, pair2Games);
        assertThat(set.pair1Games()).isEqualTo(pair1Games);
        assertThat(set.pair2Games()).isEqualTo(pair2Games);
    }

    @ParameterizedTest
    @CsvSource({
            "5, 4", "4, 5",
            "6, 6",
            "8, 6", "6, 8",
            "7, 4", "4, 7",
            "7, 3", "3, 7",
            "0, 0",
            "5, 5",
            "8, 7", "7, 8",
            "-1, 6", "6, -1"
    })
    void shouldRejectInvalidSetScores(int pair1Games, int pair2Games) {
        assertThatThrownBy(() -> SetResult.of(pair1Games, pair2Games))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDetermineWinnerIsPair1() {
        SetResult set = SetResult.of(6, 3);
        assertThat(set.winnerPosition()).isEqualTo(1);
    }

    @Test
    void shouldDetermineWinnerIsPair2() {
        SetResult set = SetResult.of(4, 6);
        assertThat(set.winnerPosition()).isEqualTo(2);
    }

    @Test
    void shouldDetermineWinnerInTiebreak() {
        SetResult set = SetResult.of(7, 6);
        assertThat(set.winnerPosition()).isEqualTo(1);

        SetResult set2 = SetResult.of(6, 7);
        assertThat(set2.winnerPosition()).isEqualTo(2);
    }

    @Test
    void shouldFormatToString() {
        SetResult set = SetResult.of(6, 4);
        assertThat(set.toString()).isEqualTo("6-4");
    }

    @Test
    void shouldBeEqualWhenSameScore() {
        SetResult s1 = SetResult.of(6, 3);
        SetResult s2 = SetResult.of(6, 3);
        assertThat(s1).isEqualTo(s2);
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentScore() {
        SetResult s1 = SetResult.of(6, 3);
        SetResult s2 = SetResult.of(6, 4);
        assertThat(s1).isNotEqualTo(s2);
    }
}
