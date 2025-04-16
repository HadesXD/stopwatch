package com.example.diploma;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UIManager {
    private Label timerLabel;
    private Stopwatch stopwatch;
    private DatabaseManager dbManager = new DatabaseManager();

    public VBox createUI() {
        // Timer label
        timerLabel = new Label("00:00:00");
        timerLabel.setFont(new Font("Segoe UI Semibold", 40));
        timerLabel.setTextFill(Color.web("#2c3e50"));
        timerLabel.setEffect(new DropShadow(4, Color.rgb(0, 0, 0, 0.2)));

        // Stopwatch setup
        stopwatch = new Stopwatch(() -> timerLabel.setText(stopwatch.getElapsedTime()));

        // Buttons
        Button startButton = createStyledButton("Start");
        Button stopButton = createStyledButton("Stop");
        Button saveButton = createStyledButton("Save Entry");
        saveButton.setDisable(true);

        Button viewEntriesButton = createStyledButton("View Entries");
        viewEntriesButton.setOnAction(e -> showEntriesPopup());

        // TextArea
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Add a short description...");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(100);
        descriptionArea.setStyle("""
            -fx-font-family: 'Segoe UI';
            -fx-font-size: 14px;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-background-color: #ffffff;
            -fx-border-color: #d0d7de;
            -fx-padding: 12px;
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
        VBox layout = new VBox(20, timerLabel, startButton, stopButton, descriptionArea, saveButton, viewEntriesButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #f1f4f8, #dfe9f3);
            -fx-border-radius: 20px;
        """);

        return layout;
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("""
            -fx-font-family: 'Segoe UI Semibold';
            -fx-font-size: 15px;
            -fx-text-fill: white;
            -fx-background-color: #3498db;
            -fx-background-radius: 10px;
            -fx-border-radius: 10px;
            -fx-padding: 10px 20px;
            -fx-cursor: hand;
        """);

        button.setOnMouseEntered(e -> button.setStyle("""
            -fx-font-family: 'Segoe UI Semibold';
            -fx-font-size: 15px;
            -fx-text-fill: white;
            -fx-background-color: #2980b9;
            -fx-background-radius: 10px;
            -fx-border-radius: 10px;
            -fx-padding: 10px 20px;
            -fx-cursor: hand;
        """));

        button.setOnMouseExited(e -> button.setStyle("""
            -fx-font-family: 'Segoe UI Semibold';
            -fx-font-size: 15px;
            -fx-text-fill: white;
            -fx-background-color: #3498db;
            -fx-background-radius: 10px;
            -fx-border-radius: 10px;
            -fx-padding: 10px 20px;
            -fx-cursor: hand;
        """));

        return button;
    }

    private void showEntriesPopup() {
        var popupStage = new Stage();
        popupStage.setWidth(800); // Set the width to 800px, for example
        popupStage.setHeight(600); // Set the height as needed
        popupStage.setTitle("Saved Entries");

        var container = new VBox(10);
        container.getStyleClass().add("container");

        // Fetch entries from the database
        List<Entry> entries = dbManager.getAllEntries();

        // Check if there are no entries
        if (entries.isEmpty()) {
            var noEntriesLabel = new Label("No entries yet.");
            noEntriesLabel.getStyleClass().add("no-entries-label");
            container.getChildren().add(noEntriesLabel);
        } else {
            // Create the UI elements for each entry
            AtomicReference<TextArea> activeEditField = new AtomicReference<>(null);
            for (Entry entry : entries) {
                container.getChildren().add(createEntryRow(entry, activeEditField, popupStage));
            }
        }

        // Create a ScrollPane for better scrolling behavior
        var scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        // Create a Scene and add the stylesheet
        var scene = new Scene(scrollPane, 500, 400);
        loadStylesheet(scene);

        // Set up the stage with the scene and show it
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.show();
    }

    private HBox createEntryRow(Entry entry, AtomicReference<TextArea> activeEditField, Stage popupStage) {
        // Create non-editable fields: Duration, Created Date, Last Modified
        var durationLabel = new Label("⏱ " + entry.getDuration());
        durationLabel.getStyleClass().add("label-duration");

        var createdDateLabel = new Label("Created: " + entry.getDateCreated());
        createdDateLabel.getStyleClass().add("label-created-date");

        var lastModifiedLabel = new Label("Last Modified: " + entry.getLastModified());
        lastModifiedLabel.getStyleClass().add("label-last-modified");

        // Group non-editable fields together in a VBox
        var nonEditableFields = new VBox(5, durationLabel, createdDateLabel, lastModifiedLabel);
        nonEditableFields.setAlignment(Pos.TOP_LEFT);

        // Label for description (non-editable initially)
        var descriptionLabel = new Label(entry.getDescription() == null ? "" : entry.getDescription());
        descriptionLabel.getStyleClass().add("label-entry");

        // Create a TextArea for editable description (initially hidden)
        var editArea = new TextArea(entry.getDescription() == null ? "" : entry.getDescription());
        editArea.setVisible(false);
        editArea.setManaged(false);
        editArea.getStyleClass().add("textarea-edit");

        // StackPane to manage description label and edit area
        var descriptionContainer = new StackPane(descriptionLabel, editArea);
        StackPane.setAlignment(descriptionLabel, Pos.CENTER);
        StackPane.setAlignment(editArea, Pos.CENTER);
        descriptionContainer.setPrefWidth(350);

        // When the description label is clicked, switch to editable mode
        descriptionLabel.setOnMouseClicked(e -> {
            if (activeEditField.get() != null && activeEditField.get() != editArea) {
                TextArea active = activeEditField.get();
                Entry activeEntry = (Entry) active.getUserData();
                dbManager.updateDescription(activeEntry.getId(), active.getText());
                Label sibling = (Label) ((StackPane) active.getParent()).getChildren().get(0);
                sibling.setText("⏱ " + activeEntry.getDuration() + " - " + active.getText());
                active.setVisible(false);
                active.setManaged(false);
                sibling.setVisible(true);
            }

            // Hide the label, show the editable TextArea
            descriptionLabel.setVisible(false);
            editArea.setVisible(true);
            editArea.setManaged(true);
            editArea.requestFocus();
            activeEditField.set(editArea);
        });

        // Save the edited description when focus is lost
        editArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String newDescription = editArea.getText();
                dbManager.updateDescription(entry.getId(), newDescription);
                descriptionLabel.setText(newDescription);
                descriptionLabel.setVisible(true);
                editArea.setVisible(false);
                editArea.setManaged(false);
                activeEditField.set(null);
            }
        });

        // Delete button
        var deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("button-delete");
        deleteBtn.setOnAction(e -> {
            dbManager.deleteEntry(entry.getId());
            popupStage.close();
            showEntriesPopup();
        });

        // Create the complete entry row layout
        var row = new HBox(10, nonEditableFields, descriptionContainer, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("entry-row");

        return row;
    }


    private void handleLabelClick(Label label, TextField editField, AtomicReference<TextField> activeEditField) {
        if (activeEditField.get() != null && activeEditField.get() != editField) {
            TextField active = activeEditField.get();
            Entry activeEntry = (Entry) active.getUserData();
            dbManager.updateDescription(activeEntry.getId(), active.getText());
            Label sibling = (Label) ((StackPane) active.getParent()).getChildren().get(0);
            sibling.setText("⏱ " + activeEntry.getDuration() + " - " + active.getText());
            active.setVisible(false);
            active.setManaged(false);
            sibling.setVisible(true);
        }

        label.setVisible(false);
        editField.setVisible(true);
        editField.setManaged(true);
        editField.requestFocus();
        activeEditField.set(editField);
    }

    private void handleEditFieldFocusLost(Entry entry, Label label, TextField editField, AtomicReference<TextField> activeEditField) {
        if (!editField.isFocused()) {
            String newDescription = sanitizeInput(editField.getText());  // Sanitize the input to avoid SQL injection
            dbManager.updateDescription(entry.getId(), newDescription);
            label.setText("⏱ " + entry.getDuration() + " - " + newDescription);
            label.setVisible(true);
            editField.setVisible(false);
            editField.setManaged(false);
            activeEditField.set(null);
        }
    }

    private void deleteEntry(Entry entry, Stage popupStage) {
        dbManager.deleteEntry(entry.getId());
        popupStage.close();
        showEntriesPopup(); // Refresh the popup with the updated entries
    }

    private void loadStylesheet(Scene scene) {
        URL cssUrl = getClass().getResource("/styles/styles.entryList.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("⚠️ Could not find styles.css!");
        }
    }

    private String sanitizeInput(String input) {
        // Remove potentially harmful characters that could lead to SQL injection
        String sanitizedInput = input.replaceAll("[^\\w\\s]", "");
        return sanitizedInput;
    }


}
