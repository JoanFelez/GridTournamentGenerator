package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class MatchTest {

    private Pair pair1() {
        return Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
    }

    private Pair pair2() {
        return Pair.create(PlayerName.of("Juan"), PlayerName.of("Ana"));
    }

    private MatchResult pair1Wins() {
        return MatchResult.of(SetResult.of(6, 3), SetResult.of(6, 4));
    }

    private MatchResult pair2Wins() {
        return MatchResult.of(SetResult.of(3, 6), SetResult.of(4, 6));
    }

    private MatchResult pair1WinsThreeSets() {
        return MatchResult.of(SetResult.of(6, 3), SetResult.of(4, 6), SetResult.of(6, 2));
    }

    private MatchResult pair2WinsThreeSets() {
        return MatchResult.of(SetResult.of(6, 3), SetResult.of(4, 6), SetResult.of(2, 6));
    }

    @Test
    void shouldCreateMatchWithTwoPairs() {
        Match match = Match.create(pair1(), pair2(), 1, 0, BracketType.MAIN);
        assertThat(match.id()).isNotNull();
        assertThat(match.pair1()).isNotNull();
        assertThat(match.pair2()).isNotNull();
        assertThat(match.roundNumber()).isEqualTo(1);
        assertThat(match.position()).isEqualTo(0);
        assertThat(match.bracketType()).isEqualTo(BracketType.MAIN);
    }

    @Test
    void shouldCreateMatchWithOnlyPair1() {
        Match match = Match.createWithPair1Only(pair1(), 1, 0, BracketType.MAIN);
        assertThat(match.pair1()).isNotNull();
        assertThat(match.pair2()).isNull();
        assertThat(match.isComplete()).isFalse();
    }

    @Test
    void shouldCreateEmptyMatchSlot() {
        Match match = Match.createEmpty(2, 0, BracketType.MAIN);
        assertThat(match.pair1()).isNull();
        assertThat(match.pair2()).isNull();
    }

    @Test
    void shouldStartWithNoResult() {
        Match match = Match.create(pair1(), pair2(), 1, 0, BracketType.MAIN);
        assertThat(match.result()).isEmpty();
        assertThat(match.isPlayed()).isFalse();
    }

    @Test
    void shouldRecordResult() {
        Match match = Match.create(pair1(), pair2(), 1, 0, BracketType.MAIN);
        match.recordResult(pair1WinsThreeSets());
        assertThat(match.result()).isPresent();
        assertThat(match.isPlayed()).isTrue();
    }

    @Test
    void shouldDetermineWinner() {
        Pair p1 = pair1();
        Pair p2 = pair2();
        Match match = Match.create(p1, p2, 1, 0, BracketType.MAIN);
        match.recordResult(pair1Wins());
        assertThat(match.winner()).isPresent().contains(p1);
        assertThat(match.loser()).isPresent().contains(p2);
    }

    @Test
    void shouldDetermineWinnerWhenPair2Wins() {
        Pair p1 = pair1();
        Pair p2 = pair2();
        Match match = Match.create(p1, p2, 1, 0, BracketType.MAIN);
        match.recordResult(pair2Wins());
        assertThat(match.winner()).isPresent().contains(p2);
        assertThat(match.loser()).isPresent().contains(p1);
    }

    @Test
    void shouldAllowSettingLocationInAdvance() {
        Match match = Match.create(pair1(), pair2(), 1, 0, BracketType.MAIN);
        match.setLocation(Location.of("Pista Central"));
        assertThat(match.location()).isPresent();
        assertThat(match.location().get().value()).isEqualTo("Pista Central");
    }

    @Test
    void shouldAllowSettingDateTimeInAdvance() {
        Match match = Match.create(pair1(), pair2(), 1, 0, BracketType.MAIN);
        LocalDateTime dt = LocalDateTime.of(2026, 5, 1, 10, 0);
        match.setDateTime(MatchDateTime.of(dt));
        assertThat(match.dateTime()).isPresent();
    }

    @Test
    void shouldAllowClearingLocation() {
        Match match = Match.create(pair1(), pair2(), 1, 0, BracketType.MAIN);
        match.setLocation(Location.of("Pista 1"));
        match.clearLocation();
        assertThat(match.location()).isEmpty();
    }

    @Test
    void shouldAllowClearingDateTime() {
        Match match = Match.create(pair1(), pair2(), 1, 0, BracketType.MAIN);
        match.setDateTime(MatchDateTime.of(LocalDateTime.now()));
        match.clearDateTime();
        assertThat(match.dateTime()).isEmpty();
    }

    @Test
    void shouldAllowUpdatingResult() {
        Match match = Match.create(pair1(), pair2(), 1, 0, BracketType.MAIN);
        match.recordResult(pair1Wins());
        match.recordResult(pair2WinsThreeSets());
        assertThat(match.result().get().winnerPosition()).isEqualTo(2);
    }

    @Test
    void shouldAllowClearingResult() {
        Match match = Match.create(pair1(), pair2(), 1, 0, BracketType.MAIN);
        match.recordResult(pair1Wins());
        match.clearResult();
        assertThat(match.result()).isEmpty();
        assertThat(match.isPlayed()).isFalse();
    }

    @Test
    void shouldSetPair2() {
        Match match = Match.createWithPair1Only(pair1(), 1, 0, BracketType.MAIN);
        Pair p2 = pair2();
        match.setPair2(p2);
        assertThat(match.pair2()).isEqualTo(p2);
    }

    @Test
    void shouldSetPair1() {
        Match match = Match.createEmpty(2, 0, BracketType.MAIN);
        Pair p1 = pair1();
        match.setPair1(p1);
        assertThat(match.pair1()).isEqualTo(p1);
    }

    @Test
    void shouldBeCompleteWhenBothPairsPresent() {
        Match match = Match.create(pair1(), pair2(), 1, 0, BracketType.MAIN);
        assertThat(match.isComplete()).isTrue();
    }

    @Test
    void shouldAutoResolveWhenOnePairIsBye() {
        Pair regular = pair1();
        Pair bye = Pair.bye();
        Match match = Match.create(regular, bye, 1, 0, BracketType.MAIN);
        assertThat(match.isByeMatch()).isTrue();
    }
}
