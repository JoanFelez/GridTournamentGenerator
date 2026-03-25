package com.gridpadel.domain.service;

import com.gridpadel.domain.exception.InvalidOperationException;
import com.gridpadel.domain.model.*;
import com.gridpadel.domain.model.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BracketEditServiceTest {

    private BracketGenerationService bracketService;
    private BracketEditService editService;
    private MatchAdvancementService advancementService;

    @BeforeEach
    void setUp() {
        bracketService = new BracketGenerationService();
        editService = new BracketEditService();
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

    private Tournament tournamentWith6PairsAndByes() {
        Tournament t = Tournament.create("BYE Test");
        Pair s1 = Pair.create(PlayerName.of("S1a"), PlayerName.of("S1b"));
        s1.assignSeed(1);
        t.addPair(s1);
        Pair s2 = Pair.create(PlayerName.of("S2a"), PlayerName.of("S2b"));
        s2.assignSeed(2);
        t.addPair(s2);
        t.addPair(Pair.create(PlayerName.of("U1a"), PlayerName.of("U1b")));
        t.addPair(Pair.create(PlayerName.of("U2a"), PlayerName.of("U2b")));
        t.addPair(Pair.create(PlayerName.of("U3a"), PlayerName.of("U3b")));
        t.addPair(Pair.create(PlayerName.of("U4a"), PlayerName.of("U4b")));
        bracketService.generateMainBracket(t);
        return t;
    }

    // --- Basic swap ---

    @Test
    void shouldSwapTwoPairsInR1() {
        Tournament t = tournamentWith4Pairs();
        Round r1 = t.mainBracket().rounds().get(0);

        Match m0 = r1.matchAt(0);
        Match m1 = r1.matchAt(1);

        Pair pair1 = m0.pair1();
        Pair pair2 = m1.pair1();

        editService.swapPairsInDraw(t, pair1.id(), pair2.id());

        assertThat(m0.pair1()).isEqualTo(pair2);
        assertThat(m1.pair1()).isEqualTo(pair1);
    }

    @Test
    void shouldSwapPairsBetweenDifferentSlots() {
        Tournament t = tournamentWith4Pairs();
        Round r1 = t.mainBracket().rounds().get(0);

        Pair pair1FromM0 = r1.matchAt(0).pair2();
        Pair pair2FromM1 = r1.matchAt(1).pair2();

        editService.swapPairsInDraw(t, pair1FromM0.id(), pair2FromM1.id());

        assertThat(r1.matchAt(0).pair2()).isEqualTo(pair2FromM1);
        assertThat(r1.matchAt(1).pair2()).isEqualTo(pair1FromM0);
    }

    // --- Validation ---

    @Test
    void shouldRejectSwapWithSelf() {
        Tournament t = tournamentWith4Pairs();
        Pair pair = t.mainBracket().rounds().get(0).matchAt(0).pair1();

        assertThatThrownBy(() -> editService.swapPairsInDraw(t, pair.id(), pair.id()))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("itself");
    }

    @Test
    void shouldAllowSwapWithByePair() {
        Tournament t = tournamentWith6PairsAndByes();
        Round r1 = t.mainBracket().rounds().get(0);
        Round r2 = t.mainBracket().rounds().get(1);

        Match byeMatch = r1.matches().filter(Match::isByeMatch).head();
        Pair byePair = byeMatch.pair2().isBye() ? byeMatch.pair2() : byeMatch.pair1();
        Match normalMatch = r1.matches().filter(m -> !m.isByeMatch()).head();
        Pair targetPair = normalMatch.pair1();
        Pair targetPartner = normalMatch.pair2();

        // Swap targetPair with BYE → BYE moves to normalMatch, targetPair goes to byeMatch
        editService.swapPairsInDraw(t, targetPair.id(), byePair.id());

        // byeMatch should now have realPairInByeMatch + targetPair (no BYE)
        assertThat(byeMatch.isByeMatch())
                .as("Original BYE match should no longer be a BYE match")
                .isFalse();

        // normalMatch should now be a BYE match (BYE + targetPartner)
        assertThat(normalMatch.isByeMatch())
                .as("Normal match should now be a BYE match after BYE was swapped in")
                .isTrue();

        // targetPartner should be auto-advanced to R2 (paired with BYE in the new BYE match)
        boolean partnerAdvanced = r2.matches().exists(m ->
                (m.pair1() != null && m.pair1().equals(targetPartner)) ||
                (m.pair2() != null && m.pair2().equals(targetPartner)));
        assertThat(partnerAdvanced)
                .as("Partner of swapped-out pair should be auto-advanced via new BYE match")
                .isTrue();

        editService.validateDrawIntegrity(t);
    }

    @Test
    void shouldRejectSwapAfterResultsEntered() {
        Tournament t = tournamentWith4Pairs();
        Match m0 = t.mainBracket().rounds().get(0).matchAt(0);
        Pair pair = m0.pair1();

        MatchResult result = MatchResult.of(SetResult.of(6, 3), SetResult.of(6, 4));
        advancementService.processMatchResult(t, m0.id(), result);

        Pair otherPair = t.mainBracket().rounds().get(0).matchAt(1).pair1();
        assertThatThrownBy(() -> editService.swapPairsInDraw(t, pair.id(), otherPair.id()))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("results");
    }

    // --- Draw integrity after swap ---

    @Test
    void shouldMaintainDrawIntegrityAfterSwap() {
        Tournament t = tournamentWith4Pairs();
        Round r1 = t.mainBracket().rounds().get(0);

        Pair p1 = r1.matchAt(0).pair1();
        Pair p2 = r1.matchAt(1).pair2();

        editService.swapPairsInDraw(t, p1.id(), p2.id());

        // Should not throw
        editService.validateDrawIntegrity(t);
    }

    @Test
    void shouldMaintainDrawIntegrityWithByes() {
        Tournament t = tournamentWith6PairsAndByes();
        Round r1 = t.mainBracket().rounds().get(0);

        // Find two non-BYE pairs from different matches
        var nonByeMatches = r1.matches().filter(m -> !m.isByeMatch());
        if (nonByeMatches.size() >= 2) {
            Pair p1 = nonByeMatches.get(0).pair1();
            Pair p2 = nonByeMatches.get(1).pair1();

            editService.swapPairsInDraw(t, p1.id(), p2.id());
            editService.validateDrawIntegrity(t);
        }
    }

    // --- BYE re-resolution after swap ---

    @Test
    void shouldReResolveByesAfterSwapInByeMatch() {
        Tournament t = tournamentWith6PairsAndByes();
        Round r1 = t.mainBracket().rounds().get(0);
        Round r2 = t.mainBracket().rounds().get(1);

        // Find a non-BYE pair from a BYE match and a pair from a non-BYE match
        Match byeMatch = r1.matches().filter(Match::isByeMatch).head();
        Pair advancingPair = byeMatch.pair1().isBye() ? byeMatch.pair2() : byeMatch.pair1();

        Match normalMatch = r1.matches().filter(m -> !m.isByeMatch()).head();
        Pair normalPair = normalMatch.pair1();

        // Swap the non-BYE pair out of the BYE match
        editService.swapPairsInDraw(t, advancingPair.id(), normalPair.id());

        // R2 should now have normalPair advanced (not advancingPair)
        boolean normalPairAdvanced = r2.matches().exists(m ->
                (m.pair1() != null && m.pair1().equals(normalPair)) ||
                (m.pair2() != null && m.pair2().equals(normalPair)));
        assertThat(normalPairAdvanced)
                .as("After swap, the new pair in the BYE match should be auto-advanced")
                .isTrue();

        boolean oldPairStillAdvanced = r2.matches().exists(m ->
                (m.pair1() != null && m.pair1().equals(advancingPair)) ||
                (m.pair2() != null && m.pair2().equals(advancingPair)));
        assertThat(oldPairStillAdvanced)
                .as("After swap, the old pair should no longer be in R2")
                .isFalse();
    }
}
