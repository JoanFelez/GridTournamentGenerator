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
