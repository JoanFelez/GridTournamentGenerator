package com.gridpadel.domain.service;

import com.gridpadel.domain.model.*;
import com.gridpadel.domain.model.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MatchAdvancementServiceTest {

    private BracketGenerationService bracketService;
    private MatchAdvancementService advancementService;

    @BeforeEach
    void setUp() {
        bracketService = new BracketGenerationService();
        advancementService = new MatchAdvancementService();
    }

    private Tournament tournamentWith4Pairs() {
        Tournament t = Tournament.create("Test");
        for (int i = 1; i <= 4; i++) {
            t.addPair(Pair.create(PlayerName.of("P" + i + "a"), PlayerName.of("P" + i + "b")));
        }
        bracketService.generateMainBracket(t);
        return t;
    }

    private Tournament tournamentWith8Pairs() {
        Tournament t = Tournament.create("Test");
        for (int i = 1; i <= 8; i++) {
            t.addPair(Pair.create(PlayerName.of("P" + i + "a"), PlayerName.of("P" + i + "b")));
        }
        bracketService.generateMainBracket(t);
        return t;
    }

    private MatchResult pair1WinsResult() {
        return MatchResult.of(SetResult.of(6, 3), SetResult.of(6, 4));
    }

    private MatchResult pair2WinsResult() {
        return MatchResult.of(SetResult.of(3, 6), SetResult.of(4, 6));
    }

    // --- Winner advancement in main bracket ---

    @Test
    void shouldAdvanceWinnerToNextRound() {
        Tournament t = tournamentWith4Pairs();
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        Pair expectedWinner = r1m0.pair1();

        advancementService.processMatchResult(t, r1m0.id(), pair1WinsResult());

        Match r2m0 = t.mainBracket().rounds().get(1).matchAt(0);
        assertThat(r2m0.pair1()).isEqualTo(expectedWinner);
    }

    @Test
    void shouldAdvanceToCorrectSlotBasedOnPosition() {
        Tournament t = tournamentWith4Pairs();
        Match r1m1 = t.mainBracket().rounds().get(0).matchAt(1);
        Pair expectedWinner = r1m1.pair2();

        advancementService.processMatchResult(t, r1m1.id(), pair2WinsResult());

        Match r2m0 = t.mainBracket().rounds().get(1).matchAt(0);
        assertThat(r2m0.pair2()).isEqualTo(expectedWinner);
    }

    @Test
    void shouldRecordResultOnMatch() {
        Tournament t = tournamentWith4Pairs();
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);

        advancementService.processMatchResult(t, r1m0.id(), pair1WinsResult());

        assertThat(r1m0.isPlayed()).isTrue();
        assertThat(r1m0.result().isDefined()).isTrue();
    }

    // --- Consolation bracket routing ---

    @Test
    void shouldRouteR1LoserToConsolationBracket() {
        Tournament t = tournamentWith4Pairs();
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        Pair expectedLoser = r1m0.pair2();

        advancementService.processMatchResult(t, r1m0.id(), pair1WinsResult());

        // Consolation bracket should now have a round with the loser
        assertThat(t.consolationBracket().rounds()).isNotEmpty();
        Round cr1 = t.consolationBracket().rounds().get(0);
        Match consolationMatch = cr1.matchAt(0);

        boolean loserIsInConsolation =
                (consolationMatch.pair1() != null && consolationMatch.pair1().equals(expectedLoser)) ||
                (consolationMatch.pair2() != null && consolationMatch.pair2().equals(expectedLoser));
        assertThat(loserIsInConsolation).isTrue();
    }

    @Test
    void shouldCreateConsolationMatchWhenBothR1MatchesPlayed() {
        Tournament t = tournamentWith4Pairs();
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        Match r1m1 = t.mainBracket().rounds().get(0).matchAt(1);

        advancementService.processMatchResult(t, r1m0.id(), pair1WinsResult());
        advancementService.processMatchResult(t, r1m1.id(), pair1WinsResult());

        // Consolation R1 should have a complete match (2 losers paired)
        Round cr1 = t.consolationBracket().rounds().get(0);
        Match consolationMatch = cr1.matchAt(0);
        assertThat(consolationMatch.pair1()).isNotNull();
        assertThat(consolationMatch.pair2()).isNotNull();
    }

    // --- Second loss elimination ---

    @Test
    void shouldEliminatePairAfterSecondLoss() {
        Tournament t = tournamentWith4Pairs();
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        Match r1m1 = t.mainBracket().rounds().get(0).matchAt(1);
        Pair loser1 = r1m0.pair2(); // will lose R1
        Pair loser2 = r1m1.pair2(); // will lose R1

        // Both lose R1 → go to consolation
        advancementService.processMatchResult(t, r1m0.id(), pair1WinsResult());
        advancementService.processMatchResult(t, r1m1.id(), pair1WinsResult());

        // In consolation, one of them loses again → eliminated
        Round cr1 = t.consolationBracket().rounds().get(0);
        Match consolationMatch = cr1.matchAt(0);

        advancementService.processMatchResult(t, consolationMatch.id(), pair1WinsResult());

        // The loser of consolation match has lost twice → eliminated (no further advancement)
        Pair consolationLoser = consolationMatch.loser().getOrElseThrow(() ->
                new AssertionError("Expected loser"));
        // No more matches for this pair in any bracket
        boolean hasMoreMatches = t.allMatches().stream()
                .filter(m -> !m.isPlayed())
                .anyMatch(m -> pairIsInMatch(m, consolationLoser));
        assertThat(hasMoreMatches).isFalse();
    }

    // --- Full tournament flow ---

    @Test
    void shouldCompleteFull4PairTournament() {
        Tournament t = tournamentWith4Pairs();

        // R1: both matches
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        Match r1m1 = t.mainBracket().rounds().get(0).matchAt(1);
        Pair mainWinner1 = r1m0.pair1();
        Pair mainWinner2 = r1m1.pair1();

        advancementService.processMatchResult(t, r1m0.id(), pair1WinsResult());
        advancementService.processMatchResult(t, r1m1.id(), pair1WinsResult());

        // Main Final
        Match mainFinal = t.mainBracket().rounds().get(1).matchAt(0);
        assertThat(mainFinal.pair1()).isEqualTo(mainWinner1);
        assertThat(mainFinal.pair2()).isEqualTo(mainWinner2);

        advancementService.processMatchResult(t, mainFinal.id(), pair1WinsResult());
        assertThat(mainFinal.winner().get()).isEqualTo(mainWinner1);

        // Consolation match
        Match consolation = t.consolationBracket().rounds().get(0).matchAt(0);
        assertThat(consolation.isComplete()).isTrue();
        advancementService.processMatchResult(t, consolation.id(), pair1WinsResult());
        assertThat(consolation.isPlayed()).isTrue();
    }

    // --- Error handling ---

    @Test
    void shouldRejectResultForNonExistentMatch() {
        Tournament t = tournamentWith4Pairs();
        MatchId fakeId = MatchId.generate();

        assertThatThrownBy(() -> advancementService.processMatchResult(t, fakeId, pair1WinsResult()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectResultForIncompleteMatch() {
        Tournament t = tournamentWith4Pairs();
        // R2 match has no pairs yet
        Match r2m0 = t.mainBracket().rounds().get(1).matchAt(0);

        assertThatThrownBy(() -> advancementService.processMatchResult(t, r2m0.id(), pair1WinsResult()))
                .isInstanceOf(IllegalStateException.class);
    }

    // --- Result clearing ---

    @Test
    void shouldAllowClearingAndReenteringResult() {
        Tournament t = tournamentWith4Pairs();
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        Pair originalPair1 = r1m0.pair1();
        Pair originalPair2 = r1m0.pair2();

        // Record result (pair1 wins)
        advancementService.processMatchResult(t, r1m0.id(), pair1WinsResult());
        Match r2m0 = t.mainBracket().rounds().get(1).matchAt(0);
        assertThat(r2m0.pair1()).isEqualTo(originalPair1);

        // Clear and re-enter with pair2 winning
        advancementService.clearMatchResult(t, r1m0.id());
        assertThat(r1m0.isPlayed()).isFalse();
        assertThat(r2m0.pair1()).isNull();

        advancementService.processMatchResult(t, r1m0.id(), pair2WinsResult());
        assertThat(r2m0.pair1()).isEqualTo(originalPair2);
    }

    // --- Helpers ---

    private boolean pairIsInMatch(Match match, Pair pair) {
        return (match.pair1() != null && match.pair1().equals(pair)) ||
               (match.pair2() != null && match.pair2().equals(pair));
    }
}
