package com.gridpadel.ui.view;

import com.gridpadel.domain.model.Tournament;
import com.gridpadel.ui.component.BracketPane;
import com.gridpadel.ui.controller.TournamentController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;

import java.util.List;
import java.util.function.Consumer;

public class MainView extends BorderPane {

    private static final double MIN_ZOOM = 0.3;
    private static final double MAX_ZOOM = 2.0;
    private static final double ZOOM_STEP = 0.1;

    private final BracketPane bracketPane;
    private final Group zoomGroup;
    private final Scale scaleTransform;
    private final Label titleLabel;
    private final VBox tournamentListBox;
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

        tournamentListBox = new VBox(4);
        VBox sidebar = createSidebar();

        setCenter(scrollPane);
        setTop(createToolbar());
        setLeft(sidebar);

        getStylesheets().add(getClass().getResource("/css/bracket.css").toExternalForm());
    }

    public void setController(TournamentController controller) {
        this.controller = controller;
        controller.setMainView(this);
        bracketPane.setMatchClickHandler(match -> controller.onMatchClicked(match));
        controller.refreshTournamentList();
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

    public void updateTournamentList(List<Tournament> tournaments) {
        tournamentListBox.getChildren().clear();

        if (tournaments.isEmpty()) {
            Label empty = new Label("No tournaments yet");
            empty.getStyleClass().add("sidebar-empty");
            tournamentListBox.getChildren().add(empty);
            return;
        }

        Tournament current = controller != null ? controller.currentTournament() : null;
        for (Tournament t : tournaments) {
            Label item = new Label(t.name());
            item.getStyleClass().add("sidebar-item");
            item.setMaxWidth(Double.MAX_VALUE);
            if (current != null && current.id().equals(t.id())) {
                item.getStyleClass().add("sidebar-item-active");
            }
            String pairInfo = t.pairCount() + " pairs";
            boolean hasBracket = !t.mainBracket().rounds().isEmpty();
            if (hasBracket) pairInfo += " • bracket";
            item.setTooltip(new Tooltip(pairInfo));
            item.setOnMouseClicked(e -> {
                if (controller != null) controller.openTournament(t);
            });
            tournamentListBox.getChildren().add(item);
        }
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

    private VBox createSidebar() {
        Label header = new Label("Tournaments");
        header.getStyleClass().add("sidebar-header");

        ScrollPane listScroll = new ScrollPane(tournamentListBox);
        listScroll.setFitToWidth(true);
        listScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listScroll.getStyleClass().add("sidebar-scroll");
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        VBox sidebar = new VBox(8, header, listScroll);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(200);
        sidebar.setMinWidth(160);
        sidebar.setPadding(new Insets(10));
        return sidebar;
    }

    private HBox createToolbar() {
        Button newBtn = createButton("🆕 New", e -> { if (controller != null) controller.createNewTournament(); });
        Button saveBtn = createButton("💾 Save", e -> { if (controller != null) controller.saveTournament(); });
        Button editBtn = createButton("✏️ Rename", e -> { if (controller != null) controller.editTournamentName(); });
        Button pairsBtn = createButton("👥 Pairs", e -> { if (controller != null) controller.managePairs(); });
        Button generateBtn = createButton("⚡ Generate", e -> { if (controller != null) controller.generateBracket(); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label zoomInfo = new Label("Ctrl + Scroll to zoom");
        zoomInfo.getStyleClass().add("toolbar-hint");

        HBox toolbar = new HBox(8,
                newBtn, saveBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                editBtn, pairsBtn, generateBtn,
                spacer,
                titleLabel,
                spacer(10),
                zoomInfo
        );
        toolbar.getStyleClass().add("toolbar");
        toolbar.setPadding(new Insets(8, 15, 8, 15));
        toolbar.setAlignment(Pos.CENTER_LEFT);
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
