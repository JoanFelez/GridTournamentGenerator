package com.gridpadel.ui.view;

import com.gridpadel.domain.model.Tournament;
import com.gridpadel.ui.component.BracketPane;
import com.gridpadel.ui.controller.TournamentController;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;

import java.util.function.Consumer;

public class MainView extends BorderPane {

    private static final double MIN_ZOOM = 0.3;
    private static final double MAX_ZOOM = 2.0;
    private static final double ZOOM_STEP = 0.1;

    private final BracketPane bracketPane;
    private final Group zoomGroup;
    private final Scale scaleTransform;
    private final Label titleLabel;
    private double currentZoom = 1.0;

    private TournamentController controller;

    public MainView() {
        bracketPane = new BracketPane();
        scaleTransform = new Scale(1, 1);
        zoomGroup = new Group(bracketPane);
        zoomGroup.getTransforms().add(scaleTransform);

        ScrollPane scrollPane = new ScrollPane(zoomGroup);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().add("bracket-scroll");

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                event.consume();
                double delta = event.getDeltaY() > 0 ? ZOOM_STEP : -ZOOM_STEP;
                zoom(delta);
            }
        });

        titleLabel = new Label("Grid Padel Tournament Generator");
        titleLabel.getStyleClass().add("toolbar-title");

        setCenter(scrollPane);
        setTop(createToolbar());

        getStylesheets().add(getClass().getResource("/css/bracket.css").toExternalForm());
    }

    public void setController(TournamentController controller) {
        this.controller = controller;
        controller.setMainView(this);
        bracketPane.setMatchClickHandler(match -> controller.onMatchClicked(match));
    }

    public void displayTournament(Tournament tournament) {
        bracketPane.renderTournament(tournament);
    }

    public void clearDisplay() {
        bracketPane.getChildren().clear();
        titleLabel.setText("Grid Padel Tournament Generator");
    }

    public void updateTitle(String tournamentName) {
        titleLabel.setText("Grid Padel — " + tournamentName);
    }

    public void zoom(double delta) {
        currentZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, currentZoom + delta));
        scaleTransform.setX(currentZoom);
        scaleTransform.setY(currentZoom);
    }

    public void resetZoom() {
        currentZoom = 1.0;
        scaleTransform.setX(1.0);
        scaleTransform.setY(1.0);
    }

    private HBox createToolbar() {
        Button newBtn = createButton("🆕 New", e -> { if (controller != null) controller.createNewTournament(); });
        Button openBtn = createButton("📂 Open", e -> { if (controller != null) controller.openTournamentHistory(); });
        Button saveBtn = createButton("💾 Save", e -> { if (controller != null) controller.saveTournament(); });
        Button editBtn = createButton("✏️ Rename", e -> { if (controller != null) controller.editTournamentName(); });
        Button pairsBtn = createButton("👥 Pairs", e -> { if (controller != null) controller.managePairs(); });
        Button generateBtn = createButton("⚡ Generate", e -> { if (controller != null) controller.generateBracket(); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label zoomInfo = new Label("Ctrl + Scroll to zoom");
        zoomInfo.getStyleClass().add("toolbar-hint");

        HBox toolbar = new HBox(8,
                newBtn, openBtn, saveBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                editBtn, pairsBtn, generateBtn,
                spacer,
                titleLabel,
                spacer(10),
                zoomInfo
        );
        toolbar.getStyleClass().add("toolbar");
        toolbar.setPadding(new Insets(8, 15, 8, 15));
        toolbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return toolbar;
    }

    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.getStyleClass().add("toolbar-button");
        btn.setOnAction(handler);
        return btn;
    }

    private Region spacer(double width) {
        Region s = new Region();
        s.setMinWidth(width);
        return s;
    }
}
