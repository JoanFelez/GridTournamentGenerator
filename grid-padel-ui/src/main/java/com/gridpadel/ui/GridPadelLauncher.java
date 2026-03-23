package com.gridpadel.ui;

import javafx.application.Application;

/**
 * Launcher class to avoid JavaFX module issues with Spring Boot fat JARs.
 * This class is NOT a JavaFX Application subclass, so it can be used
 * as the main class in spring-boot-maven-plugin.
 */
public class GridPadelLauncher {

    public static void main(String[] args) {
        Application.launch(GridPadelApplication.class, args);
    }
}
