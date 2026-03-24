package com.gridpadel.ui.dialog;

import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.vo.MatchResult;
import com.gridpadel.domain.model.vo.SetResult;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchResultDialog {

    public sealed interface ResultAction permits SaveResult, ClearResult {}
    public record SaveResult(MatchResult result) implements ResultAction {}
    public record ClearResult() implements ResultAction {}

    public static Optional<ResultAction> show(Match match) {
        Dialog<ResultAction> dialog = new Dialog<>();
        dialog.setTitle("Enter Match Result");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType woButton = new ButtonType("Walkover (W.O.)", ButtonBar.ButtonData.LEFT);
        ButtonType clearButton = new ButtonType("Clear Result", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, woButton, clearButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        grid.setAlignment(javafx.geometry.Pos.CENTER);

        Label headerLabel = new Label("Enter the score for each set");
        headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setAlignment(javafx.geometry.Pos.CENTER);
        grid.add(headerLabel, 0, 0, 3, 1);

        String p1Name = match.pair1() != null ? match.pair1().displayName() : "Pair 1";
        String p2Name = match.pair2() != null ? match.pair2().displayName() : "Pair 2";

        Label p1Label = new Label(p1Name);
        p1Label.setStyle("-fx-font-weight: bold;");
        p1Label.setMinWidth(120);
        p1Label.setAlignment(javafx.geometry.Pos.CENTER);
        Label p2Label = new Label(p2Name);
        p2Label.setStyle("-fx-font-weight: bold;");
        p2Label.setMinWidth(120);
        p2Label.setAlignment(javafx.geometry.Pos.CENTER);

        grid.add(new Label(""), 0, 1);
        grid.add(p1Label, 1, 1);
        grid.add(p2Label, 2, 1);

        @SuppressWarnings("unchecked")
        Spinner<Integer>[] p1Spinners = new Spinner[3];
        @SuppressWarnings("unchecked")
        Spinner<Integer>[] p2Spinners = new Spinner[3];
        io.vavr.collection.List<SetResult> existingSets = match.result()
                .filter(r -> !r.isWalkover())
                .map(MatchResult::sets)
                .getOrElse(io.vavr.collection.List.empty());

        for (int i = 0; i < 3; i++) {
            grid.add(new Label("Set " + (i + 1) + ":"), 0, i + 2);

            int p1Val = i < existingSets.size() ? existingSets.get(i).pair1Games() : 0;
            int p2Val = i < existingSets.size() ? existingSets.get(i).pair2Games() : 0;

            p1Spinners[i] = new Spinner<>(0, 7, p1Val);
            p1Spinners[i].setPrefWidth(70);
            p2Spinners[i] = new Spinner<>(0, 7, p2Val);
            p2Spinners[i].setPrefWidth(70);

            GridPane.setHalignment(p1Spinners[i], javafx.geometry.HPos.CENTER);
            GridPane.setHalignment(p2Spinners[i], javafx.geometry.HPos.CENTER);

            grid.add(p1Spinners[i], 1, i + 2);
            grid.add(p2Spinners[i], 2, i + 2);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                List<SetResult> sets = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    int g1 = p1Spinners[i].getValue();
                    int g2 = p2Spinners[i].getValue();
                    if (g1 == 0 && g2 == 0) continue;
                    sets.add(SetResult.of(g1, g2));
                }
                if (sets.isEmpty()) return null;
                return new SaveResult(MatchResult.of(sets));
            }
            if (button == woButton) {
                return showWalkoverDialog(match);
            }
            if (button == clearButton) {
                return new ClearResult();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static ResultAction showWalkoverDialog(Match match) {
        String p1Name = match.pair1() != null ? match.pair1().displayName() : "Pair 1";
        String p2Name = match.pair2() != null ? match.pair2().displayName() : "Pair 2";

        ChoiceDialog<String> woDialog = new ChoiceDialog<>(p1Name, p1Name, p2Name);
        woDialog.setTitle("Walkover");
        woDialog.setHeaderText("Which pair can't play?");
        woDialog.setContentText("Select the pair giving W.O.:");

        return woDialog.showAndWait()
                .map(selected -> {
                    int position = selected.equals(p1Name) ? 1 : 2;
                    return (ResultAction) new SaveResult(MatchResult.walkover(position));
                })
                .orElse(null);
    }

    private static String buildHeaderText(Match match) {
        String p1 = match.pair1() != null ? match.pair1().displayName() : "TBD";
        String p2 = match.pair2() != null ? match.pair2().displayName() : "TBD";
        return p1 + "  vs  " + p2;
    }
}
