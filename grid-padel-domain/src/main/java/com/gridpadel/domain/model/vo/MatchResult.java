package com.gridpadel.domain.model.vo;

import java.util.Objects;

public final class MatchResult {

    private final int setsWonByPair1;
    private final int setsWonByPair2;

    private MatchResult(int setsWonByPair1, int setsWonByPair2) {
        if (setsWonByPair1 < 0 || setsWonByPair2 < 0) {
            throw new IllegalArgumentException("Sets won cannot be negative");
        }
        if (setsWonByPair1 == setsWonByPair2) {
            throw new IllegalArgumentException("Match result cannot be a draw — one pair must win");
        }
        this.setsWonByPair1 = setsWonByPair1;
        this.setsWonByPair2 = setsWonByPair2;
    }

    public static MatchResult of(int setsWonByPair1, int setsWonByPair2) {
        return new MatchResult(setsWonByPair1, setsWonByPair2);
    }

    public int setsWonByPair1() {
        return setsWonByPair1;
    }

    public int setsWonByPair2() {
        return setsWonByPair2;
    }

    /**
     * Returns 1 if pair1 won, 2 if pair2 won.
     */
    public int winnerPosition() {
        return setsWonByPair1 > setsWonByPair2 ? 1 : 2;
    }

    /**
     * Returns 1 if pair1 lost, 2 if pair2 lost.
     */
    public int loserPosition() {
        return winnerPosition() == 1 ? 2 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchResult other)) return false;
        return setsWonByPair1 == other.setsWonByPair1 && setsWonByPair2 == other.setsWonByPair2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(setsWonByPair1, setsWonByPair2);
    }

    @Override
    public String toString() {
        return setsWonByPair1 + "-" + setsWonByPair2;
    }
}
