package com.gridpadel.ui.dialog;

import com.gridpadel.domain.model.Pair;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.*;

public class ByeAssignmentDialog {

    public static Optional<Set<Pair>> show(List<Pair> pairs, int totalSlots) {
        int byeCount = totalSlots - pairs.size();
        if (byeCount <= 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No se necesitan BYEs");
            alert.setHeaderText(null);
            alert.setContentText("El número de parejas completa el cuadro. No se necesitan BYEs.");
            alert.showAndWait();
            return Optional.of(Set.of());
        }

        Dialog<Set<Pair>> dialog = new Dialog<>();
        dialog.setTitle("Asignar BYEs");
        dialog.setHeaderText("Selecciona " + byeCount + " pareja(s) que recibirán BYE (pase directo a ronda 2)");

        ButtonType okButton = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Map<CheckBox, Pair> checkboxMap = new LinkedHashMap<>();
        for (Pair pair : pairs) {
            String label = pair.displayName();
            if (pair.seed().isDefined()) {
                label += "  [Cabeza de serie " + pair.seed().get() + "]";
            }
            CheckBox cb = new CheckBox(label);
            checkboxMap.put(cb, pair);
            content.getChildren().add(cb);
        }

        Label infoLabel = new Label("0 de " + byeCount + " BYEs asignados");
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
        content.getChildren().addFirst(infoLabel);

        for (CheckBox cb : checkboxMap.keySet()) {
            cb.setOnAction(e -> {
                long selected = checkboxMap.keySet().stream().filter(CheckBox::isSelected).count();
                infoLabel.setText(selected + " de " + byeCount + " BYEs asignados");
                if (selected > byeCount) {
                    cb.setSelected(false);
                    infoLabel.setText(byeCount + " de " + byeCount + " BYEs asignados (máximo alcanzado)");
                }
            });
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        dialog.getDialogPane().setContent(scrollPane);

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                Set<Pair> byePairs = new LinkedHashSet<>();
                checkboxMap.forEach((cb, pair) -> {
                    if (cb.isSelected()) byePairs.add(pair);
                });
                return byePairs;
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
