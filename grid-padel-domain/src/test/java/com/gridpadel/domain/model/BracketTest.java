package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BracketTest {

    private Pair pair(String p1, String p2) {
        return Pair.create(PlayerName.of(p1), PlayerName.of(p2));
    }

    @Test
    void shouldCreateBracketWithType() {
        Bracket bracket = Bracket.create(BracketType.MAIN);
        assertThat(bracket.type()).isEqualTo(BracketType.MAIN);
        assertThat(bracket.rounds()).isEmpty();
    }

    @Test
    void shouldAddRound() {
        Bracket bracket = Bracket.create(BracketType.MAIN);
        Match m = Match.create(pair("A", "B"), pair("C", "D"), 1, 0, BracketType.MAIN);
        Round round = Round.of(1, List.of(m), BracketType.MAIN);
        bracket.addRound(round);
        assertThat(bracket.rounds()).hasSize(1);
    }

    @Test
    void shouldReturnRoundByNumber() {
        Bracket bracket = Bracket.create(BracketType.MAIN);
        Match m = Match.create(pair("A", "B"), pair("C", "D"), 1, 0, BracketType.MAIN);
        Round round = Round.of(1, List.of(m), BracketType.MAIN);
        bracket.addRound(round);
        assertThat(bracket.round(1).isDefined()).isTrue();
        assertThat(bracket.round(2).isEmpty()).isTrue();
    }

    @Test
    void shouldReturnTotalMatches() {
        Bracket bracket = Bracket.create(BracketType.MAIN);
        Match m1 = Match.create(pair("A", "B"), pair("C", "D"), 1, 0, BracketType.MAIN);
        Match m2 = Match.create(pair("E", "F"), pair("G", "H"), 1, 1, BracketType.MAIN);
        bracket.addRound(Round.of(1, List.of(m1, m2), BracketType.MAIN));

        Match m3 = Match.createEmpty(2, 0, BracketType.MAIN);
        bracket.addRound(Round.of(2, List.of(m3), BracketType.MAIN));

        assertThat(bracket.totalMatches()).isEqualTo(3);
    }

    @Test
    void shouldReturnAllMatches() {
        Bracket bracket = Bracket.create(BracketType.MAIN);
        Match m1 = Match.create(pair("A", "B"), pair("C", "D"), 1, 0, BracketType.MAIN);
        bracket.addRound(Round.of(1, List.of(m1), BracketType.MAIN));

        assertThat(bracket.allMatches()).hasSize(1).contains(m1);
    }
}
