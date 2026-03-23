package com.gridpadel.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class TournamentDialog {

    public record TournamentData(String name) {}

    public static Optional<TournamentData> showCreate() {
        return show("New Tournament", "", false);
    }

    public static Optional<TournamentData> showEdit(String currentName) {
        return show("Edit Tournament", currentName, true);
    }

    private static Optional<TournamentData> show(String title, String currentName, boolean isEdit) {
        Dialog<TournamentData> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(isEdit ? "Edit tournament name" : "Create a new tournament");

        ButtonType actionButton = new ButtonType(isEdit ? "Save" : "Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(actionButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(currentName);
        nameField.setPromptText("Tournament name");
        nameField.setPrefWidth(250);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        nameField.requestFocus();

        dialog.setResultConverter(button -> {
            if (button == actionButton) {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    return new TournamentData(name);
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
