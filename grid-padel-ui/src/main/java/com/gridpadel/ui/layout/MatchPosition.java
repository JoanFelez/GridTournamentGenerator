package com.gridpadel.ui.layout;

import java.util.Objects;

public record MatchPosition(String matchId, double x, double y, int roundNumber, int position, boolean isConsolation) {

    public MatchPosition {
        Objects.requireNonNull(matchId);
    }
}
