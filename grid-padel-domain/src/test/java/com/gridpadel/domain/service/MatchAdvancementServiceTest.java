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
        boolean hasMoreMatches = t.allMatches()
                .filter(m -> !m.isPlayed())
                .exists(m -> pairIsInMatch(m, consolationLoser));
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

    @Test
    void shouldRemoveOldLoserFromConsolationWhenOverwritingResult() {
        Tournament t = tournamentWith4Pairs();
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        Pair originalPair1 = r1m0.pair1();
        Pair originalPair2 = r1m0.pair2();

        // Record result: pair1 wins → pair2 goes to consolation
        advancementService.processMatchResult(t, r1m0.id(), pair1WinsResult());

        Match consolationM0 = t.consolationBracket().rounds().get(0).matchAt(0);
        assertThat(pairIsInMatch(consolationM0, originalPair2))
                .as("Loser should be in consolation after first result")
                .isTrue();

        // Overwrite result directly (without manual clear): pair2 wins now
        advancementService.processMatchResult(t, r1m0.id(), pair2WinsResult());

        // Old loser (pair2) should NOT be in consolation anymore — they won
        assertThat(pairIsInMatch(consolationM0, originalPair2))
                .as("Previous loser should be removed from consolation when result is overwritten")
                .isFalse();
        // New loser (pair1) should be in consolation
        assertThat(pairIsInMatch(consolationM0, originalPair1))
                .as("New loser should be routed to consolation")
                .isTrue();

        // Winner should be advanced correctly
        Match r2m0 = t.mainBracket().rounds().get(1).matchAt(0);
        assertThat(r2m0.pair1()).isEqualTo(originalPair2);
    }

    // --- BYE consolation routing ---

    private Tournament tournamentWith6PairsAndByes() {
        Tournament t = Tournament.create("BYE Test");
        Pair s1 = Pair.create(PlayerName.of("S1a"), PlayerName.of("S1b"));
        s1.assignSeed(1);
        t.addPair(s1);
        Pair s2 = Pair.create(PlayerName.of("S2a"), PlayerName.of("S2b"));
        s2.assignSeed(2);
        t.addPair(s2);
        Pair s3 = Pair.create(PlayerName.of("S3a"), PlayerName.of("S3b"));
        s3.assignSeed(3);
        t.addPair(s3);
        t.addPair(Pair.create(PlayerName.of("U1a"), PlayerName.of("U1b")));
        t.addPair(Pair.create(PlayerName.of("U2a"), PlayerName.of("U2b")));
        t.addPair(Pair.create(PlayerName.of("U3a"), PlayerName.of("U3b")));
        bracketService.generateMainBracket(t);
        return t;
    }

    @Test
    void shouldRouteByeAdvancedLoserToConsolation() {
        Tournament t = tournamentWith6PairsAndByes();

        // Find BYE matches in R1
        Round r1 = t.mainBracket().rounds().get(0);
        Match byeMatch = r1.matches()
                .filter(Match::isByeMatch)
                .head();

        Pair byeAdvancedPair = byeMatch.pair1().isBye() ? byeMatch.pair2() : byeMatch.pair1();

        // Find the R2 match that this pair advanced to
        Round r2 = t.mainBracket().rounds().get(1);
        Match r2Match = r2.matches()
                .filter(m -> (m.pair1() != null && m.pair1().equals(byeAdvancedPair)) ||
                             (m.pair2() != null && m.pair2().equals(byeAdvancedPair)))
                .head();

        // Play non-BYE R1 matches first to fill R2
        r1.matches()
                .filter(m -> !m.isByeMatch())
                .forEach(m -> advancementService.processMatchResult(t, m.id(), pair1WinsResult()));

        // Now play R2 match — the bye-advanced pair loses
        MatchResult losingResult = byeAdvancedPair.equals(r2Match.pair1())
                ? pair2WinsResult()
                : pair1WinsResult();
        advancementService.processMatchResult(t, r2Match.id(), losingResult);

        // The bye-advanced pair should now be in consolation R1
        Round cr1 = t.consolationBracket().rounds().get(0);
        boolean isInConsolation = cr1.matches()
                .exists(m -> pairIsInMatch(m, byeAdvancedPair));
        assertThat(isInConsolation)
                .as("BYE-advanced pair that lost in R2 should be routed to consolation")
                .isTrue();
    }

    @Test
    void shouldClearByeAdvancedLoserFromConsolation() {
        Tournament t = tournamentWith6PairsAndByes();

        Round r1 = t.mainBracket().rounds().get(0);
        Match byeMatch = r1.matches()
                .filter(Match::isByeMatch)
                .head();

        Pair byeAdvancedPair = byeMatch.pair1().isBye() ? byeMatch.pair2() : byeMatch.pair1();

        Round r2 = t.mainBracket().rounds().get(1);

        // Play non-BYE R1 matches
        r1.matches()
                .filter(m -> !m.isByeMatch())
                .forEach(m -> advancementService.processMatchResult(t, m.id(), pair1WinsResult()));

        Match r2Match = r2.matches()
                .filter(m -> (m.pair1() != null && m.pair1().equals(byeAdvancedPair)) ||
                             (m.pair2() != null && m.pair2().equals(byeAdvancedPair)))
                .head();

        MatchResult losingResult = byeAdvancedPair.equals(r2Match.pair1())
                ? pair2WinsResult()
                : pair1WinsResult();
        advancementService.processMatchResult(t, r2Match.id(), losingResult);

        // Verify in consolation
        Round cr1 = t.consolationBracket().rounds().get(0);
        assertThat(cr1.matches().exists(m -> pairIsInMatch(m, byeAdvancedPair))).isTrue();

        // Clear the result
        advancementService.clearMatchResult(t, r2Match.id());

        // Should be removed from consolation
        boolean stillInConsolation = cr1.matches()
                .exists(m -> pairIsInMatch(m, byeAdvancedPair));
        assertThat(stillInConsolation)
                .as("Cleared R2 result should remove BYE-advanced pair from consolation")
                .isFalse();
    }

    // --- Helpers ---

    private boolean pairIsInMatch(Match match, Pair pair) {
        return (match.pair1() != null && match.pair1().equals(pair)) ||
               (match.pair2() != null && match.pair2().equals(pair));
    }

    // --- Walkover (W.O.) tests ---

    @Test
    void shouldAdvanceWinnerOnWalkover() {
        Tournament t = tournamentWith4Pairs();
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        Pair expectedWinner = r1m0.pair1();

        advancementService.processMatchResult(t, r1m0.id(), MatchResult.walkover(2));

        Match r2m0 = t.mainBracket().rounds().get(1).matchAt(0);
        assertThat(r2m0.pair1()).isEqualTo(expectedWinner);
    }

    @Test
    void shouldRouteWalkoverLoserToConsolationInR1() {
        Tournament t = tournamentWith4Pairs();
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        Pair woLoser = r1m0.pair2();

        advancementService.processMatchResult(t, r1m0.id(), MatchResult.walkover(2));

        Match consolationM0 = t.consolationBracket().rounds().get(0).matchAt(0);
        assertThat(consolationM0.pair1())
                .as("W.O. loser goes to consolation so their opponent gets guaranteed 2 matches")
                .isEqualTo(woLoser);
    }

    @Test
    void shouldRouteWalkoverWinnerToConsolationWhenTheyLoseInR2() {
        Tournament t = tournamentWith4Pairs();
        Round r1 = t.mainBracket().rounds().get(0);

        // R1 match 0: pair1 wins via W.O.
        Match r1m0 = r1.matchAt(0);
        Pair woWinner = r1m0.pair1();
        advancementService.processMatchResult(t, r1m0.id(), MatchResult.walkover(2));

        // R1 match 1: pair1 wins normally
        Match r1m1 = r1.matchAt(1);
        advancementService.processMatchResult(t, r1m1.id(), pair1WinsResult());

        // R2: W.O. winner loses
        Match r2m0 = t.mainBracket().rounds().get(1).matchAt(0);
        advancementService.processMatchResult(t, r2m0.id(), pair2WinsResult());

        // W.O. winner should be routed to consolation (W.O. doesn't count as match played)
        Match consolationM0 = t.consolationBracket().rounds().get(0).matchAt(0);
        assertThat(pairIsInMatch(consolationM0, woWinner))
                .as("W.O. winner who loses in R2 should go to consolation")
                .isTrue();
    }

    @Test
    void shouldClearWalkoverConsolationRoutingOnResultClear() {
        Tournament t = tournamentWith4Pairs();
        Round r1 = t.mainBracket().rounds().get(0);

        Match r1m0 = r1.matchAt(0);
        Pair woWinner = r1m0.pair1();
        advancementService.processMatchResult(t, r1m0.id(), MatchResult.walkover(2));

        Match r1m1 = r1.matchAt(1);
        advancementService.processMatchResult(t, r1m1.id(), pair1WinsResult());

        Match r2m0 = t.mainBracket().rounds().get(1).matchAt(0);
        advancementService.processMatchResult(t, r2m0.id(), pair2WinsResult());

        // Clear R2 result
        advancementService.clearMatchResult(t, r2m0.id());

        Match consolationM0 = t.consolationBracket().rounds().get(0).matchAt(0);
        assertThat(pairIsInMatch(consolationM0, woWinner))
                .as("Consolation slot should be cleared when R2 result is cleared")
                .isFalse();
    }

    @Test
    void shouldMarkMatchAsWalkover() {
        Tournament t = tournamentWith4Pairs();
        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);

        advancementService.processMatchResult(t, r1m0.id(), MatchResult.walkover(1));

        assertThat(r1m0.isWalkover()).isTrue();
        assertThat(r1m0.isPlayed()).isTrue();
        assertThat(r1m0.winner().get()).isEqualTo(r1m0.pair2());
    }
}
