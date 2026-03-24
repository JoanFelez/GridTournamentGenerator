package com.gridpadel.ui.component;

import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.ui.layout.BracketLayout;
import com.gridpadel.ui.layout.BracketLayoutCalculator;
import com.gridpadel.ui.layout.ConnectorLine;
import com.gridpadel.ui.layout.MatchPosition;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;

import java.util.List;
import java.util.function.Consumer;

public class BracketPane extends Pane {

    private final BracketLayoutCalculator layoutCalculator;
    private Map<String, MatchBoxView> matchBoxes = HashMap.empty();
    private Consumer<Match> matchClickHandler;
    private Consumer<Tournament> tournamentSelectHandler;

    public BracketPane() {
        this.layoutCalculator = new BracketLayoutCalculator();
        getStyleClass().add("bracket-pane");
    }

    public void setMatchClickHandler(Consumer<Match> handler) {
        this.matchClickHandler = handler;
    }

    public void setTournamentSelectHandler(Consumer<Tournament> handler) {
        this.tournamentSelectHandler = handler;
    }

    public void showWelcomeView(List<Tournament> tournaments) {
        getChildren().clear();
        matchBoxes = HashMap.empty();

        VBox container = new VBox(20);
        container.setPadding(new Insets(40));
        container.setAlignment(Pos.TOP_CENTER);
        container.setMinWidth(600);

        Label title = new Label("🏸 Grid Padel Tournament Generator");
        title.getStyleClass().add("welcome-title");

        if (tournaments.isEmpty()) {
            Label emptyMsg = new Label("No tournaments yet.\nClick 🆕 New to create your first tournament.");
            emptyMsg.getStyleClass().add("welcome-empty");
            container.getChildren().addAll(title, emptyMsg);
        } else {
            Label subtitle = new Label("Select a tournament to open:");
            subtitle.getStyleClass().add("welcome-subtitle");

            FlowPane grid = new FlowPane(16, 16);
            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10, 0, 0, 0));

            for (Tournament t : tournaments) {
                VBox card = createTournamentCard(t);
                grid.getChildren().add(card);
            }

            container.getChildren().addAll(title, subtitle, grid);
        }

        container.setLayoutX(0);
        container.setLayoutY(0);
        getChildren().add(container);
    }

    private VBox createTournamentCard(Tournament tournament) {
        Label name = new Label(tournament.name());
        name.getStyleClass().add("welcome-card-name");

        String pairsText = tournament.pairCount() + " pair" + (tournament.pairCount() != 1 ? "s" : "");
        boolean hasBracket = !tournament.mainBracket().rounds().isEmpty();
        String statusText = hasBracket ? "📊 Bracket generated" : "📝 Draft";

        Label pairs = new Label("👥 " + pairsText);
        pairs.getStyleClass().add("welcome-card-detail");

        Label status = new Label(statusText);
        status.getStyleClass().add("welcome-card-detail");

        VBox card = new VBox(6, name, pairs, status);
        card.getStyleClass().add("welcome-card");
        card.setPrefWidth(220);
        card.setPadding(new Insets(16));

        card.setOnMouseClicked(e -> {
            if (tournamentSelectHandler != null) {
                tournamentSelectHandler.accept(tournament);
            }
        });

        return card;
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
        java.util.Map<Integer, Double> mainRoundX = new java.util.TreeMap<>();
        java.util.Map<Integer, Double> consolRoundX = new java.util.TreeMap<>();
        int mainTotalRounds = 0;
        int consolTotalRounds = 0;

        for (MatchPosition p : layout.matchPositions()) {
            if (p.isConsolation()) {
                consolRoundX.merge(p.roundNumber(), p.x(), Math::min);
                consolTotalRounds = Math.max(consolTotalRounds, p.roundNumber());
            } else {
                mainRoundX.merge(p.roundNumber(), p.x(), Math::min);
                mainTotalRounds = Math.max(mainTotalRounds, p.roundNumber());
            }
        }

        // Main bracket header over R1
        if (!mainRoundX.isEmpty()) {
            double r1X = mainRoundX.values().iterator().next();
            Label mainLabel = new Label("MAIN BRACKET →");
            mainLabel.getStyleClass().add("bracket-label");
            mainLabel.getStyleClass().add("bracket-label-main");
            mainLabel.setLayoutX(r1X + BracketLayoutCalculator.MATCH_BOX_WIDTH / 2 - 60);
            mainLabel.setLayoutY(5);
            getChildren().add(mainLabel);
        }

        // Consolation bracket header over its R1 (nearest to center)
        if (!consolRoundX.isEmpty()) {
            double maxConsolX = consolRoundX.values().stream().mapToDouble(d -> d).max().orElse(0);
            Label consolLabel = new Label("← CONSOLATION BRACKET");
            consolLabel.getStyleClass().add("bracket-label");
            consolLabel.getStyleClass().add("bracket-label-consolation");
            consolLabel.setLayoutX(maxConsolX + BracketLayoutCalculator.MATCH_BOX_WIDTH / 2 - 80);
            consolLabel.setLayoutY(5);
            getChildren().add(consolLabel);
        }

        // Round headers for main bracket
        for (var entry : mainRoundX.entrySet()) {
            String roundName = roundLabel(entry.getKey(), mainTotalRounds);
            Label lbl = new Label(roundName);
            lbl.getStyleClass().add("round-header");
            lbl.setLayoutX(entry.getValue() + BracketLayoutCalculator.MATCH_BOX_WIDTH / 2 - 30);
            lbl.setLayoutY(22);
            getChildren().add(lbl);
        }

        // Round headers for consolation bracket
        for (var entry : consolRoundX.entrySet()) {
            String roundName = roundLabel(entry.getKey(), consolTotalRounds);
            Label lbl = new Label(roundName);
            lbl.getStyleClass().add("round-header");
            lbl.getStyleClass().add("round-header-consolation");
            lbl.setLayoutX(entry.getValue() + BracketLayoutCalculator.MATCH_BOX_WIDTH / 2 - 30);
            lbl.setLayoutY(22);
            getChildren().add(lbl);
        }
    }

    private String roundLabel(int roundNumber, int totalRounds) {
        int fromFinal = totalRounds - roundNumber;
        return switch (fromFinal) {
            case 0 -> "Final";
            case 1 -> "Semifinal";
            case 2 -> "1/4";
            case 3 -> "1/8";
            case 4 -> "1/16";
            case 5 -> "1/32";
            default -> "R" + roundNumber;
        };
    }
}
