package com.gridpadel.ui.layout;

import com.gridpadel.domain.model.Bracket;
import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.Round;
import com.gridpadel.domain.model.Tournament;
import io.vavr.collection.List;

public class BracketLayoutCalculator {

    public static final double MATCH_BOX_WIDTH = 220;
    public static final double MATCH_BOX_HEIGHT = 80;
    public static final double COLUMN_GAP = 80;
    public static final double VERTICAL_GAP = 20;
    public static final double PADDING = 40;

    public BracketLayout calculate(Tournament tournament) {
        Bracket mainBracket = tournament.mainBracket();
        Bracket consolationBracket = tournament.consolationBracket();

        int mainRounds = mainBracket.rounds().size();
        int consolationRounds = consolationBracket.rounds().size();

        double centerX = calculateCenterX(consolationRounds);

        int r1MatchCount = mainRounds > 0 ? mainBracket.rounds().get(0).matchCount() : 0;
        double totalHeight = calculateTotalHeight(r1MatchCount);

        List<MatchPosition> positions = List.empty();
        List<ConnectorLine> connectors = List.empty();

        var result = layoutBracket(mainBracket, centerX, totalHeight, true, positions, connectors);
        positions = result.positions;
        connectors = result.connectors;

        result = layoutBracket(consolationBracket, centerX, totalHeight, false, positions, connectors);
        positions = result.positions;
        connectors = result.connectors;

        connectors = addCrossBracketConnectors(mainBracket, consolationBracket, centerX, positions, connectors);

        return new BracketLayout(positions.toJavaList(), connectors.toJavaList());
    }

    double calculateCenterX(int consolationRounds) {
        if (consolationRounds == 0) return PADDING;
        return PADDING + consolationRounds * (MATCH_BOX_WIDTH + COLUMN_GAP) + COLUMN_GAP;
    }

    double calculateTotalHeight(int r1MatchCount) {
        if (r1MatchCount == 0) return PADDING * 2 + MATCH_BOX_HEIGHT;
        return PADDING * 2 + r1MatchCount * MATCH_BOX_HEIGHT + (r1MatchCount - 1) * VERTICAL_GAP;
    }

    private record LayoutResult(List<MatchPosition> positions, List<ConnectorLine> connectors) {}

    LayoutResult layoutBracket(Bracket bracket, double centerX, double totalHeight,
                        boolean isMain, List<MatchPosition> positions, List<ConnectorLine> connectors) {
        io.vavr.collection.List<Round> rounds = bracket.rounds();
        if (rounds.isEmpty()) return new LayoutResult(positions, connectors);

        for (int roundIdx = 0; roundIdx < rounds.size(); roundIdx++) {
            Round round = rounds.get(roundIdx);
            int matchCount = round.matchCount();

            double columnX = calculateColumnX(centerX, roundIdx, isMain);
            double[] matchYPositions = calculateMatchYPositions(
                    roundIdx, matchCount, totalHeight, rounds, isMain, positions);

            for (int matchIdx = 0; matchIdx < matchCount; matchIdx++) {
                Match match = round.matches().get(matchIdx);
                double y = matchYPositions[matchIdx];

                positions = positions.append(new MatchPosition(
                        match.id().value(), columnX, y,
                        round.roundNumber(), match.position(),
                        !isMain
                ));
            }

            if (roundIdx > 0) {
                connectors = addConnectors(round, rounds.get(roundIdx - 1), columnX, matchYPositions,
                        isMain, positions, connectors);
            }
        }

        return new LayoutResult(positions, connectors);
    }

    double calculateColumnX(double centerX, int roundIdx, boolean isMain) {
        double offset = roundIdx * (MATCH_BOX_WIDTH + COLUMN_GAP);
        if (isMain) {
            return centerX + offset;
        }
        return centerX - offset - MATCH_BOX_WIDTH - COLUMN_GAP;
    }

    double[] calculateMatchYPositions(int roundIdx, int matchCount, double totalHeight,
                                      io.vavr.collection.List<Round> rounds, boolean isMain,
                                      List<MatchPosition> existingPositions) {
        double[] yPositions = new double[matchCount];

        if (roundIdx == 0) {
            double availableHeight = totalHeight - 2 * PADDING;
            double slotHeight = matchCount > 1
                    ? availableHeight / matchCount
                    : availableHeight;
            for (int i = 0; i < matchCount; i++) {
                yPositions[i] = PADDING + i * slotHeight + (slotHeight - MATCH_BOX_HEIGHT) / 2;
            }
        } else {
            Round prevRound = rounds.get(roundIdx - 1);
            int prevMatchCount = prevRound.matchCount();

            for (int i = 0; i < matchCount; i++) {
                int feeder1Idx = i * 2;
                int feeder2Idx = i * 2 + 1;

                if (feeder2Idx < prevMatchCount) {
                    double y1 = findMatchY(prevRound.matches().get(feeder1Idx).id().value(), existingPositions);
                    double y2 = findMatchY(prevRound.matches().get(feeder2Idx).id().value(), existingPositions);
                    yPositions[i] = (y1 + y2) / 2;
                } else if (feeder1Idx < prevMatchCount) {
                    yPositions[i] = findMatchY(prevRound.matches().get(feeder1Idx).id().value(), existingPositions);
                } else {
                    double availableHeight = totalHeight - 2 * PADDING;
                    double slotHeight = matchCount > 1 ? availableHeight / matchCount : availableHeight;
                    yPositions[i] = PADDING + i * slotHeight + (slotHeight - MATCH_BOX_HEIGHT) / 2;
                }
            }
        }

        return yPositions;
    }

    double findMatchY(String matchId, List<MatchPosition> positions) {
        return positions.find(p -> p.matchId().equals(matchId))
                .map(MatchPosition::y)
                .getOrElse(0.0);
    }

    List<ConnectorLine> addConnectors(Round currentRound, Round prevRound, double columnX,
                        double[] currentYPositions, boolean isMain,
                        List<MatchPosition> positions, List<ConnectorLine> connectors) {

        int prevMatchCount = prevRound.matchCount();

        for (int i = 0; i < currentRound.matchCount(); i++) {
            int feeder1Idx = i * 2;
            int feeder2Idx = i * 2 + 1;

            if (feeder1Idx >= prevMatchCount) continue;

            double currentMatchCenterY = currentYPositions[i] + MATCH_BOX_HEIGHT / 2;
            double currentMatchEdgeX = isMain ? columnX : columnX + MATCH_BOX_WIDTH;
            double midX = isMain
                    ? columnX - COLUMN_GAP / 2
                    : columnX + MATCH_BOX_WIDTH + COLUMN_GAP / 2;

            double feeder1Y = findMatchY(prevRound.matches().get(feeder1Idx).id().value(), positions)
                    + MATCH_BOX_HEIGHT / 2;
            double feeder1EdgeX = isMain
                    ? columnX - COLUMN_GAP
                    : columnX + MATCH_BOX_WIDTH + COLUMN_GAP;

            connectors = connectors.append(new ConnectorLine(feeder1EdgeX, feeder1Y, midX, feeder1Y));
            connectors = connectors.append(new ConnectorLine(midX, currentMatchCenterY, currentMatchEdgeX, currentMatchCenterY));

            if (feeder2Idx < prevMatchCount) {
                double feeder2Y = findMatchY(prevRound.matches().get(feeder2Idx).id().value(), positions)
                        + MATCH_BOX_HEIGHT / 2;
                connectors = connectors.append(new ConnectorLine(feeder1EdgeX, feeder2Y, midX, feeder2Y));
                connectors = connectors.append(new ConnectorLine(midX, feeder1Y, midX, feeder2Y));
            }
        }

        return connectors;
    }

    private List<ConnectorLine> addCrossBracketConnectors(Bracket mainBracket, Bracket consolationBracket,
                                                           double centerX, List<MatchPosition> positions,
                                                           List<ConnectorLine> connectors) {
        if (mainBracket.rounds().isEmpty() || consolationBracket.rounds().isEmpty()) {
            return connectors;
        }

        Round mainR1 = mainBracket.rounds().get(0);
        Round consolR1 = consolationBracket.rounds().get(0);

        double mainR1LeftEdge = centerX;
        double consolR1RightEdge = centerX - COLUMN_GAP;
        double midX = centerX - COLUMN_GAP / 2;

        for (int i = 0; i < consolR1.matchCount(); i++) {
            int feeder1Idx = i * 2;
            int feeder2Idx = i * 2 + 1;

            if (feeder1Idx >= mainR1.matchCount()) continue;

            double consolY = findMatchY(consolR1.matches().get(i).id().value(), positions)
                    + MATCH_BOX_HEIGHT / 2;

            double feeder1Y = findMatchY(mainR1.matches().get(feeder1Idx).id().value(), positions)
                    + MATCH_BOX_HEIGHT / 2;

            connectors = connectors.append(new ConnectorLine(mainR1LeftEdge, feeder1Y, midX, feeder1Y));
            connectors = connectors.append(new ConnectorLine(midX, consolY, consolR1RightEdge, consolY));

            if (feeder2Idx < mainR1.matchCount()) {
                double feeder2Y = findMatchY(mainR1.matches().get(feeder2Idx).id().value(), positions)
                        + MATCH_BOX_HEIGHT / 2;
                connectors = connectors.append(new ConnectorLine(mainR1LeftEdge, feeder2Y, midX, feeder2Y));
                connectors = connectors.append(new ConnectorLine(midX, feeder1Y, midX, feeder2Y));
            }
        }

        return connectors;
    }
}
