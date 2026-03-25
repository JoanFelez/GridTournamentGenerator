package com.gridpadel.infrastructure.export;

import com.gridpadel.domain.model.*;
import com.gridpadel.domain.port.BracketExportPort;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import io.vavr.collection.List;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.OutputStream;

@Component
public class PdfBracketExporter implements BracketExportPort {

    private static final float BOX_W = 150;
    private static final float BOX_H = 50;
    private static final float COL_GAP = 50;
    private static final float V_GAP = 14;
    private static final float PADDING = 40;
    private static final float HEADER_HEIGHT = 30;

    @Override
    public void exportToPdf(Tournament tournament, OutputStream outputStream) {
        Bracket mainBracket = tournament.mainBracket();
        Bracket consolationBracket = tournament.consolationBracket();

        int mainRounds = mainBracket.rounds().size();
        int consolRounds = consolationBracket.rounds().size();
        int r1Count = mainRounds > 0 ? mainBracket.rounds().get(0).matchCount() : 0;

        float centerX = consolRounds == 0 ? PADDING : PADDING + consolRounds * (BOX_W + COL_GAP) + COL_GAP;
        float totalHeight = PADDING * 2 + HEADER_HEIGHT + r1Count * BOX_H + Math.max(0, r1Count - 1) * V_GAP;

        float totalWidth = centerX + mainRounds * (BOX_W + COL_GAP) + PADDING;
        float pageHeight = Math.max(totalHeight + PADDING, 400);

        try {
            Rectangle pageSize = new Rectangle(totalWidth, pageHeight);
            Document document = new Document(pageSize, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            PdfContentByte cb = writer.getDirectContent();
            float flipY = pageHeight;

            // Title
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false);
            BaseFont bfNormal = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);

            cb.beginText();
            cb.setFontAndSize(bf, 14);
            cb.setColorFill(new Color(38, 50, 56));
            cb.showTextAligned(Element.ALIGN_LEFT, tournament.name(), PADDING, flipY - 25, 0);
            cb.endText();

            float contentStartY = HEADER_HEIGHT;

            // Draw main bracket
            java.util.Map<String, float[]> matchPositions = new java.util.HashMap<>();
            if (mainRounds > 0) {
                drawBracketLabel(cb, bf, "CUADRO PRINCIPAL", centerX + BOX_W / 2, flipY - contentStartY - 5, new Color(21, 101, 192));
                drawBracket(cb, bf, bfNormal, mainBracket, centerX, totalHeight, true, flipY, contentStartY, matchPositions);
            }

            // Draw consolation bracket
            if (consolRounds > 0) {
                float consolLabelX = centerX - COL_GAP - BOX_W / 2;
                drawBracketLabel(cb, bf, "CUADRO DE CONSOLACIÓN", consolLabelX, flipY - contentStartY - 5, new Color(198, 40, 40));
                drawBracket(cb, bf, bfNormal, consolationBracket, centerX, totalHeight, false, flipY, contentStartY, matchPositions);
            }

            // Draw connectors
            drawConnectors(cb, mainBracket, consolationBracket, centerX, flipY, contentStartY, matchPositions);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private void drawBracketLabel(PdfContentByte cb, BaseFont bf, String text, float x, float y, Color color) {
        cb.beginText();
        cb.setFontAndSize(bf, 10);
        cb.setColorFill(color);
        cb.showTextAligned(Element.ALIGN_CENTER, text, x, y, 0);
        cb.endText();
    }

    private void drawBracket(PdfContentByte cb, BaseFont bf, BaseFont bfNormal,
                             Bracket bracket, float centerX, float totalHeight,
                             boolean isMain, float flipY, float contentStartY,
                             java.util.Map<String, float[]> matchPositions) {

        List<Round> rounds = bracket.rounds();
        if (rounds.isEmpty()) return;

        for (int roundIdx = 0; roundIdx < rounds.size(); roundIdx++) {
            Round round = rounds.get(roundIdx);
            int matchCount = round.matchCount();

            float colX = columnX(centerX, roundIdx, isMain);
            float[] yPositions = calculateYPositions(roundIdx, matchCount, totalHeight, rounds, isMain, matchPositions, contentStartY);

            for (int i = 0; i < matchCount; i++) {
                Match match = round.matches().get(i);
                float x = colX;
                float y = yPositions[i];
                matchPositions.put(match.id().value(), new float[]{x, y});
                drawMatchBox(cb, bf, bfNormal, match, x, flipY - y - contentStartY - HEADER_HEIGHT);
            }

            // Draw round header above the topmost match in this column
            float minY = yPositions[0];
            for (float yp : yPositions) { minY = Math.min(minY, yp); }
            String roundLabel = getRoundLabel(roundIdx, rounds.size(), isMain);
            float headerX = colX + BOX_W / 2;
            float headerPdfY = flipY - minY - contentStartY - HEADER_HEIGHT + 14;
            cb.beginText();
            cb.setFontAndSize(bf, 8);
            cb.setColorFill(isMain ? new Color(21, 101, 192) : new Color(198, 40, 40));
            cb.showTextAligned(Element.ALIGN_CENTER, roundLabel, headerX, headerPdfY, 0);
            cb.endText();
        }
    }

    private void drawMatchBox(PdfContentByte cb, BaseFont bf, BaseFont bfNormal, Match match, float x, float pdfY) {
        // Background
        Color bgColor = match.isByeMatch() ? new Color(232, 232, 232) : Color.WHITE;
        Color borderColor = match.isPlayed() ? new Color(46, 125, 50)
                : match.isComplete() ? new Color(21, 101, 192) : new Color(158, 158, 158);

        cb.setColorFill(bgColor);
        cb.rectangle(x, pdfY - BOX_H, BOX_W, BOX_H);
        cb.fill();

        cb.setColorStroke(borderColor);
        cb.setLineWidth(match.isPlayed() ? 1.5f : 1f);
        cb.rectangle(x, pdfY - BOX_H, BOX_W, BOX_H);
        cb.stroke();

        // Pair names
        String pair1Name = pairName(match, true);
        String pair2Name = pairName(match, false);
        boolean pair1Winner = isWinner(match, true);
        boolean pair2Winner = isWinner(match, false);

        float textX = x + 6;

        // Pair 1
        cb.beginText();
        cb.setFontAndSize(pair1Winner ? bf : bfNormal, 8);
        cb.setColorFill(pair1Winner ? new Color(46, 125, 50) : new Color(51, 51, 51));
        cb.showTextAligned(Element.ALIGN_LEFT, truncate(pair1Name, 20), textX, pdfY - 15, 0);
        cb.endText();

        // Score for pair 1
        if (match.isPlayed()) {
            String score1 = scoreText(match, true);
            cb.beginText();
            cb.setFontAndSize(bfNormal, 7);
            cb.setColorFill(new Color(85, 85, 85));
            cb.showTextAligned(Element.ALIGN_RIGHT, score1, x + BOX_W - 6, pdfY - 15, 0);
            cb.endText();
        }

        // Separator line
        cb.setColorStroke(new Color(204, 204, 204));
        cb.setLineWidth(0.5f);
        cb.moveTo(x + 4, pdfY - BOX_H / 2);
        cb.lineTo(x + BOX_W - 4, pdfY - BOX_H / 2);
        cb.stroke();

        // Pair 2
        cb.beginText();
        cb.setFontAndSize(pair2Winner ? bf : bfNormal, 8);
        cb.setColorFill(pair2Winner ? new Color(46, 125, 50) : new Color(51, 51, 51));
        cb.showTextAligned(Element.ALIGN_LEFT, truncate(pair2Name, 20), textX, pdfY - BOX_H + 10, 0);
        cb.endText();

        // Score for pair 2
        if (match.isPlayed()) {
            String score2 = scoreText(match, false);
            cb.beginText();
            cb.setFontAndSize(bfNormal, 7);
            cb.setColorFill(new Color(85, 85, 85));
            cb.showTextAligned(Element.ALIGN_RIGHT, score2, x + BOX_W - 6, pdfY - BOX_H + 10, 0);
            cb.endText();
        }

        // Details (location, date)
        String details = buildDetails(match);
        if (!details.isEmpty()) {
            cb.beginText();
            cb.setFontAndSize(bfNormal, 6);
            cb.setColorFill(new Color(120, 120, 120));
            cb.showTextAligned(Element.ALIGN_CENTER, details, x + BOX_W / 2, pdfY - BOX_H + 3, 0);
            cb.endText();
        }
    }

    private void drawConnectors(PdfContentByte cb, Bracket mainBracket, Bracket consolationBracket,
                                float centerX, float flipY, float contentStartY,
                                java.util.Map<String, float[]> matchPositions) {
        cb.setColorStroke(new Color(102, 102, 102));
        cb.setLineWidth(0.8f);

        drawBracketConnectors(cb, mainBracket, centerX, true, flipY, contentStartY, matchPositions);
        drawBracketConnectors(cb, consolationBracket, centerX, false, flipY, contentStartY, matchPositions);
        drawCrossBracketConnectors(cb, mainBracket, consolationBracket, centerX, flipY, contentStartY, matchPositions);
    }

    private void drawBracketConnectors(PdfContentByte cb, Bracket bracket, float centerX,
                                       boolean isMain, float flipY, float contentStartY,
                                       java.util.Map<String, float[]> matchPositions) {
        List<Round> rounds = bracket.rounds();
        for (int roundIdx = 1; roundIdx < rounds.size(); roundIdx++) {
            Round round = rounds.get(roundIdx);
            Round prevRound = rounds.get(roundIdx - 1);

            for (int i = 0; i < round.matchCount(); i++) {
                int f1 = i * 2;
                int f2 = i * 2 + 1;
                if (f1 >= prevRound.matchCount()) continue;

                Match currentMatch = round.matches().get(i);
                float[] currentPos = matchPositions.get(currentMatch.id().value());
                if (currentPos == null) continue;

                float currentCenterY = currentPos[1] + BOX_H / 2;
                float currentEdgeX = isMain ? currentPos[0] : currentPos[0] + BOX_W;
                float midX = isMain ? currentPos[0] - COL_GAP / 2 : currentPos[0] + BOX_W + COL_GAP / 2;

                Match feeder1 = prevRound.matches().get(f1);
                float[] f1Pos = matchPositions.get(feeder1.id().value());
                if (f1Pos == null) continue;

                float f1CenterY = f1Pos[1] + BOX_H / 2;
                float f1EdgeX = isMain ? f1Pos[0] + BOX_W : f1Pos[0];

                drawLine(cb, f1EdgeX, f1CenterY, midX, f1CenterY, flipY, contentStartY);
                drawLine(cb, midX, currentCenterY, currentEdgeX, currentCenterY, flipY, contentStartY);

                if (f2 < prevRound.matchCount()) {
                    Match feeder2 = prevRound.matches().get(f2);
                    float[] f2Pos = matchPositions.get(feeder2.id().value());
                    if (f2Pos != null) {
                        float f2CenterY = f2Pos[1] + BOX_H / 2;
                        drawLine(cb, f1EdgeX, f2CenterY, midX, f2CenterY, flipY, contentStartY);
                        drawLine(cb, midX, f1CenterY, midX, f2CenterY, flipY, contentStartY);
                    }
                }
            }
        }
    }

    private void drawCrossBracketConnectors(PdfContentByte cb, Bracket mainBracket, Bracket consolationBracket,
                                            float centerX, float flipY, float contentStartY,
                                            java.util.Map<String, float[]> matchPositions) {
        if (mainBracket.rounds().isEmpty() || consolationBracket.rounds().isEmpty()) return;

        Round mainR1 = mainBracket.rounds().get(0);
        Round consolR1 = consolationBracket.rounds().get(0);
        float midX = centerX - COL_GAP / 2;

        for (int i = 0; i < consolR1.matchCount(); i++) {
            int f1 = i * 2;
            int f2 = i * 2 + 1;
            if (f1 >= mainR1.matchCount()) continue;

            Match consolMatch = consolR1.matches().get(i);
            float[] consolPos = matchPositions.get(consolMatch.id().value());
            Match mainFeeder1 = mainR1.matches().get(f1);
            float[] mf1Pos = matchPositions.get(mainFeeder1.id().value());
            if (consolPos == null || mf1Pos == null) continue;

            float consolCY = consolPos[1] + BOX_H / 2;
            float f1CY = mf1Pos[1] + BOX_H / 2;

            drawLine(cb, centerX, f1CY, midX, f1CY, flipY, contentStartY);
            drawLine(cb, midX, consolCY, centerX - COL_GAP, consolCY, flipY, contentStartY);

            if (f2 < mainR1.matchCount()) {
                Match mainFeeder2 = mainR1.matches().get(f2);
                float[] mf2Pos = matchPositions.get(mainFeeder2.id().value());
                if (mf2Pos != null) {
                    float f2CY = mf2Pos[1] + BOX_H / 2;
                    drawLine(cb, centerX, f2CY, midX, f2CY, flipY, contentStartY);
                    drawLine(cb, midX, f1CY, midX, f2CY, flipY, contentStartY);
                }
            }
        }
    }

    private void drawLine(PdfContentByte cb, float x1, float y1, float x2, float y2, float flipY, float offsetY) {
        float py1 = flipY - y1 - offsetY - HEADER_HEIGHT;
        float py2 = flipY - y2 - offsetY - HEADER_HEIGHT;
        cb.moveTo(x1, py1);
        cb.lineTo(x2, py2);
        cb.stroke();
    }

    private float columnX(float centerX, int roundIdx, boolean isMain) {
        float offset = roundIdx * (BOX_W + COL_GAP);
        return isMain ? centerX + offset : centerX - offset - BOX_W - COL_GAP;
    }

    private float[] calculateYPositions(int roundIdx, int matchCount, float totalHeight,
                                        List<Round> rounds, boolean isMain,
                                        java.util.Map<String, float[]> existingPositions, float contentStartY) {
        float[] yPositions = new float[matchCount];
        float availableHeight = totalHeight - 2 * PADDING;

        if (roundIdx == 0) {
            float slotHeight = matchCount > 1 ? availableHeight / matchCount : availableHeight;
            for (int i = 0; i < matchCount; i++) {
                yPositions[i] = PADDING + i * slotHeight + (slotHeight - BOX_H) / 2;
            }
        } else {
            Round prevRound = rounds.get(roundIdx - 1);
            int prevCount = prevRound.matchCount();
            for (int i = 0; i < matchCount; i++) {
                int f1 = i * 2;
                int f2 = i * 2 + 1;
                if (f2 < prevCount) {
                    float[] p1 = existingPositions.get(prevRound.matches().get(f1).id().value());
                    float[] p2 = existingPositions.get(prevRound.matches().get(f2).id().value());
                    yPositions[i] = (p1 != null && p2 != null) ? (p1[1] + p2[1]) / 2 : PADDING;
                } else if (f1 < prevCount) {
                    float[] p1 = existingPositions.get(prevRound.matches().get(f1).id().value());
                    yPositions[i] = p1 != null ? p1[1] : PADDING;
                } else {
                    float slotHeight = matchCount > 1 ? availableHeight / matchCount : availableHeight;
                    yPositions[i] = PADDING + i * slotHeight + (slotHeight - BOX_H) / 2;
                }
            }
        }
        return yPositions;
    }

    private String getRoundLabel(int roundIdx, int totalRounds, boolean isMain) {
        int roundsFromFinal = totalRounds - roundIdx;
        return switch (roundsFromFinal) {
            case 1 -> "Final";
            case 2 -> "Semifinal";
            case 3 -> "Cuartos";
            case 4 -> "Octavos";
            default -> "Ronda " + (roundIdx + 1);
        };
    }

    private String pairName(Match match, boolean isPair1) {
        var pair = isPair1 ? match.pair1() : match.pair2();
        if (pair == null) return "TBD";
        if (pair.isBye()) return "BYE";
        return pair.displayName();
    }

    private boolean isWinner(Match match, boolean isPair1) {
        if (!match.isPlayed()) return false;
        return match.winner()
                .map(w -> isPair1 ? w.equals(match.pair1()) : w.equals(match.pair2()))
                .getOrElse(false);
    }

    private String scoreText(Match match, boolean isPair1) {
        if (match.isWalkover()) {
            return match.result()
                    .map(r -> {
                        boolean isWoPair = (isPair1 && r.walkoverPosition() == 1) || (!isPair1 && r.walkoverPosition() == 2);
                        return isWoPair ? "W.O." : "";
                    }).getOrElse("");
        }
        return match.result()
                .map(r -> r.sets()
                        .map(s -> isPair1 ? String.valueOf(s.pair1Games()) : String.valueOf(s.pair2Games()))
                        .mkString(" "))
                .getOrElse("");
    }

    private String buildDetails(Match match) {
        StringBuilder sb = new StringBuilder();
        match.location().forEach(loc -> sb.append(loc.value()));
        match.dateTime().forEach(dt -> {
            if (!sb.isEmpty()) sb.append(" | ");
            sb.append(dt.value().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")));
        });
        return sb.toString();
    }

    private String truncate(String text, int maxLen) {
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 1) + "…";
    }
}
