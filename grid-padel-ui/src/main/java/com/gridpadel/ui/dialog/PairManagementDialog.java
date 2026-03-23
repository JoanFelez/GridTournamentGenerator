package com.gridpadel.ui.dialog;

import com.gridpadel.domain.model.Pair;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PairManagementDialog {

    public record PairEntry(String player1, String player2, Integer seed, boolean isNew) {}

    public static Optional<List<PairEntry>> show(List<Pair> existingPairs) {
        Dialog<List<PairEntry>> dialog = new Dialog<>();
        dialog.setTitle("Manage Pairs");
        dialog.setHeaderText("Add, edit, or remove pairs (max 32)");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        ListView<PairEntry> listView = new ListView<>();
        listView.setPrefSize(450, 350);

        for (Pair p : existingPairs) {
            listView.getItems().add(new PairEntry(
                    p.player1Name().value(), p.player2Name().value(),
                    p.seed().getOrNull(), false));
        }

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PairEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String seedText = item.seed() != null ? " [Seed " + item.seed() + "]" : "";
                    Label label = new Label(item.player1() + " / " + item.player2() + seedText);
                    HBox.setHgrow(label, Priority.ALWAYS);
                    label.setMaxWidth(Double.MAX_VALUE);
                    setGraphic(label);
                }
            }
        });

        Button addBtn = new Button("Add Pair");
        Button editBtn = new Button("Edit");
        Button removeBtn = new Button("Remove");

        addBtn.setOnAction(e -> {
            if (listView.getItems().size() >= 32) {
                showAlert("Maximum of 32 pairs reached.");
                return;
            }
            PairDialog.showCreate().ifPresent(data ->
                    listView.getItems().add(new PairEntry(data.player1(), data.player2(), data.seed(), true)));
        });

        editBtn.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            PairEntry current = listView.getItems().get(idx);
            PairDialog.showEdit(current.player1(), current.player2(), current.seed()).ifPresent(data ->
                    listView.getItems().set(idx, new PairEntry(data.player1(), data.player2(), data.seed(), current.isNew())));
        });

        removeBtn.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx >= 0) listView.getItems().remove(idx);
        });

        HBox buttons = new HBox(10, addBtn, editBtn, removeBtn);
        buttons.setPadding(new Insets(5, 0, 0, 0));

        VBox content = new VBox(10, listView, buttons);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                return new ArrayList<>(listView.getItems());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
