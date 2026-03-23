package com.gridpadel.domain.model.vo;

public record MatchResult(int setsWonByPair1, int setsWonByPair2) {

    public MatchResult {
        if (setsWonByPair1 < 0 || setsWonByPair2 < 0) {
            throw new IllegalArgumentException("Sets won cannot be negative");
        }
        if (setsWonByPair1 == setsWonByPair2) {
            throw new IllegalArgumentException("Match result cannot be a draw — one pair must win");
        }
    }

    public static MatchResult of(int setsWonByPair1, int setsWonByPair2) {
        return new MatchResult(setsWonByPair1, setsWonByPair2);
    }

    public int winnerPosition() {
        return setsWonByPair1 > setsWonByPair2 ? 1 : 2;
    }

    public int loserPosition() {
        return winnerPosition() == 1 ? 2 : 1;
    }

    @Override
    public String toString() {
        return setsWonByPair1 + "-" + setsWonByPair2;
    }
}
