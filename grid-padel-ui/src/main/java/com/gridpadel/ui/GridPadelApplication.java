package com.gridpadel.ui;

import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.model.vo.PlayerName;
import com.gridpadel.domain.service.BracketGenerationService;
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
        MainView mainView = new MainView();

        Tournament demo = createDemoTournament();
        mainView.displayTournament(demo);

        Scene scene = new Scene(mainView, 1280, 800);

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

    private Tournament createDemoTournament() {
        Tournament tournament = Tournament.create("Demo Tournament");
        tournament.addPair(Pair.create(PlayerName.of("Carlos"), PlayerName.of("María")));
        tournament.addPair(Pair.create(PlayerName.of("Juan"), PlayerName.of("Ana")));
        tournament.addPair(Pair.create(PlayerName.of("Pedro"), PlayerName.of("Laura")));
        tournament.addPair(Pair.create(PlayerName.of("Luis"), PlayerName.of("Elena")));
        tournament.addPair(Pair.create(PlayerName.of("Miguel"), PlayerName.of("Sara")));
        tournament.addPair(Pair.create(PlayerName.of("David"), PlayerName.of("Lucía")));

        Pair seed1 = tournament.pairs().get(0);
        seed1.assignSeed(1);
        Pair seed2 = tournament.pairs().get(1);
        seed2.assignSeed(2);

        new BracketGenerationService().generateMainBracket(tournament);
        return tournament;
    }
}
