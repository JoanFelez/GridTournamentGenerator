package com.gridpadel.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class PairDialog {

    public record PairData(String player1, String player2, Integer seed) {}

    public static Optional<PairData> showCreate() {
        return show("Add Pair", "", "", null);
    }

    public static Optional<PairData> showEdit(String player1, String player2, Integer seed) {
        return show("Edit Pair", player1, player2, seed);
    }

    private static Optional<PairData> show(String title, String p1, String p2, Integer currentSeed) {
        Dialog<PairData> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Enter player names");

        ButtonType actionButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(actionButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField player1Field = new TextField(p1);
        player1Field.setPromptText("Player 1 name");
        TextField player2Field = new TextField(p2);
        player2Field.setPromptText("Player 2 name");
        TextField seedField = new TextField(currentSeed != null ? String.valueOf(currentSeed) : "");
        seedField.setPromptText("Optional (1, 2, 3...)");

        grid.add(new Label("Player 1:"), 0, 0);
        grid.add(player1Field, 1, 0);
        grid.add(new Label("Player 2:"), 0, 1);
        grid.add(player2Field, 1, 1);
        grid.add(new Label("Seed:"), 0, 2);
        grid.add(seedField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        player1Field.requestFocus();

        dialog.setResultConverter(button -> {
            if (button == actionButton) {
                String name1 = player1Field.getText().trim();
                String name2 = player2Field.getText().trim();
                if (!name1.isEmpty() && !name2.isEmpty()) {
                    Integer seed = parseSeed(seedField.getText().trim());
                    return new PairData(name1, name2, seed);
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static Integer parseSeed(String text) {
        if (text.isEmpty()) return null;
        try {
            int val = Integer.parseInt(text);
            return val > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
