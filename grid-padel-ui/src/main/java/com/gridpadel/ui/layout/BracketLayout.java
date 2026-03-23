package com.gridpadel.ui.layout;

import java.util.List;

public record BracketLayout(List<MatchPosition> matchPositions, List<ConnectorLine> connectors) {

    public BracketLayout {
        matchPositions = List.copyOf(matchPositions);
        connectors = List.copyOf(connectors);
    }
}
