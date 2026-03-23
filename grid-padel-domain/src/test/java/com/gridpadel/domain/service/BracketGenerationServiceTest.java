package com.gridpadel.domain.service;

import com.gridpadel.domain.model.*;
import com.gridpadel.domain.model.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class BracketGenerationServiceTest {

    private BracketGenerationService service;

    @BeforeEach
    void setUp() {
        service = new BracketGenerationService();
    }

    private Tournament tournamentWithPairs(int count) {
        Tournament t = Tournament.create("Test Tournament");
        for (int i = 1; i <= count; i++) {
            t.addPair(Pair.create(PlayerName.of("P" + i + "a"), PlayerName.of("P" + i + "b")));
        }
        return t;
    }

    private Tournament tournamentWithSeededPairs(int total, int seeded) {
        Tournament t = Tournament.create("Seeded Tournament");
        for (int i = 1; i <= total; i++) {
            Pair p = Pair.create(PlayerName.of("P" + i + "a"), PlayerName.of("P" + i + "b"));
            if (i <= seeded) {
                p.assignSeed(i);
            }
            t.addPair(p);
        }
        return t;
    }

    // --- Basic bracket generation ---

    @Test
    void shouldRejectTournamentWithNoPairs() {
        Tournament t = Tournament.create("Empty");
        assertThatThrownBy(() -> service.generateMainBracket(t))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectTournamentWithOnePair() {
        Tournament t = tournamentWithPairs(1);
        assertThatThrownBy(() -> service.generateMainBracket(t))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldGenerateBracketWith2Pairs() {
        Tournament t = tournamentWithPairs(2);
        service.generateMainBracket(t);

        Bracket main = t.mainBracket();
        assertThat(main.rounds()).hasSize(1);
        assertThat(main.rounds().get(0).matchCount()).isEqualTo(1);

        Match match = main.rounds().get(0).matches().get(0);
        assertThat(match.pair1()).isNotNull();
        assertThat(match.pair2()).isNotNull();
        assertThat(match.isByeMatch()).isFalse();
    }

    @Test
    void shouldGenerateBracketWith4Pairs() {
        Tournament t = tournamentWithPairs(4);
        service.generateMainBracket(t);

        Bracket main = t.mainBracket();
        assertThat(main.rounds()).hasSize(2);
        assertThat(main.rounds().get(0).matchCount()).isEqualTo(2);
        assertThat(main.rounds().get(1).matchCount()).isEqualTo(1);

        main.rounds().get(0).matches().forEach(m -> {
            assertThat(m.pair1()).isNotNull();
            assertThat(m.pair2()).isNotNull();
            assertThat(m.isByeMatch()).isFalse();
        });

        Match finalMatch = main.rounds().get(1).matches().get(0);
        assertThat(finalMatch.pair1()).isNull();
        assertThat(finalMatch.pair2()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 4, 8, 16, 32})
    void shouldGenerateBracketWithPowerOf2Pairs(int count) {
        Tournament t = tournamentWithPairs(count);
        service.generateMainBracket(t);

        Bracket main = t.mainBracket();
        int expectedRounds = (int) (Math.log(count) / Math.log(2));
        assertThat(main.rounds()).hasSize(expectedRounds);

        Round r1 = main.rounds().get(0);
        assertThat(r1.matchCount()).isEqualTo(count / 2);
        r1.matches().forEach(m -> assertThat(m.isByeMatch()).isFalse());
    }

    // --- BYE handling ---

    @Test
    void shouldGenerateBracketWith3PairsUsing1Bye() {
        Tournament t = tournamentWithPairs(3);
        service.generateMainBracket(t);

        Bracket main = t.mainBracket();
        assertThat(main.rounds()).hasSize(2);
        Round r1 = main.rounds().get(0);
        assertThat(r1.matchCount()).isEqualTo(2);

        int byeMatches = r1.matches().count(Match::isByeMatch);
        assertThat(byeMatches).isEqualTo(1);
    }

    @Test
    void shouldAutoResolvByeMatches() {
        Tournament t = tournamentWithPairs(3);
        service.generateMainBracket(t);

        Round r1 = t.mainBracket().rounds().get(0);
        Match byeMatch = r1.matches()
                .find(Match::isByeMatch)
                .getOrElseThrow(() -> new AssertionError("No BYE match found"));

        assertThat(byeMatch.isPlayed()).isFalse();
        Match r2Match = t.mainBracket().rounds().get(1).matches().get(0);
        boolean advancedToR2 = r2Match.pair1() != null || r2Match.pair2() != null;
        assertThat(advancedToR2).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 5, 6, 7, 9, 10, 15, 17, 24, 31})
    void shouldCalculateCorrectNumberOfByes(int pairCount) {
        Tournament t = tournamentWithPairs(pairCount);
        service.generateMainBracket(t);

        int drawSize = Integer.highestOneBit(pairCount - 1) << 1;
        int expectedByes = drawSize - pairCount;

        Round r1 = t.mainBracket().rounds().get(0);
        int byeMatches = r1.matches().count(Match::isByeMatch);
        assertThat(byeMatches).isEqualTo(expectedByes);
    }

    // --- Seeding ---

    @Test
    void shouldPlaceSeed1AndSeed2InOppositeHalves() {
        Tournament t = tournamentWithSeededPairs(8, 2);
        service.generateMainBracket(t);

        Round r1 = t.mainBracket().rounds().get(0);
        Match seed1Match = findMatchWithSeed(r1, 1);
        Match seed2Match = findMatchWithSeed(r1, 2);

        assertThat(seed1Match.position()).isNotEqualTo(seed2Match.position());
        int halfSize = r1.matchCount() / 2;
        boolean seed1InFirstHalf = seed1Match.position() < halfSize;
        boolean seed2InFirstHalf = seed2Match.position() < halfSize;
        assertThat(seed1InFirstHalf).isNotEqualTo(seed2InFirstHalf);
    }

    @Test
    void shouldGiveBYEsToTopSeedsWhenNotPowerOf2() {
        Tournament t = tournamentWithSeededPairs(6, 2);
        service.generateMainBracket(t);

        Round r1 = t.mainBracket().rounds().get(0);
        io.vavr.collection.List<Match> byeMatches = r1.matches().filter(Match::isByeMatch);

        assertThat(byeMatches).hasSize(2);
        byeMatches.forEach(m -> {
            Pair nonBye = m.pair1().isBye() ? m.pair2() : m.pair1();
            assertThat(nonBye.isSeeded()).isTrue();
        });
    }

    @Test
    void shouldPlaceSeeds3And4InCorrectQuarters() {
        Tournament t = tournamentWithSeededPairs(16, 4);
        service.generateMainBracket(t);

        Round r1 = t.mainBracket().rounds().get(0);
        Match seed1Match = findMatchWithSeed(r1, 1);
        Match seed2Match = findMatchWithSeed(r1, 2);
        Match seed3Match = findMatchWithSeed(r1, 3);
        Match seed4Match = findMatchWithSeed(r1, 4);

        int quarterSize = r1.matchCount() / 4;
        int q1 = seed1Match.position() / quarterSize;
        int q2 = seed2Match.position() / quarterSize;
        int q3 = seed3Match.position() / quarterSize;
        int q4 = seed4Match.position() / quarterSize;

        assertThat(java.util.List.of(q1, q2, q3, q4)).containsExactlyInAnyOrder(0, 1, 2, 3);
    }

    // --- Round structure ---

    @Test
    void shouldCreateCorrectRoundStructure() {
        Tournament t = tournamentWithPairs(8);
        service.generateMainBracket(t);

        Bracket main = t.mainBracket();
        assertThat(main.rounds()).hasSize(3);

        assertThat(main.rounds().get(0).matchCount()).isEqualTo(4);
        assertThat(main.rounds().get(1).matchCount()).isEqualTo(2);
        assertThat(main.rounds().get(2).matchCount()).isEqualTo(1);
    }

    @Test
    void shouldSetCorrectRoundNumbers() {
        Tournament t = tournamentWithPairs(8);
        service.generateMainBracket(t);

        for (int i = 0; i < t.mainBracket().rounds().size(); i++) {
            assertThat(t.mainBracket().rounds().get(i).roundNumber()).isEqualTo(i + 1);
        }
    }

    @Test
    void shouldSetCorrectPositionsWithinRounds() {
        Tournament t = tournamentWithPairs(8);
        service.generateMainBracket(t);

        Round r1 = t.mainBracket().rounds().get(0);
        for (int i = 0; i < r1.matchCount(); i++) {
            assertThat(r1.matches().get(i).position()).isEqualTo(i);
        }
    }

    @Test
    void shouldSetBracketTypeToMain() {
        Tournament t = tournamentWithPairs(4);
        service.generateMainBracket(t);

        t.mainBracket().allMatches().forEach(m ->
                assertThat(m.bracketType()).isEqualTo(BracketType.MAIN));
    }

    @Test
    void shouldNotGenerateConsolationBracketYet() {
        Tournament t = tournamentWithPairs(8);
        service.generateMainBracket(t);

        assertThat(t.consolationBracket().rounds()).isEmpty();
    }

    @Test
    void shouldRejectRegeneratingBracket() {
        Tournament t = tournamentWithPairs(4);
        service.generateMainBracket(t);
        assertThatThrownBy(() -> service.generateMainBracket(t))
                .isInstanceOf(IllegalStateException.class);
    }

    // --- Edge cases ---

    @Test
    void shouldGenerateWith32Pairs() {
        Tournament t = tournamentWithPairs(32);
        service.generateMainBracket(t);

        Bracket main = t.mainBracket();
        assertThat(main.rounds()).hasSize(5);
        assertThat(main.rounds().get(0).matchCount()).isEqualTo(16);
    }

    // --- Helper ---

    private Match findMatchWithSeed(Round round, int seed) {
        return round.matches()
                .find(m -> hasSeed(m.pair1(), seed) || hasSeed(m.pair2(), seed))
                .getOrElseThrow(() -> new AssertionError("No match with seed " + seed));
    }

    private boolean hasSeed(Pair pair, int seed) {
        return pair != null && !pair.isBye() && pair.isSeeded() && pair.seed().getOrElse(-1) == seed;
    }
}
