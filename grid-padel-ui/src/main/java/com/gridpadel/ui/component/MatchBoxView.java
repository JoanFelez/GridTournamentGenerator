package com.gridpadel.ui.component;

import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.vo.SetResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class MatchBoxView extends VBox {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final double BOX_WIDTH = 220;
    private static final double BOX_HEIGHT = 80;

    private final Match match;

    public MatchBoxView(Match match) {
        this.match = match;
        getStyleClass().add("match-box");
        setPrefSize(BOX_WIDTH, BOX_HEIGHT);
        setMinSize(BOX_WIDTH, BOX_HEIGHT);
        setMaxSize(BOX_WIDTH, BOX_HEIGHT);
        setPadding(new Insets(4, 8, 4, 8));
        setSpacing(2);

        applyCssClasses();
        buildContent();
    }

    public Match getMatch() {
        return match;
    }

    private void applyCssClasses() {
        if (match.isByeMatch()) {
            getStyleClass().add("match-bye");
        } else if (match.isPlayed()) {
            getStyleClass().add("match-played");
        } else if (match.isComplete()) {
            getStyleClass().add("match-ready");
        } else {
            getStyleClass().add("match-pending");
        }
    }

    private void buildContent() {
        HBox pair1Row = createPairRow(pairName(true), isWinner(true));
        HBox pair2Row = createPairRow(pairName(false), isWinner(false));

        getChildren().addAll(pair1Row, createSeparator(), pair2Row);

        String details = buildDetailsText();
        if (!details.isEmpty()) {
            Label detailsLabel = new Label(details);
            detailsLabel.getStyleClass().add("match-details");
            getChildren().add(detailsLabel);
        }
    }

    private HBox createPairRow(String name, boolean winner) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("pair-name");
        if (winner) {
            nameLabel.getStyleClass().add("pair-winner");
        }

        HBox row = new HBox(nameLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        if (match.isPlayed()) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label scoreLabel = new Label(buildScoreText(winner));
            scoreLabel.getStyleClass().add("match-score");
            row.getChildren().addAll(spacer, scoreLabel);
        }

        return row;
    }

    private Region createSeparator() {
        Region sep = new Region();
        sep.getStyleClass().add("match-separator");
        sep.setPrefHeight(1);
        sep.setMaxHeight(1);
        return sep;
    }

    private String pairName(boolean isPair1) {
        var pair = isPair1 ? match.pair1() : match.pair2();
        if (pair == null) return "TBD";
        if (pair.isBye()) return "BYE";
        return pair.displayName();
    }

    private boolean isWinner(boolean isPair1) {
        if (!match.isPlayed()) return false;
        return match.winner()
                .map(w -> isPair1 ? w.equals(match.pair1()) : w.equals(match.pair2()))
                .orElse(false);
    }

    private String buildScoreText(boolean isPair1) {
        return match.result()
                .map(r -> r.sets().stream()
                        .map(s -> isPair1
                                ? String.valueOf(s.pair1Games())
                                : String.valueOf(s.pair2Games()))
                        .collect(Collectors.joining(" ")))
                .orElse("");
    }

    private String buildDetailsText() {
        StringBuilder sb = new StringBuilder();
        match.location().ifPresent(loc -> sb.append("📍 ").append(loc.value()));
        match.dateTime().ifPresent(dt -> {
            if (!sb.isEmpty()) sb.append("  ");
            sb.append("📅 ").append(dt.value().format(DATE_FMT));
        });
        return sb.toString();
    }
}
