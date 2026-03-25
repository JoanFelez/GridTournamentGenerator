package com.gridpadel.ui.dialog;

import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.Round;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class SwapPairDialog {

    public record SwapResult(Pair selectedPair, Pair targetPair) {}

    public static Optional<SwapResult> show(Match currentMatch, Round r1) {
        Dialog<SwapResult> dialog = new Dialog<>();
        dialog.setTitle("Intercambiar pareja");
        dialog.setHeaderText("Selecciona una pareja de este partido y otra con la que intercambiar");

        ButtonType swapButton = new ButtonType("Intercambiar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(swapButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // "From this match" selector
        grid.add(new Label("Pareja a mover:"), 0, 0);
        ComboBox<PairItem> fromCombo = new ComboBox<>();
        if (currentMatch.pair1() != null) {
            fromCombo.getItems().add(new PairItem(currentMatch.pair1()));
        }
        if (currentMatch.pair2() != null) {
            fromCombo.getItems().add(new PairItem(currentMatch.pair2()));
        }
        if (!fromCombo.getItems().isEmpty()) {
            fromCombo.getSelectionModel().selectFirst();
        }
        fromCombo.setPrefWidth(250);
        grid.add(fromCombo, 1, 0);

        // "Swap with" selector — all pairs from OTHER R1 matches (including BYE)
        grid.add(new Label("Intercambiar con:"), 0, 1);
        ComboBox<PairItem> toCombo = new ComboBox<>();
        for (Match m : r1.matches()) {
            if (m.id().equals(currentMatch.id())) continue;
            if (m.pair1() != null) {
                toCombo.getItems().add(new PairItem(m.pair1()));
            }
            if (m.pair2() != null) {
                toCombo.getItems().add(new PairItem(m.pair2()));
            }
        }
        toCombo.setPrefWidth(250);
        grid.add(toCombo, 1, 1);

        // Preview
        Label previewLabel = new Label("");
        previewLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
        previewLabel.setWrapText(true);
        previewLabel.setMaxWidth(350);
        grid.add(previewLabel, 0, 2, 2, 1);

        Runnable updatePreview = () -> {
            PairItem from = fromCombo.getValue();
            PairItem to = toCombo.getValue();
            if (from != null && to != null) {
                previewLabel.setText("Se intercambiará \"" + from + "\" ↔ \"" + to + "\"");
            } else {
                previewLabel.setText("");
            }
        };
        fromCombo.setOnAction(e -> updatePreview.run());
        toCombo.setOnAction(e -> updatePreview.run());

        dialog.getDialogPane().setContent(grid);

        // Disable swap button until both selections are made
        dialog.getDialogPane().lookupButton(swapButton).setDisable(true);
        Runnable checkValid = () -> {
            boolean valid = fromCombo.getValue() != null && toCombo.getValue() != null;
            dialog.getDialogPane().lookupButton(swapButton).setDisable(!valid);
        };
        fromCombo.setOnAction(e -> { updatePreview.run(); checkValid.run(); });
        toCombo.setOnAction(e -> { updatePreview.run(); checkValid.run(); });

        dialog.setResultConverter(button -> {
            if (button == swapButton) {
                PairItem from = fromCombo.getValue();
                PairItem to = toCombo.getValue();
                if (from != null && to != null) {
                    return new SwapResult(from.pair, to.pair);
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static class PairItem {
        final Pair pair;

        PairItem(Pair pair) {
            this.pair = pair;
        }

        @Override
        public String toString() {
            return pair.displayName();
        }
    }
}
