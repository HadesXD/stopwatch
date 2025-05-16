package com.example.diploma;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UIManager {
    private ComboBox<String> filterDropdown;
    private Label timerLabel;
    private Stopwatch stopwatch;
    private DatabaseManager dbManager = new DatabaseManager();

    public VBox createUI() {
        timerLabel = createTimerLabel();
        stopwatch = new Stopwatch(() -> timerLabel.setText(stopwatch.getElapsedTime()));

        TextArea descriptionArea = createDescriptionArea();
        HBox filterBox = createFilterBox(); // now returns HBox with center alignment
        VBox centerContent = createControlButtons(descriptionArea);

        BorderPane layout = new BorderPane();
        layout.setTop(filterBox);        // ✅ Centered HBox goes here
        layout.setCenter(centerContent);

        return new VBox(layout);
    }

    private Label createTimerLabel() {
        Label label = new Label("00:00:00");
        label.getStyleClass().add("timer-label");
        return label;
    }

    private TextArea createDescriptionArea() {
        TextArea area = new TextArea();
        area.setPromptText("Add a short description...");
        area.setWrapText(true);
        area.setPrefHeight(100);
        area.getStyleClass().add("textarea-description");
        return area;
    }

    private HBox createFilterBox() {
        // Create and populate the dropdown
        filterDropdown = new ComboBox<>();
        filterDropdown.getItems().addAll(dbManager.getAllFilters());
        filterDropdown.setPromptText("Filter");
        filterDropdown.setPrefWidth(150);
        filterDropdown.getStyleClass().add("combo-filter");

        // Create the '+' add filter button
        Button addButton = new Button("+");
        addButton.getStyleClass().add("button-add");
        addButton.setOnAction(e -> showAddFilterDialog());

        // Layout container for dropdown and add button
        HBox hbox = new HBox(10, filterDropdown, addButton);
        hbox.setAlignment(Pos.CENTER); // ✅ Center horizontally
        hbox.setPadding(new Insets(10));

        return hbox;
    }

    private void showAddFilterDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Filter");
        dialog.setHeaderText("Add a new filter option");
        dialog.setContentText("Filter name:");

        dialog.showAndWait().ifPresentOrElse(name -> {
            String trimmed = name.trim();
            if (!trimmed.isEmpty() && !filterDropdown.getItems().contains(trimmed)) {
                boolean saved = dbManager.saveFilter(trimmed);
                if (saved) {
                    filterDropdown.getItems().add(trimmed);
                    filterDropdown.getSelectionModel().select(trimmed);
                } else {
                    showAlert("Filter not saved", "This filter may already exist.");
                }
            }
        }, () -> System.out.println("Add filter dialog was cancelled."));
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("styled-button");
        return button;
    }

    private VBox createControlButtons(TextArea descriptionArea) {
        Button startButton = createButton("Start");
        Button stopButton = createButton("Stop");
        Button saveButton = createButton("Save Entry");
        Button viewEntriesButton = createButton("View Entries");

        saveButton.setDisable(true);
        viewEntriesButton.setOnAction(e -> showEntriesPopup());

        startButton.setOnAction(e -> {
            if (!stopwatch.isRunning()) {
                stopwatch.start();
                saveButton.setDisable(true);
            }
        });

        stopButton.setOnAction(e -> {
            stopwatch.stop();
            saveButton.setDisable(false);
        });

        saveButton.setOnAction(e -> {
            String selectedFilter = filterDropdown.getSelectionModel().getSelectedItem();
            if (selectedFilter == null) {
                System.out.println("⚠️ Please select a filter before saving.");
                return;
            }

            dbManager.saveEntry(selectedFilter, timerLabel.getText(), descriptionArea.getText());
            saveButton.setDisable(true);
        });

        VBox vbox = new VBox(20, timerLabel, startButton, stopButton, descriptionArea, saveButton, viewEntriesButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(40));
        vbox.getStyleClass().add("center-content");
        return vbox;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
