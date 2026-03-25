package com.gridpadel.ui.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class TournamentDialog {

    public record TournamentData(String name, String category) {}

    public static Optional<TournamentData> showCreate() {
        return show("Nuevo torneo", "", "", false);
    }

    public static Optional<TournamentData> showEdit(String currentName, String currentCategory) {
        return show("Editar torneo", currentName, currentCategory, true);
    }

    private static Optional<TournamentData> show(String title, String currentName, String currentCategory, boolean isEdit) {
        Dialog<TournamentData> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(isEdit ? "Editar torneo" : "Crear un nuevo torneo");

        ButtonType actionButton = new ButtonType(isEdit ? "Guardar" : "Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(actionButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(currentName);
        nameField.setPromptText("Nombre del torneo");
        nameField.setPrefWidth(250);

        TextField categoryField = new TextField(currentCategory);
        categoryField.setPromptText("Ej: Masculina, Femenina, Mixta...");
        categoryField.setPrefWidth(250);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Categoría:"), 0, 1);
        grid.add(categoryField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(button -> {
            if (button == actionButton) {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    return new TournamentData(name, categoryField.getText().trim());
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public static Optional<TournamentData> showAddCategory() {
        Dialog<TournamentData> dialog = new Dialog<>();
        dialog.setTitle("Nueva categoría");
        dialog.setHeaderText("Añadir categoría al torneo");

        ButtonType actionButton = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(actionButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField categoryField = new TextField();
        categoryField.setPromptText("Ej: Masculina, Femenina, Mixta...");
        categoryField.setPrefWidth(250);

        grid.add(new Label("Categoría:"), 0, 0);
        grid.add(categoryField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(categoryField::requestFocus);

        dialog.setResultConverter(button -> {
            if (button == actionButton) {
                String cat = categoryField.getText().trim();
                if (!cat.isEmpty()) {
                    return new TournamentData("", cat);
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
