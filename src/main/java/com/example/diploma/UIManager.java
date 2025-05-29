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
    private static final int CHARACTER_LIMIT = 100;
    private static final double POPUP_WIDTH = 800;
    private static final double POPUP_HEIGHT = 600;
    private static final double SCENE_WIDTH = 500;
    private static final double SCENE_HEIGHT = 400;

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

    private Button createButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("styled-button");
        return button;
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

    private Stage createPopupStage() {
        Stage stage = new Stage();
        stage.setWidth(POPUP_WIDTH);
        stage.setHeight(POPUP_HEIGHT);
        stage.setResizable(false);
        stage.setTitle("Saved Entries");
        return stage;
    }

    private ScrollPane createScrollPane(VBox content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        return scrollPane;
    }

    private VBox createEntryContainer(List<Entry> entries, Stage popupStage) {
        VBox container = new VBox(10);
        container.getStyleClass().add("container");

        if (entries.isEmpty()) {
            Label noEntriesLabel = new Label("No entries yet.");
            noEntriesLabel.getStyleClass().add("no-entries-label");
            container.getChildren().add(noEntriesLabel);
        } else {
            AtomicReference<TextArea> activeEditField = new AtomicReference<>(null);
            for (Entry entry : entries) {
                container.getChildren().add(createEntryRow(entry, activeEditField, popupStage));
            }
        }

        return container;
    }

    private void showEntriesPopup() {
        final Stage popupStage = createPopupStage();
        final List<Entry> entries = dbManager.getAllEntries();
        final VBox container = createEntryContainer(entries, popupStage);
        final ScrollPane scrollPane = createScrollPane(container);
        final Scene scene = new Scene(scrollPane, SCENE_WIDTH, SCENE_HEIGHT);

        loadStylesheet(scene);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void showEntriesPopup2() {
        var popupStage = new Stage();
        popupStage.setWidth(800);
        popupStage.setHeight(600);
        popupStage.setTitle("Saved Entries");

        var container = new VBox(10);
        container.getStyleClass().add("container");

        List<Entry> entries = dbManager.getAllEntries();
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
        // Fields that cannot be edited
        var durationLabel = new Label("⏱ " + entry.getDuration());
        durationLabel.getStyleClass().add("label-duration");
        var createdDateLabel = new Label("Created: " + entry.getDateCreated());
        createdDateLabel.getStyleClass().add("label-created-date");
        var lastModifiedLabel = new Label("Last Modified: " + entry.getLastModified());
        lastModifiedLabel.getStyleClass().add("label-last-modified");
        var nonEditableFields = new VBox(5, durationLabel, createdDateLabel, lastModifiedLabel);
        nonEditableFields.setAlignment(Pos.TOP_LEFT);

        // Description label (display-only with truncation)
        String fullDescription = entry.getDescription() == null ? "" : entry.getDescription();
        var descriptionLabel = new Label(truncate(fullDescription, CHARACTER_LIMIT));
        descriptionLabel.getStyleClass().add("label-entry");

        // Editable description area (no character limit)
        var editArea = new TextArea(fullDescription);
        editArea.setVisible(false);
        editArea.setManaged(false);
        editArea.getStyleClass().add("textarea-edit");
        editArea.setUserData(entry); // So we can fetch it later for the active field logic

        // StackPane to overlay label + TextArea
        var descriptionContainer = new StackPane(descriptionLabel, editArea);
        StackPane.setAlignment(descriptionLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(editArea, Pos.CENTER_LEFT);
        descriptionContainer.setPrefWidth(350);

        // Click label → switch to edit mode
        descriptionLabel.setOnMouseClicked(e -> {
            // Close previously active field if different
            if (activeEditField.get() != null && activeEditField.get() != editArea) {
                TextArea active = activeEditField.get();
                active.setVisible(false);
                active.setManaged(false);
                Entry activeEntry = (Entry) active.getUserData();
                String updatedText = active.getText();
                dbManager.updateDescription(activeEntry.getId(), updatedText);
                active.setText(updatedText);
                Label siblingLabel = (Label) ((StackPane) active.getParent()).getChildren().get(0);
                siblingLabel.setText(truncate(updatedText, CHARACTER_LIMIT));
                siblingLabel.setVisible(true);
            }
            // Show edit field
            descriptionLabel.setVisible(false);
            editArea.setVisible(true);
            editArea.setManaged(true);
            editArea.requestFocus();
            activeEditField.set(editArea);
        });

        // Lose focus → save and switch back to label
        editArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String newDescription = editArea.getText();
                dbManager.updateDescription(entry.getId(), newDescription);
                descriptionLabel.setText(truncate(newDescription, CHARACTER_LIMIT));
                descriptionLabel.setVisible(true);
                editArea.setVisible(false);
                editArea.setManaged(false);
                activeEditField.set(null);
            }
        });

        // Delete entry button
        var deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-delete");
        deleteBtn.setOnAction(e -> {
            dbManager.deleteEntry(entry.getId());
            popupStage.close();
            showEntriesPopup();
        });

        // Full row layout
        var row = new HBox(10, nonEditableFields, descriptionContainer, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("entry-row");
        return row;
    }

    private String truncate(String text, int limit) {
        if (text == null) return "";
        return text.length() > limit ? text.substring(0, limit) + "..." : text;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadStylesheet(Scene scene) {
        final URL cssUrl = getClass().getResource("/styles/styles.entryList.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("⚠️ Could not find styles.css!");
        }
    }

}
