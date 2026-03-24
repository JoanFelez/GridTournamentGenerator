package com.gridpadel.ui.component;

import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.ui.layout.BracketLayout;
import com.gridpadel.ui.layout.BracketLayoutCalculator;
import com.gridpadel.ui.layout.ConnectorLine;
import com.gridpadel.ui.layout.MatchPosition;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.util.function.Consumer;

public class BracketPane extends Pane {

    private final BracketLayoutCalculator layoutCalculator;
    private Map<String, MatchBoxView> matchBoxes = HashMap.empty();
    private Consumer<Match> matchClickHandler;

    public BracketPane() {
        this.layoutCalculator = new BracketLayoutCalculator();
        getStyleClass().add("bracket-pane");
    }

    public void setMatchClickHandler(Consumer<Match> handler) {
        this.matchClickHandler = handler;
    }

    public void renderTournament(Tournament tournament) {
        getChildren().clear();
        matchBoxes = HashMap.empty();

        if (tournament.mainBracket().rounds().isEmpty()) {
            showPairsSummary(tournament);
            return;
        }

        BracketLayout layout = layoutCalculator.calculate(tournament);
        Map<String, Match> matchLookup = buildMatchLookup(tournament);

        for (MatchPosition pos : layout.matchPositions()) {
            Match match = matchLookup.getOrElse(pos.matchId(), (Match) null);
            if (match == null) continue;

            MatchBoxView box = new MatchBoxView(match);
            box.setLayoutX(pos.x());
            box.setLayoutY(pos.y());
            if (matchClickHandler != null) {
                box.setOnMatchClicked(matchClickHandler);
            }
            matchBoxes = matchBoxes.put(pos.matchId(), box);
            getChildren().add(box);
        }

        for (ConnectorLine connector : layout.connectors()) {
            Line line = new Line(connector.startX(), connector.startY(),
                    connector.endX(), connector.endY());
            line.getStyleClass().add("connector-line");
            getChildren().add(line);
        }

        addBracketLabels(layout);
    }

    private void showPairsSummary(Tournament tournament) {
        javafx.scene.layout.VBox summary = new javafx.scene.layout.VBox(8);
        summary.setPadding(new javafx.geometry.Insets(30));
        summary.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        javafx.scene.control.Label title = new javafx.scene.control.Label(
                tournament.name() + " — " + tournament.pairCount() + " pair(s) registered");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        summary.getChildren().add(title);

        if (tournament.pairCount() > 0) {
            javafx.scene.control.Label header = new javafx.scene.control.Label("Registered Pairs:");
            header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555; -fx-padding: 10 0 4 0;");
            summary.getChildren().add(header);

            tournament.pairs().zipWithIndex().forEach(tuple -> {
                var pair = tuple._1;
                int idx = tuple._2.intValue() + 1;
                String seedText = pair.isSeeded() ? " [Seed " + pair.seed().get() + "]" : "";
                javafx.scene.control.Label pairLabel = new javafx.scene.control.Label(
                        idx + ". " + pair.player1Name().value() + " / " + pair.player2Name().value() + seedText);
                pairLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
                summary.getChildren().add(pairLabel);
            });

            javafx.scene.control.Label hint = new javafx.scene.control.Label(
                    "Click ⚡ Generate to create the bracket");
            hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-padding: 12 0 0 0;");
            summary.getChildren().add(hint);
        } else {
            javafx.scene.control.Label hint = new javafx.scene.control.Label(
                    "Click 👥 Pairs to add pairs, then ⚡ Generate to create the bracket");
            hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #888; -fx-padding: 8 0 0 0;");
            summary.getChildren().add(hint);
        }

        summary.setLayoutX(30);
        summary.setLayoutY(30);
        getChildren().add(summary);
    }

    private Map<String, Match> buildMatchLookup(Tournament tournament) {
        return tournament.allMatches()
                .toMap(m -> io.vavr.Tuple.of(m.id().value(), m));
    }

    private void addBracketLabels(BracketLayout layout) {
        boolean hasMain = layout.matchPositions().stream().anyMatch(p -> !p.isConsolation());
        boolean hasConsolation = layout.matchPositions().stream().anyMatch(MatchPosition::isConsolation);

        if (hasMain) {
            javafx.scene.control.Label mainLabel = new javafx.scene.control.Label("MAIN BRACKET →");
            mainLabel.getStyleClass().add("bracket-label");
            mainLabel.getStyleClass().add("bracket-label-main");

            double maxX = layout.matchPositions().stream()
                    .filter(p -> !p.isConsolation())
                    .mapToDouble(MatchPosition::x)
                    .max().orElse(0);
            mainLabel.setLayoutX(maxX + BracketLayoutCalculator.MATCH_BOX_WIDTH / 2 - 60);
            mainLabel.setLayoutY(5);
            getChildren().add(mainLabel);
        }

        if (hasConsolation) {
            javafx.scene.control.Label consolLabel = new javafx.scene.control.Label("← CONSOLATION");
            consolLabel.getStyleClass().add("bracket-label");
            consolLabel.getStyleClass().add("bracket-label-consolation");

            double minX = layout.matchPositions().stream()
                    .filter(MatchPosition::isConsolation)
                    .mapToDouble(MatchPosition::x)
                    .min().orElse(0);
            consolLabel.setLayoutX(minX + BracketLayoutCalculator.MATCH_BOX_WIDTH / 2 - 60);
            consolLabel.setLayoutY(5);
            getChildren().add(consolLabel);
        }
    }
}
