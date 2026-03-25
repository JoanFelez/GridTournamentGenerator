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
    private final ScrollPane bracketScrollPane;
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
        this.bracketScrollPane = scrollPane;
        scrollPane.setPannable(false);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("bracket-scroll");

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            event.consume();
            if (event.isControlDown()) {
                double delta = event.getDeltaY() * 0.005;
                zoom(delta);
            } else {
                manualScroll(scrollPane, event.getDeltaX(), event.getDeltaY());
            }
        });

        titleLabel = new Label("Grid Padel — Generador de Cuadros");
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
        bracketPane.setTournamentSelectHandler(t -> controller.openTournament(t));
        controller.refreshTournamentList();
    }

    public void displayTournament(Tournament tournament) {
        bracketPane.renderTournament(tournament);
        scrollToFirstRound(tournament);
    }

    private void scrollToFirstRound(Tournament tournament) {
        if (tournament.mainBracket().rounds().isEmpty()) return;

        javafx.application.Platform.runLater(() -> {
            double contentWidth = zoomGroup.getBoundsInParent().getWidth();
            double viewportWidth = bracketScrollPane.getViewportBounds().getWidth();

            if (contentWidth <= viewportWidth) return;

            int consolationRounds = tournament.consolationBracket().rounds().size();
            double r1CenterX;
            if (consolationRounds == 0) {
                r1CenterX = 60 + 110; // PADDING + half box width
            } else {
                r1CenterX = 60 + consolationRounds * (220 + 80) + 80 + 110;
            }
            r1CenterX *= currentZoom;

            double hValue = (r1CenterX - viewportWidth / 2) / (contentWidth - viewportWidth);
            hValue = Math.max(0, Math.min(1, hValue));
            bracketScrollPane.setHvalue(hValue);
        });
    }

    public void clearDisplay() {
        bracketPane.getChildren().clear();
        titleLabel.setText("Grid Padel — Generador de Cuadros");
        if (controller != null) {
            controller.refreshTournamentList();
        }
    }

    public void updateTitle(String tournamentName) {
        titleLabel.setText("Grid Padel — " + tournamentName);
    }

    public void updateTournamentList(List<Tournament> tournaments) {
        tournamentListBox.getChildren().clear();

        if (tournaments.isEmpty()) {
            Label empty = new Label("No hay torneos todavía");
            empty.getStyleClass().add("sidebar-empty");
            tournamentListBox.getChildren().add(empty);
        } else {
            Tournament current = controller != null ? controller.currentTournament() : null;

            // Group by tournament name, with categories as sub-items
            java.util.LinkedHashMap<String, java.util.List<Tournament>> byName = new java.util.LinkedHashMap<>();
            for (Tournament t : tournaments) {
                byName.computeIfAbsent(t.name(), k -> new java.util.ArrayList<>()).add(t);
            }

            for (var entry : byName.entrySet()) {
                java.util.List<Tournament> group = entry.getValue();
                boolean hasCategories = group.stream()
                        .anyMatch(t -> t.category() != null && !t.category().isBlank());

                if (hasCategories && group.size() > 1) {
                    // Tournament header
                    Label header = new Label("🏆 " + entry.getKey());
                    header.getStyleClass().add("sidebar-category");
                    header.setMaxWidth(Double.MAX_VALUE);
                    header.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #1565c0; -fx-padding: 6 0 2 4;");
                    tournamentListBox.getChildren().add(header);

                    for (Tournament t : group) {
                        String label = t.category() != null && !t.category().isBlank()
                                ? t.category() : "(sin categoría)";
                        Label item = new Label(label);
                        item.getStyleClass().add("sidebar-item");
                        item.setMaxWidth(Double.MAX_VALUE);
                        item.setStyle(item.getStyle() + "-fx-padding: 4 4 4 16;");
                        if (current != null && current.id().equals(t.id())) {
                            item.getStyleClass().add("sidebar-item-active");
                        }
                        String pairInfo = t.pairCount() + " parejas";
                        if (!t.mainBracket().rounds().isEmpty()) pairInfo += " • cuadro";
                        item.setTooltip(new Tooltip(pairInfo));
                        item.setOnMouseClicked(e -> {
                            if (controller != null) controller.openTournament(t);
                        });
                        tournamentListBox.getChildren().add(item);
                    }
                } else {
                    // Single tournament or no categories — show flat
                    for (Tournament t : group) {
                        String label = t.name();
                        if (t.category() != null && !t.category().isBlank()) {
                            label += " — " + t.category();
                        }
                        Label item = new Label(label);
                        item.getStyleClass().add("sidebar-item");
                        item.setMaxWidth(Double.MAX_VALUE);
                        if (current != null && current.id().equals(t.id())) {
                            item.getStyleClass().add("sidebar-item-active");
                        }
                        String pairInfo = t.pairCount() + " parejas";
                        if (!t.mainBracket().rounds().isEmpty()) pairInfo += " • cuadro";
                        item.setTooltip(new Tooltip(pairInfo));
                        item.setOnMouseClicked(e -> {
                            if (controller != null) controller.openTournament(t);
                        });
                        tournamentListBox.getChildren().add(item);
                    }
                }
            }
        }

        // Show welcome view in center when no tournament is selected
        if (controller == null || controller.currentTournament() == null) {
            bracketPane.showWelcomeView(tournaments);
        }
    }

    public void zoom(double delta) {
        currentZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, currentZoom + delta));
        scaleTransform.setX(currentZoom);
        scaleTransform.setY(currentZoom);
    }

    private void manualScroll(ScrollPane sp, double deltaX, double deltaY) {
        double scaledW = bracketPane.getWidth() * currentZoom;
        double scaledH = bracketPane.getHeight() * currentZoom;
        double vpW = sp.getViewportBounds().getWidth();
        double vpH = sp.getViewportBounds().getHeight();

        if (scaledH > vpH && deltaY != 0) {
            double vStep = deltaY / (scaledH - vpH);
            sp.setVvalue(Math.max(0, Math.min(1, sp.getVvalue() - vStep)));
        }
        if (scaledW > vpW && deltaX != 0) {
            double hStep = deltaX / (scaledW - vpW);
            sp.setHvalue(Math.max(0, Math.min(1, sp.getHvalue() - hStep)));
        }
    }

    public void resetZoom() {
        currentZoom = 1.0;
        scaleTransform.setX(1.0);
        scaleTransform.setY(1.0);
    }

    private VBox createSidebar() {
        Label header = new Label("Torneos");
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
        Button newBtn = createButton("🆕 Nuevo", e -> { if (controller != null) controller.createNewTournament(); });
        Button saveBtn = createButton("💾 Guardar", e -> { if (controller != null) controller.saveTournament(); });
        Button editBtn = createButton("✏️ Renombrar", e -> { if (controller != null) controller.editTournamentName(); });
        Button deleteBtn = createButton("🗑 Eliminar", e -> { if (controller != null) controller.deleteCurrentTournament(); });
        Button pairsBtn = createButton("👥 Parejas", e -> { if (controller != null) controller.managePairs(); });
        Button generateBtn = createButton("⚡ Generar", e -> { if (controller != null) controller.generateBracket(); });
        Button publishBtn = createButton("📤 Publicar", e -> { if (controller != null) controller.publishToGitHubPages(); });
        Button pdfBtn = createButton("📄 Exportar PDF", e -> { if (controller != null) controller.exportPdf(); });

        Button resetZoomBtn = createButton("🔍 Zoom 1:1", e -> resetZoom());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label zoomInfo = new Label("Ctrl + Scroll para zoom");
        zoomInfo.getStyleClass().add("toolbar-hint");

        HBox toolbar = new HBox(8,
                newBtn, saveBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                editBtn, deleteBtn, pairsBtn, generateBtn, pdfBtn, publishBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                resetZoomBtn,
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
