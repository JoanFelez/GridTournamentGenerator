package com.gridpadel.ui.dialog;

import com.gridpadel.domain.model.Tournament;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TournamentHistoryDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public enum Action { OPEN, DELETE }
    public record HistoryResult(Tournament tournament, Action action) {}

    public static Optional<HistoryResult> show(List<Tournament> tournaments) {
        Dialog<HistoryResult> dialog = new Dialog<>();
        dialog.setTitle("Historial de torneos");
        dialog.setHeaderText("Selecciona un torneo para abrir o eliminar");

        ButtonType openButton = new ButtonType("Abrir", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButton = new ButtonType("Eliminar", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(openButton, deleteButton, ButtonType.CANCEL);

        ListView<Tournament> listView = new ListView<>();
        listView.setPrefSize(400, 300);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Tournament item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String catPrefix = item.category() != null && !item.category().isBlank()
                            ? "[" + item.category() + "] " : "";
                    setText(catPrefix + item.name() + "  (" + item.pairCount() + " parejas)  —  " +
                            item.updatedAt().format(FMT));
                }
            }
        });
        listView.getItems().addAll(tournaments);

        if (!tournaments.isEmpty()) {
            listView.getSelectionModel().selectFirst();
        }

        VBox content = new VBox(10, listView);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            Tournament selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return null;
            if (button == openButton) return new HistoryResult(selected, Action.OPEN);
            if (button == deleteButton) return new HistoryResult(selected, Action.DELETE);
            return null;
        });

        return dialog.showAndWait();
    }
}
