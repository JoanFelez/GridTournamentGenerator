package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RoundTest {

    private Pair pair(String p1, String p2) {
        return Pair.create(PlayerName.of(p1), PlayerName.of(p2));
    }

    @Test
    void shouldCreateRoundWithMatches() {
        Match m1 = Match.create(pair("A", "B"), pair("C", "D"), 1, 0, BracketType.MAIN);
        Match m2 = Match.create(pair("E", "F"), pair("G", "H"), 1, 1, BracketType.MAIN);
        Round round = Round.of(1, List.of(m1, m2), BracketType.MAIN);

        assertThat(round.roundNumber()).isEqualTo(1);
        assertThat(round.matches()).hasSize(2);
        assertThat(round.bracketType()).isEqualTo(BracketType.MAIN);
    }

    @Test
    void shouldRejectEmptyMatchList() {
        assertThatThrownBy(() -> Round.of(1, List.of(), BracketType.MAIN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullMatchList() {
        assertThatThrownBy(() -> Round.of(1, null, BracketType.MAIN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnMatchByPosition() {
        Match m = Match.create(pair("A", "B"), pair("C", "D"), 1, 0, BracketType.MAIN);
        Round round = Round.of(1, List.of(m), BracketType.MAIN);
        assertThat(round.matchAt(0)).isEqualTo(m);
    }

    @Test
    void shouldCheckIfAllMatchesPlayed() {
        Match m = Match.create(pair("A", "B"), pair("C", "D"), 1, 0, BracketType.MAIN);
        Round round = Round.of(1, List.of(m), BracketType.MAIN);
        assertThat(round.isComplete()).isFalse();

        m.recordResult(MatchResult.of(SetResult.of(6, 3), SetResult.of(6, 4)));
        assertThat(round.isComplete()).isTrue();
    }

    @Test
    void shouldReturnMatchCount() {
        Match m1 = Match.create(pair("A", "B"), pair("C", "D"), 1, 0, BracketType.MAIN);
        Match m2 = Match.create(pair("E", "F"), pair("G", "H"), 1, 1, BracketType.MAIN);
        Round round = Round.of(1, List.of(m1, m2), BracketType.MAIN);
        assertThat(round.matchCount()).isEqualTo(2);
    }
}
