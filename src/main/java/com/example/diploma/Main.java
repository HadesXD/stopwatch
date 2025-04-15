package com.example.diploma;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        UIManager uiManager = new UIManager();
        VBox layout = uiManager.createUI();

        Scene scene = new Scene(layout, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Stopwatch Tracker");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}