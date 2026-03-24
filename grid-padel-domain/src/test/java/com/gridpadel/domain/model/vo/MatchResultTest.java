package com.gridpadel.domain.model.vo;

import com.gridpadel.domain.exception.DomainException;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MatchResultTest {

    private SetResult set(int p1, int p2) {
        return SetResult.of(p1, p2);
    }

    // --- Valid match results (best of 3: first to win 2 sets) ---

    @Test
    void shouldCreateMatchWonTwoZeroByPair1() {
        MatchResult result = MatchResult.of(List.of(set(6, 3), set(6, 4)));
        assertThat(result.setsWonByPair1()).isEqualTo(2);
        assertThat(result.setsWonByPair2()).isEqualTo(0);
        assertThat(result.winnerPosition()).isEqualTo(1);
        assertThat(result.loserPosition()).isEqualTo(2);
    }

    @Test
    void shouldCreateMatchWonTwoZeroByPair2() {
        MatchResult result = MatchResult.of(List.of(set(3, 6), set(4, 6)));
        assertThat(result.setsWonByPair1()).isEqualTo(0);
        assertThat(result.setsWonByPair2()).isEqualTo(2);
        assertThat(result.winnerPosition()).isEqualTo(2);
        assertThat(result.loserPosition()).isEqualTo(1);
    }

    @Test
    void shouldCreateMatchWonTwoOneByPair1() {
        MatchResult result = MatchResult.of(List.of(set(4, 6), set(6, 2), set(7, 5)));
        assertThat(result.setsWonByPair1()).isEqualTo(2);
        assertThat(result.setsWonByPair2()).isEqualTo(1);
        assertThat(result.winnerPosition()).isEqualTo(1);
    }

    @Test
    void shouldCreateMatchWonTwoOneByPair2() {
        MatchResult result = MatchResult.of(List.of(set(6, 1), set(5, 7), set(6, 7)));
        assertThat(result.setsWonByPair1()).isEqualTo(1);
        assertThat(result.setsWonByPair2()).isEqualTo(2);
        assertThat(result.winnerPosition()).isEqualTo(2);
    }

    @Test
    void shouldAcceptTiebreakSets() {
        MatchResult result = MatchResult.of(List.of(set(7, 6), set(6, 7), set(7, 6)));
        assertThat(result.setsWonByPair1()).isEqualTo(2);
        assertThat(result.setsWonByPair2()).isEqualTo(1);
    }

    // --- Accessors ---

    @Test
    void shouldReturnSetResults() {
        List<SetResult> sets = List.of(set(6, 0), set(6, 1));
        MatchResult result = MatchResult.of(sets);
        assertThat(result.sets()).hasSize(2);
        assertThat(result.sets().get(0)).isEqualTo(set(6, 0));
        assertThat(result.sets().get(1)).isEqualTo(set(6, 1));
    }

    @Test
    void shouldReturnSetsWonCounts() {
        MatchResult result = MatchResult.of(List.of(set(6, 3), set(2, 6), set(6, 4)));
        assertThat(result.setsWonByPair1()).isEqualTo(2);
        assertThat(result.setsWonByPair2()).isEqualTo(1);
    }

    // --- Invalid match results ---

    @Test
    void shouldRejectNullSetsList() {
        assertThatThrownBy(() -> MatchResult.of((List<SetResult>) null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldRejectEmptySetsList() {
        assertThatThrownBy(() -> MatchResult.of(List.of()))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldRejectSingleSet() {
        assertThatThrownBy(() -> MatchResult.of(List.of(set(6, 3))))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldRejectFourSets() {
        assertThatThrownBy(() -> MatchResult.of(List.of(set(6, 3), set(3, 6), set(6, 4), set(6, 2))))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldRejectTwoSetsWhenNoOneWonTwo() {
        // 1-1 after 2 sets — match not decided
        assertThatThrownBy(() -> MatchResult.of(List.of(set(6, 3), set(3, 6))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("2 sets");
    }

    @Test
    void shouldRejectThreeSetsWhenSomeoneWonFirstTwo() {
        // Pair 1 won first 2 sets — third set shouldn't be played
        assertThatThrownBy(() -> MatchResult.of(List.of(set(6, 3), set(6, 4), set(6, 2))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already won");
    }

    @Test
    void shouldRejectThreeSetsWhenPair2WonFirstTwo() {
        assertThatThrownBy(() -> MatchResult.of(List.of(set(3, 6), set(4, 6), set(2, 6))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already won");
    }

    // --- Equality ---

    @Test
    void shouldBeEqualWhenSameSetResults() {
        MatchResult r1 = MatchResult.of(List.of(set(6, 3), set(6, 4)));
        MatchResult r2 = MatchResult.of(List.of(set(6, 3), set(6, 4)));
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentSetResults() {
        MatchResult r1 = MatchResult.of(List.of(set(6, 3), set(6, 4)));
        MatchResult r2 = MatchResult.of(List.of(set(6, 3), set(6, 2)));
        assertThat(r1).isNotEqualTo(r2);
    }

    // --- toString ---

    @Test
    void shouldFormatTwoSetsToString() {
        MatchResult result = MatchResult.of(List.of(set(6, 3), set(6, 4)));
        assertThat(result.toString()).isEqualTo("6-3 6-4");
    }

    @Test
    void shouldFormatThreeSetsToString() {
        MatchResult result = MatchResult.of(List.of(set(4, 6), set(6, 2), set(7, 5)));
        assertThat(result.toString()).isEqualTo("4-6 6-2 7-5");
    }

    // --- Convenience factory ---

    @Test
    void shouldCreateFromVarargs() {
        MatchResult result = MatchResult.of(set(6, 3), set(6, 4));
        assertThat(result.sets()).hasSize(2);
        assertThat(result.winnerPosition()).isEqualTo(1);
    }

    @Test
    void shouldReturnImmutableSetsList() {
        MatchResult result = MatchResult.of(set(6, 3), set(6, 4));
        // VAVR List is inherently immutable — append returns a new list
        io.vavr.collection.List<SetResult> original = result.sets();
        io.vavr.collection.List<SetResult> appended = original.append(set(6, 0));
        assertThat(original).hasSize(2);
        assertThat(appended).hasSize(3);
    }
}
