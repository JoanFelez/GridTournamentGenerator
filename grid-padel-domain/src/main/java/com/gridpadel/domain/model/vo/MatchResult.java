package com.gridpadel.domain.model.vo;

import com.gridpadel.domain.exception.ValidationException;
import io.vavr.collection.List;

public record MatchResult(List<SetResult> sets, boolean walkover, int walkoverPosition) {

    public MatchResult {
        if (walkover) {
            if (walkoverPosition != 1 && walkoverPosition != 2) {
                throw new ValidationException("Walkover position must be 1 or 2", "matchResult");
            }
            if (sets == null) {
                sets = List.empty();
            }
        } else {
            if (sets == null || sets.isEmpty()) {
                throw new ValidationException("Match must have at least one set result", "matchResult");
            }
            if (sets.size() < 2 || sets.size() > 3) {
                throw new ValidationException("Match must have 2 or 3 sets, got " + sets.size(), "matchResult");
            }

            long pair1Wins = sets.count(s -> s.winnerPosition() == 1);
            long pair2Wins = sets.count(s -> s.winnerPosition() == 2);

            if (sets.size() == 2) {
                if (pair1Wins != 2 && pair2Wins != 2) {
                    throw new ValidationException(
                            "With 2 sets, one pair must have won both", "matchResult");
                }
            }

            if (sets.size() == 3) {
                List<SetResult> firstTwo = sets.take(2);
                long p1FirstTwo = firstTwo.count(s -> s.winnerPosition() == 1);
                long p2FirstTwo = firstTwo.count(s -> s.winnerPosition() == 2);

                if (p1FirstTwo == 2) {
                    throw new ValidationException(
                            "Pair 1 already won after 2 sets — third set should not be played", "matchResult");
                }
                if (p2FirstTwo == 2) {
                    throw new ValidationException(
                            "Pair 2 already won after 2 sets — third set should not be played", "matchResult");
                }
            }
        }
    }

    public static MatchResult of(java.util.List<SetResult> sets) {
        if (sets == null) {
            return new MatchResult(null, false, 0);
        }
        return new MatchResult(List.ofAll(sets), false, 0);
    }

    public static MatchResult of(SetResult... sets) {
        return new MatchResult(List.of(sets), false, 0);
    }

    public static MatchResult walkover(int walkoverPosition) {
        return new MatchResult(List.empty(), true, walkoverPosition);
    }

    public boolean isWalkover() {
        return walkover;
    }

    public int setsWonByPair1() {
        return sets.count(s -> s.winnerPosition() == 1);
    }

    public int setsWonByPair2() {
        return sets.count(s -> s.winnerPosition() == 2);
    }

    public int winnerPosition() {
        if (walkover) {
            return walkoverPosition == 1 ? 2 : 1;
        }
        return setsWonByPair1() > setsWonByPair2() ? 1 : 2;
    }

    public int loserPosition() {
        return winnerPosition() == 1 ? 2 : 1;
    }

    @Override
    public String toString() {
        if (walkover) {
            return "W.O. (Pair " + walkoverPosition + ")";
        }
        return sets.map(SetResult::toString).mkString(" ");
    }
}
