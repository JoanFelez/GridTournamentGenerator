package com.gridpadel.ui.dialog;

import com.gridpadel.domain.model.Match;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class MatchDetailsDialog {

    public record MatchDetails(String location, LocalDateTime dateTime) {}

    public static Optional<MatchDetails> show(Match match) {
        Dialog<MatchDetails> dialog = new Dialog<>();
        dialog.setTitle("Match Details");
        dialog.setHeaderText(buildHeaderText(match));

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType clearButton = new ButtonType("Clear", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, clearButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField locationField = new TextField(match.location().map(l -> l.value()).orElse(""));
        locationField.setPromptText("Court name or location");
        locationField.setPrefWidth(250);

        DatePicker datePicker = new DatePicker();
        match.dateTime().ifPresent(dt -> datePicker.setValue(dt.value().toLocalDate()));

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23,
                match.dateTime().map(dt -> dt.value().getHour()).orElse(10));
        hourSpinner.setPrefWidth(70);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59,
                match.dateTime().map(dt -> dt.value().getMinute()).orElse(0), 15);
        minuteSpinner.setPrefWidth(70);

        grid.add(new Label("Location:"), 0, 0);
        grid.add(locationField, 1, 0, 3, 1);
        grid.add(new Label("Date:"), 0, 1);
        grid.add(datePicker, 1, 1, 3, 1);
        grid.add(new Label("Time:"), 0, 2);
        grid.add(hourSpinner, 1, 2);
        grid.add(new Label(":"), 2, 2);
        grid.add(minuteSpinner, 3, 2);

        dialog.getDialogPane().setContent(grid);
        locationField.requestFocus();

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                String loc = locationField.getText().trim();
                LocalDateTime dt = null;
                if (datePicker.getValue() != null) {
                    dt = LocalDateTime.of(datePicker.getValue(),
                            LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue()));
                }
                return new MatchDetails(loc.isEmpty() ? null : loc, dt);
            }
            if (button == clearButton) {
                return new MatchDetails(null, null);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static String buildHeaderText(Match match) {
        String p1 = match.pair1() != null ? match.pair1().displayName() : "TBD";
        String p2 = match.pair2() != null ? match.pair2().displayName() : "TBD";
        return p1 + "  vs  " + p2;
    }
}
