package com.gridpadel.infrastructure.publish;

import com.gridpadel.domain.model.*;
import io.vavr.collection.List;

import java.time.format.DateTimeFormatter;

/**
 * Generates self-contained HTML pages for tournament brackets.
 */
public class HtmlBracketGenerator {

    private static final double BOX_W = 200;
    private static final double BOX_H = 70;
    private static final double COL_GAP = 60;
    private static final double V_GAP = 16;
    private static final double PADDING = 50;
    private static final double HEADER_AREA = 60;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    public String generateIndexPage(java.util.List<Tournament> tournaments) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Torneos de Pádel</title>");
        html.append("<style>").append(indexCss()).append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<h1>🏆 Torneos de Pádel</h1>");
        html.append("<div class='tournament-list'>");

        // One card per tournament (grouped by name)
        java.util.LinkedHashMap<String, java.util.List<Tournament>> byName = new java.util.LinkedHashMap<>();
        for (Tournament t : tournaments) {
            byName.computeIfAbsent(t.name(), k -> new java.util.ArrayList<>()).add(t);
        }

        for (var entry : byName.entrySet()) {
            String tournamentSlug = slugify(entry.getKey());
            int totalPairs = entry.getValue().stream().mapToInt(Tournament::pairCount).sum();
            int totalCategories = entry.getValue().size();
            html.append("<a href='").append(tournamentSlug).append(".html' class='tournament-card'>");
            html.append("<h3>").append(escapeHtml(entry.getKey())).append("</h3>");
            html.append("<p>").append(totalCategories).append(" categoría(s) • ").append(totalPairs).append(" parejas</p>");
            html.append("</a>");
        }

        html.append("</div></div></body></html>");
        return html.toString();
    }

    /**
     * Generates a landing page for a tournament showing all its categories.
     * This is the shareable URL per tournament.
     */
    public String generateTournamentLandingPage(String tournamentName, java.util.List<Tournament> categories) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>").append(escapeHtml(tournamentName)).append("</title>");
        html.append("<style>").append(indexCss()).append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div style='display:flex;align-items:center;gap:16px;margin-bottom:24px;'>");
        html.append("<a href='index.html' style='color:#1565c0;text-decoration:none;font-size:14px;'>← Torneos</a>");
        html.append("<h1 style='margin:0;'>🏆 ").append(escapeHtml(tournamentName)).append("</h1>");
        html.append("</div>");
        html.append("<div class='tournament-list'>");

        for (Tournament t : categories) {
            String slug = slugify(tournamentName + "-" + t.category());
            String displayName = t.category() != null && !t.category().isBlank()
                    ? t.category() : "(sin categoría)";
            html.append("<a href='").append(slug).append(".html' class='tournament-card'>");
            html.append("<h3>").append(escapeHtml(displayName)).append("</h3>");
            html.append("<p>").append(t.pairCount()).append(" parejas</p>");
            int played = t.allMatches().count(Match::isPlayed);
            int total = t.allMatches().size();
            html.append("<p>Partidos: ").append(played).append("/").append(total).append("</p>");
            html.append("</a>");
        }

        html.append("</div></div></body></html>");
        return html.toString();
    }

    public String generateTournamentPage(Tournament tournament) {
        Bracket mainBracket = tournament.mainBracket();
        Bracket consolationBracket = tournament.consolationBracket();

        int mainRounds = mainBracket.rounds().size();
        int consolRounds = consolationBracket.rounds().size();
        int r1Count = mainRounds > 0 ? mainBracket.rounds().get(0).matchCount() : 0;

        double centerX = consolRounds == 0 ? PADDING : PADDING + consolRounds * (BOX_W + COL_GAP) + COL_GAP;
        double contentHeight = PADDING * 2 + r1Count * BOX_H + Math.max(0, r1Count - 1) * V_GAP;
        double totalWidth = centerX + mainRounds * (BOX_W + COL_GAP) + PADDING;
        double totalHeight = contentHeight + HEADER_AREA;

        java.util.Map<String, double[]> matchPositions = new java.util.HashMap<>();

        StringBuilder boxes = new StringBuilder();
        StringBuilder lines = new StringBuilder();

        // Layout main bracket
        if (mainRounds > 0) {
            layoutBracket(mainBracket, centerX, contentHeight, true, matchPositions, boxes, lines);
        }

        // Layout consolation bracket
        if (consolRounds > 0) {
            layoutBracket(consolationBracket, centerX, contentHeight, false, matchPositions, boxes, lines);
        }

        // Cross-bracket connectors
        if (mainRounds > 0 && consolRounds > 0) {
            addCrossConnectors(mainBracket, consolationBracket, centerX, matchPositions, lines);
        }

        return buildPage(tournament, totalWidth, totalHeight, boxes.toString(), lines.toString());
    }

    private void layoutBracket(Bracket bracket, double centerX, double totalHeight,
                               boolean isMain, java.util.Map<String, double[]> matchPositions,
                               StringBuilder boxes, StringBuilder lines) {
        List<Round> rounds = bracket.rounds();
        if (rounds.isEmpty()) return;

        for (int roundIdx = 0; roundIdx < rounds.size(); roundIdx++) {
            Round round = rounds.get(roundIdx);
            int matchCount = round.matchCount();
            double colX = columnX(centerX, roundIdx, isMain);
            double[] yPositions = calcYPositions(roundIdx, matchCount, totalHeight, rounds, matchPositions);

            // Round header
            String label = getRoundLabel(roundIdx, rounds.size());
            double headerX = colX + BOX_W / 2;
            double minY = yPositions[0];
            for (double y : yPositions) minY = Math.min(minY, y);
            String color = isMain ? "#1565c0" : "#c62828";
            boxes.append("<div class='round-label' style='left:").append(px(headerX - 60))
                    .append(";top:").append(px(minY + HEADER_AREA - 20))
                    .append(";color:").append(color).append(";'>").append(escapeHtml(label)).append("</div>");

            for (int i = 0; i < matchCount; i++) {
                Match match = round.matches().get(i);
                double x = colX;
                double y = yPositions[i];
                matchPositions.put(match.id().value(), new double[]{x, y});
                boxes.append(renderMatchBox(match, x, y + HEADER_AREA));
            }

            // Connectors to previous round
            if (roundIdx > 0) {
                Round prevRound = rounds.get(roundIdx - 1);
                for (int i = 0; i < matchCount; i++) {
                    int f1 = i * 2, f2 = i * 2 + 1;
                    if (f1 >= prevRound.matchCount()) continue;

                    double curCY = yPositions[i] + BOX_H / 2 + HEADER_AREA;
                    double curEdgeX = isMain ? colX : colX + BOX_W;
                    double midX = isMain ? colX - COL_GAP / 2 : colX + BOX_W + COL_GAP / 2;

                    double[] p1 = matchPositions.get(prevRound.matches().get(f1).id().value());
                    if (p1 == null) continue;
                    double f1CY = p1[1] + BOX_H / 2 + HEADER_AREA;
                    double f1EdgeX = isMain ? p1[0] + BOX_W : p1[0];

                    svgLine(lines, f1EdgeX, f1CY, midX, f1CY);
                    svgLine(lines, midX, curCY, curEdgeX, curCY);

                    if (f2 < prevRound.matchCount()) {
                        double[] p2 = matchPositions.get(prevRound.matches().get(f2).id().value());
                        if (p2 != null) {
                            double f2CY = p2[1] + BOX_H / 2 + HEADER_AREA;
                            svgLine(lines, f1EdgeX, f2CY, midX, f2CY);
                            svgLine(lines, midX, f1CY, midX, f2CY);
                        }
                    }
                }
            }
        }
    }

    private void addCrossConnectors(Bracket mainBracket, Bracket consolationBracket,
                                    double centerX, java.util.Map<String, double[]> matchPositions,
                                    StringBuilder lines) {
        Round mainR1 = mainBracket.rounds().get(0);
        Round consolR1 = consolationBracket.rounds().get(0);
        double midX = centerX - COL_GAP / 2;

        for (int i = 0; i < consolR1.matchCount(); i++) {
            int f1 = i * 2, f2 = i * 2 + 1;
            if (f1 >= mainR1.matchCount()) continue;

            double[] cp = matchPositions.get(consolR1.matches().get(i).id().value());
            double[] mp1 = matchPositions.get(mainR1.matches().get(f1).id().value());
            if (cp == null || mp1 == null) continue;

            double consolCY = cp[1] + BOX_H / 2 + HEADER_AREA;
            double f1CY = mp1[1] + BOX_H / 2 + HEADER_AREA;

            svgLine(lines, centerX, f1CY, midX, f1CY);
            svgLine(lines, midX, consolCY, centerX - COL_GAP, consolCY);

            if (f2 < mainR1.matchCount()) {
                double[] mp2 = matchPositions.get(mainR1.matches().get(f2).id().value());
                if (mp2 != null) {
                    double f2CY = mp2[1] + BOX_H / 2 + HEADER_AREA;
                    svgLine(lines, centerX, f2CY, midX, f2CY);
                    svgLine(lines, midX, f1CY, midX, f2CY);
                }
            }
        }
    }

    private String renderMatchBox(Match match, double x, double y) {
        String cssClass = "match-box";
        if (match.isByeMatch()) cssClass += " bye";
        else if (match.isPlayed()) cssClass += " played";
        else if (match.isComplete()) cssClass += " ready";

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='").append(cssClass).append("' style='left:").append(px(x))
                .append(";top:").append(px(y)).append(";'>");

        sb.append("<div class='pair-row").append(isWinner(match, true) ? " winner" : "").append("'>");
        sb.append("<span class='name'>").append(escapeHtml(pairName(match, true))).append("</span>");
        if (match.isPlayed()) {
            sb.append("<span class='score'>").append(escapeHtml(scoreText(match, true))).append("</span>");
        }
        sb.append("</div>");

        sb.append("<div class='separator'></div>");

        sb.append("<div class='pair-row").append(isWinner(match, false) ? " winner" : "").append("'>");
        sb.append("<span class='name'>").append(escapeHtml(pairName(match, false))).append("</span>");
        if (match.isPlayed()) {
            sb.append("<span class='score'>").append(escapeHtml(scoreText(match, false))).append("</span>");
        }
        sb.append("</div>");

        String details = buildDetails(match);
        if (!details.isEmpty()) {
            sb.append("<div class='details'>").append(escapeHtml(details)).append("</div>");
        }

        sb.append("</div>");
        return sb.toString();
    }

    private String buildPage(Tournament tournament, double totalWidth, double totalHeight,
                             String boxes, String lines) {
        String fullTitle = tournament.category() != null && !tournament.category().isBlank()
                ? tournament.name() + " — " + tournament.category()
                : tournament.name();
        String backLink = slugify(tournament.name()) + ".html";
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>" + escapeHtml(fullTitle) + "</title>" +
                "<style>" + bracketCss() + "</style></head><body>" +
                "<div class='header'><h1>" + escapeHtml(fullTitle) + "</h1>" +
                "<a href='" + backLink + "' class='back-link'>← " + escapeHtml(tournament.name()) + "</a></div>" +
                "<div class='bracket-container'>" +
                "<div class='bracket' style='width:" + px(totalWidth) + ";height:" + px(totalHeight) + ";'>" +
                "<svg class='connectors' width='" + totalWidth + "' height='" + totalHeight + "'>" +
                lines + "</svg>" +
                boxes +
                "</div></div></body></html>";
    }

    // --- Helpers ---

    private double columnX(double centerX, int roundIdx, boolean isMain) {
        double offset = roundIdx * (BOX_W + COL_GAP);
        return isMain ? centerX + offset : centerX - offset - BOX_W - COL_GAP;
    }

    private double[] calcYPositions(int roundIdx, int matchCount, double totalHeight,
                                    List<Round> rounds, java.util.Map<String, double[]> existing) {
        double[] yPos = new double[matchCount];
        double available = totalHeight - 2 * PADDING;
        if (roundIdx == 0) {
            double slot = matchCount > 1 ? available / matchCount : available;
            for (int i = 0; i < matchCount; i++) {
                yPos[i] = PADDING + i * slot + (slot - BOX_H) / 2;
            }
        } else {
            Round prev = rounds.get(roundIdx - 1);
            for (int i = 0; i < matchCount; i++) {
                int f1 = i * 2, f2 = i * 2 + 1;
                if (f2 < prev.matchCount()) {
                    double[] p1 = existing.get(prev.matches().get(f1).id().value());
                    double[] p2 = existing.get(prev.matches().get(f2).id().value());
                    yPos[i] = (p1 != null && p2 != null) ? (p1[1] + p2[1]) / 2 : PADDING;
                } else if (f1 < prev.matchCount()) {
                    double[] p1 = existing.get(prev.matches().get(f1).id().value());
                    yPos[i] = p1 != null ? p1[1] : PADDING;
                } else {
                    double slot = matchCount > 1 ? available / matchCount : available;
                    yPos[i] = PADDING + i * slot + (slot - BOX_H) / 2;
                }
            }
        }
        return yPos;
    }

    private void svgLine(StringBuilder sb, double x1, double y1, double x2, double y2) {
        sb.append("<line x1='").append(x1).append("' y1='").append(y1)
                .append("' x2='").append(x2).append("' y2='").append(y2).append("'/>");
    }

    private String getRoundLabel(int roundIdx, int totalRounds) {
        int fromFinal = totalRounds - roundIdx;
        return switch (fromFinal) {
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
            return match.result().map(r -> {
                boolean isWo = (isPair1 && r.walkoverPosition() == 1) || (!isPair1 && r.walkoverPosition() == 2);
                return isWo ? "W.O." : "";
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
            sb.append(dt.value().format(DATE_FMT));
        });
        return sb.toString();
    }

    private String px(double v) { return String.format("%.0fpx", v); }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    static String slugify(String name) {
        return name.toLowerCase()
                .replaceAll("[áàä]", "a").replaceAll("[éèë]", "e")
                .replaceAll("[íìï]", "i").replaceAll("[óòö]", "o")
                .replaceAll("[úùü]", "u").replaceAll("ñ", "n")
                .replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    // --- CSS ---

    private String indexCss() {
        return """
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                   background: #f0f2f5; color: #333; }
            .container { max-width: 800px; margin: 0 auto; padding: 24px; }
            h1 { font-size: 28px; margin-bottom: 24px; color: #1a237e; }
            .category-header { font-size: 20px; color: #1565c0; margin: 24px 0 12px 0;
                              padding-bottom: 6px; border-bottom: 2px solid #e3f2fd; }
            .tournament-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 16px; }
            .tournament-card { display: block; background: white; border-radius: 10px; padding: 20px;
                              text-decoration: none; color: inherit; box-shadow: 0 2px 8px rgba(0,0,0,0.08);
                              transition: transform 0.15s, box-shadow 0.15s; }
            .tournament-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.12); }
            .tournament-card h3 { font-size: 18px; color: #1565c0; margin-bottom: 6px; }
            .tournament-card p { font-size: 14px; color: #666; }
            """;
    }

    private String bracketCss() {
        return """
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                   background: #f0f2f5; }
            .header { background: #1a237e; color: white; padding: 16px 24px; display: flex;
                      align-items: center; gap: 20px; }
            .header h1 { font-size: 22px; }
            .back-link { color: rgba(255,255,255,0.8); text-decoration: none; font-size: 14px; }
            .back-link:hover { color: white; }
            .bracket-container { overflow: auto; padding: 16px; -webkit-overflow-scrolling: touch; }
            .bracket { position: relative; min-width: 100%; }
            .connectors { position: absolute; top: 0; left: 0; pointer-events: none; }
            .connectors line { stroke: #90a4ae; stroke-width: 1.5; }
            .round-label { position: absolute; width: 120px; text-align: center;
                          font-size: 11px; font-weight: 700; text-transform: uppercase; }
            .match-box { position: absolute; width: """ + String.format("%.0f", BOX_W) + """
            px;
                        height: """ + String.format("%.0f", BOX_H) + """
            px;
                        background: white; border: 1.5px solid #bdbdbd; border-radius: 6px;
                        display: flex; flex-direction: column; justify-content: center;
                        padding: 6px 10px; font-size: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
            .match-box.played { border-color: #2e7d32; }
            .match-box.ready { border-color: #1565c0; }
            .match-box.bye { background: #e8e8e8; border-color: #bbb; }
            .pair-row { display: flex; justify-content: space-between; align-items: center;
                       padding: 2px 0; }
            .pair-row.winner .name { font-weight: 700; color: #2e7d32; }
            .name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
            .score { font-size: 11px; color: #555; margin-left: 6px; white-space: nowrap; }
            .separator { height: 1px; background: #e0e0e0; margin: 2px 0; }
            .details { font-size: 10px; color: #888; text-align: center; margin-top: 2px; }
            """;
    }
}
