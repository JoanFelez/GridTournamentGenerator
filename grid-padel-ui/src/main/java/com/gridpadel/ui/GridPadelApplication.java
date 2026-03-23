package com.gridpadel.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class GridPadelApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(GridPadelSpringConfig.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage primaryStage) {
        Label placeholder = new Label("Grid Padel Tournament Generator v0.0.1");
        placeholder.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        StackPane root = new StackPane(placeholder);
        Scene scene = new Scene(root, 1024, 768);

        primaryStage.setTitle("Grid Padel Tournament Generator");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }
}
