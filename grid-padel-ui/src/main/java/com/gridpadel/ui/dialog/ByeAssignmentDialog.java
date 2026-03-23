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
            alert.setTitle("No BYEs Needed");
            alert.setHeaderText(null);
            alert.setContentText("The number of pairs exactly fills the bracket. No BYEs are needed.");
            alert.showAndWait();
            return Optional.of(Set.of());
        }

        Dialog<Set<Pair>> dialog = new Dialog<>();
        dialog.setTitle("Assign BYEs");
        dialog.setHeaderText("Select " + byeCount + " pair(s) to receive a BYE (automatic pass to round 2)");

        ButtonType okButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Map<CheckBox, Pair> checkboxMap = new LinkedHashMap<>();
        for (Pair pair : pairs) {
            String label = pair.displayName();
            if (pair.seed().isPresent()) {
                label += "  [Seed " + pair.seed().get() + "]";
            }
            CheckBox cb = new CheckBox(label);
            checkboxMap.put(cb, pair);
            content.getChildren().add(cb);
        }

        Label infoLabel = new Label("0 of " + byeCount + " BYEs assigned");
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
        content.getChildren().addFirst(infoLabel);

        for (CheckBox cb : checkboxMap.keySet()) {
            cb.setOnAction(e -> {
                long selected = checkboxMap.keySet().stream().filter(CheckBox::isSelected).count();
                infoLabel.setText(selected + " of " + byeCount + " BYEs assigned");
                if (selected > byeCount) {
                    cb.setSelected(false);
                    infoLabel.setText(byeCount + " of " + byeCount + " BYEs assigned (max reached)");
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
