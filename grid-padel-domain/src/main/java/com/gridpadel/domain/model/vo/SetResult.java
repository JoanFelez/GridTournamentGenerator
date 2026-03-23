package com.gridpadel.domain.model.vo;

public record SetResult(int pair1Games, int pair2Games) {

    public SetResult {
        if (pair1Games < 0 || pair2Games < 0) {
            throw new IllegalArgumentException("Games cannot be negative");
        }
        if (!isValidSetScore(pair1Games, pair2Games)) {
            throw new IllegalArgumentException(
                    "Invalid set score: " + pair1Games + "-" + pair2Games
                            + ". Valid: 6-0..6-4, 7-5, 7-6 (and reverses)");
        }
    }

    public static SetResult of(int pair1Games, int pair2Games) {
        return new SetResult(pair1Games, pair2Games);
    }

    public int winnerPosition() {
        return pair1Games > pair2Games ? 1 : 2;
    }

    @Override
    public String toString() {
        return pair1Games + "-" + pair2Games;
    }

    private static boolean isValidSetScore(int a, int b) {
        return isValidFromOneSide(a, b) || isValidFromOneSide(b, a);
    }

    private static boolean isValidFromOneSide(int winner, int loser) {
        if (winner == 6 && loser >= 0 && loser <= 4) return true;
        if (winner == 7 && (loser == 5 || loser == 6)) return true;
        return false;
    }
}
