package com.gridpadel.domain.model.vo;

import java.util.List;
import java.util.stream.Collectors;

public record MatchResult(List<SetResult> sets) {

    public MatchResult {
        if (sets == null || sets.isEmpty()) {
            throw new IllegalArgumentException("Match must have at least one set result");
        }
        if (sets.size() < 2 || sets.size() > 3) {
            throw new IllegalArgumentException("Match must have 2 or 3 sets, got " + sets.size());
        }

        sets = List.copyOf(sets);

        long pair1Wins = sets.stream().filter(s -> s.winnerPosition() == 1).count();
        long pair2Wins = sets.stream().filter(s -> s.winnerPosition() == 2).count();

        if (sets.size() == 2) {
            if (pair1Wins != 2 && pair2Wins != 2) {
                throw new IllegalArgumentException(
                        "With 2 sets, one pair must have won both");
            }
        }

        if (sets.size() == 3) {
            long p1FirstTwo = sets.subList(0, 2).stream().filter(s -> s.winnerPosition() == 1).count();
            long p2FirstTwo = sets.subList(0, 2).stream().filter(s -> s.winnerPosition() == 2).count();

            if (p1FirstTwo == 2) {
                throw new IllegalArgumentException(
                        "Pair 1 already won after 2 sets — third set should not be played");
            }
            if (p2FirstTwo == 2) {
                throw new IllegalArgumentException(
                        "Pair 2 already won after 2 sets — third set should not be played");
            }
        }
    }

    public static MatchResult of(List<SetResult> sets) {
        return new MatchResult(sets);
    }

    public static MatchResult of(SetResult... sets) {
        return new MatchResult(List.of(sets));
    }

    public int setsWonByPair1() {
        return (int) sets.stream().filter(s -> s.winnerPosition() == 1).count();
    }

    public int setsWonByPair2() {
        return (int) sets.stream().filter(s -> s.winnerPosition() == 2).count();
    }

    public int winnerPosition() {
        return setsWonByPair1() > setsWonByPair2() ? 1 : 2;
    }

    public int loserPosition() {
        return winnerPosition() == 1 ? 2 : 1;
    }

    @Override
    public String toString() {
        return sets.stream()
                .map(SetResult::toString)
                .collect(Collectors.joining(" "));
    }
}
