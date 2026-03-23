package com.gridpadel.ui.view;

import com.gridpadel.domain.model.Tournament;
import com.gridpadel.ui.component.BracketPane;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.transform.Scale;

public class MainView extends BorderPane {

    private static final double MIN_ZOOM = 0.3;
    private static final double MAX_ZOOM = 2.0;
    private static final double ZOOM_STEP = 0.1;

    private final BracketPane bracketPane;
    private final Group zoomGroup;
    private final Scale scaleTransform;
    private double currentZoom = 1.0;

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

        setCenter(scrollPane);
        setTop(createToolbar());

        getStylesheets().add(getClass().getResource("/css/bracket.css").toExternalForm());
    }

    public void displayTournament(Tournament tournament) {
        bracketPane.renderTournament(tournament);
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
        Label title = new Label("Grid Padel Tournament Generator v0.0.1");
        title.getStyleClass().add("toolbar-title");

        Label zoomInfo = new Label("Ctrl + Scroll to zoom");
        zoomInfo.getStyleClass().add("toolbar-hint");

        HBox toolbar = new HBox(20, title, zoomInfo);
        toolbar.getStyleClass().add("toolbar");
        toolbar.setPadding(new Insets(10, 20, 10, 20));
        return toolbar;
    }
}
