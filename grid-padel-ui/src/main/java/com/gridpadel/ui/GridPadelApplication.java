package com.gridpadel.ui;

import com.gridpadel.ui.controller.TournamentController;
import com.gridpadel.ui.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
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
        TournamentController controller = springContext.getBean(TournamentController.class);

        MainView mainView = new MainView();
        mainView.setController(controller);

        Scene scene = new Scene(mainView, 1280, 800);

        primaryStage.setTitle("Generador de Torneos de Pádel");
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
