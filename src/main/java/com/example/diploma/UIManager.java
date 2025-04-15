package com.example.diploma;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class UIManager {
    private Label timerLabel;
    private Stopwatch stopwatch;
    private DatabaseManager dbManager = new DatabaseManager();

    public VBox createUI() {
        // Timer label
        timerLabel = new Label("00:00:00");
        timerLabel.setFont(new Font("Segoe UI", 32));
        timerLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Stopwatch setup
        stopwatch = new Stopwatch(() -> timerLabel.setText(stopwatch.getElapsedTime()));

        // Buttons
        Button startButton = createStyledButton("Start");
        Button stopButton = createStyledButton("Stop");
        Button saveButton = createStyledButton("Save Entry");
        saveButton.setDisable(true);

        // TextArea
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Add a short description...");
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle("""
        -fx-font-family: 'Segoe UI';
        -fx-font-size: 14px;
        -fx-border-radius: 10px;
        -fx-background-radius: 10px;
        -fx-background-color: #ecf0f1;
        -fx-border-color: transparent;
        -fx-padding: 10px;
    """);

        // Event handlers
        startButton.setOnAction(e -> stopwatch.start());
        stopButton.setOnAction(e -> {
            stopwatch.stop();
            saveButton.setDisable(false);
        });
        saveButton.setOnAction(e -> {
            dbManager.saveEntry(timerLabel.getText(), descriptionArea.getText());
            saveButton.setDisable(true);
        });

        // Layout
        VBox layout = new VBox(15, timerLabel, startButton, stopButton, descriptionArea, saveButton);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("""
        -fx-background-color: #f7f9fa;
        -fx-padding: 40px;
    """);

        return layout;
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("""
        -fx-font-family: 'Segoe UI';
        -fx-font-size: 14px;
        -fx-text-fill: white;
        -fx-background-color: #3498db;
        -fx-background-radius: 8px;
        -fx-border-radius: 8px;
        -fx-padding: 8px 16px;
    """);

        button.setOnMouseEntered(e -> button.setStyle("""
        -fx-font-family: 'Segoe UI';
        -fx-font-size: 14px;
        -fx-text-fill: white;
        -fx-background-color: #2980b9;
        -fx-background-radius: 8px;
        -fx-border-radius: 8px;
        -fx-padding: 8px 16px;
    """));

        button.setOnMouseExited(e -> button.setStyle("""
        -fx-font-family: 'Segoe UI';
        -fx-font-size: 14px;
        -fx-text-fill: white;
        -fx-background-color: #3498db;
        -fx-background-radius: 8px;
        -fx-border-radius: 8px;
        -fx-padding: 8px 16px;
    """));

        return button;
    }


}
