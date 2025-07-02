package com.example.diploma.UIManager;

import com.example.diploma.entities.Entry;
import com.example.diploma.Stopwatch;
import com.example.diploma.databaseManager.DatabaseManager;
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

public class UIManager_2 {
    private static final int CHARACTER_LIMIT = 100;
    private static final int FILTER_LIMIT = 30;
    private static final String ERROR_MSG = "Please use only letters, numbers, spaces, dashes or underscores, and keep it under {} characters.";
    private static final double POPUP_WIDTH = 800;
    private static final double POPUP_HEIGHT = 600;
    private static final double SCENE_WIDTH = 500;
    private static final double SCENE_HEIGHT = 400;

    private ComboBox<String> filterDropdown;
    private Label timerLabel;
    private Stopwatch stopwatch;
    private DatabaseManager dbManager = new DatabaseManager();

    /*
    public VBox createUI() {
        timerLabel = createTimerLabel();
        stopwatch = new Stopwatch(() -> timerLabel.setText(stopwatch.getElapsedTime()));

        var descriptionArea = createDescriptionArea();
        var filterBox = createFilterBox();
        var centerContent = createControlButtons(descriptionArea);

        var layout = new BorderPane();
        layout.setTop(filterBox);
        layout.setCenter(centerContent);
        return new VBox(layout);
    }

    private Label createTimerLabel() {
        var label = new Label("00:00:00");
        label.getStyleClass().add("timer-label");
        return label;
    }

    private TextArea createDescriptionArea() {
        var area = new TextArea();
        area.setPromptText("Add a short description...");
        area.setWrapText(true);
        area.setPrefHeight(100);
        area.getStyleClass().add("textarea-description");
        return area;
    }

    private HBox createFilterBox() {
        var filters = dbManager.getAllFilters();
        filterDropdown = new ComboBox<>();
        filterDropdown.getItems().addAll(filters);
        filterDropdown.setPromptText("Filter");
        filterDropdown.setPrefWidth(200);
        filterDropdown.getStyleClass().add("combo-filter");

        // Select first item if available
        if (!filters.isEmpty()) {
            filterDropdown.getSelectionModel().selectFirst();
        }

        var newFilterButton = createButton("New Filter");
        newFilterButton.setOnAction(e -> showAddFilterDialog());

        // Layout container for dropdown and add button
        var hbox = new HBox(10, filterDropdown, newFilterButton);
        hbox.setAlignment(Pos.CENTER); // ✅ Center horizontally
        hbox.setPadding(new Insets(10));
        return hbox;
    }

    private void showAddFilterDialog() {
        var dialog = new TextInputDialog();
        dialog.setTitle("New Filter");
        dialog.setHeaderText("Add a new filter option");
        dialog.setContentText("Filter name:");

        dialog.showAndWait().ifPresentOrElse(name -> {
            var trimmed = name.trim();
            if (!isValid(trimmed, FILTER_LIMIT)) {
                showAlert("Invalid Filter Name", ERROR_MSG);
                return;
            }

            if (!filterDropdown.getItems().contains(trimmed)) {
                boolean saved = dbManager.saveFilter(trimmed);
                if (saved) {
                    filterDropdown.getItems().add(trimmed);
                    filterDropdown.getSelectionModel().select(trimmed);
                } else {
                    showAlert("Filter Not Saved", "This filter may already exist.");
                }
            } else {
                showAlert("Duplicate Filter", "This filter name already exists.");
            }
        }, () -> System.out.println("Add filter dialog was cancelled."));
    }

    private VBox createControlButtons(TextArea descriptionArea) {
        var startButton = createButton("Start");
        var stopButton = createButton("Stop");
        var saveButton = createButton("Save Entry");
        var viewEntriesButton = createButton("View Entries");

        saveButton.setDisable(true);
        filterDropdown.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewEntriesButton.setDisable(newVal == null);
            startButton.setDisable(newVal == null);
            stopButton.setDisable(newVal == null);
        });

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

        saveButton.setOnAction(e -> saveEntry(descriptionArea.getText(), saveButton));
        viewEntriesButton.setOnAction(e -> showEntriesPopup());

        var vbox = new VBox(20, timerLabel, startButton, stopButton, descriptionArea, saveButton, viewEntriesButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(40));
        vbox.getStyleClass().add("center-content");
        return vbox;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("styled-button");
        return button;
    }

    private void saveEntry(String description, Button saveButton) {
        String selectedFilter = filterDropdown.getSelectionModel().getSelectedItem();
        if (selectedFilter == null) {
            System.out.println("⚠️ Please select a filter before saving.");
            return;
        }
        if (!isValid(description.trim(), CHARACTER_LIMIT)) {
            showAlert("Invalid Description", "Description must be under 200 characters and contain only valid text characters (letters, numbers, punctuation).");
            return;
        }

        dbManager.saveEntry(selectedFilter, timerLabel.getText(), description);
        saveButton.setDisable(true);
    }

    private void showEntriesPopup() {
        final Stage popupStage = createPopupStage();
        var selectedFilter = filterDropdown.getSelectionModel().getSelectedItem();
        List<Entry> entries = selectedFilter != null
                ? dbManager.getEntries(selectedFilter)
                : List.of();
        final VBox container = createEntryContainer(entries, popupStage);
        final ScrollPane scrollPane = createScrollPane(container);
        final Scene scene = new Scene(scrollPane, SCENE_WIDTH, SCENE_HEIGHT);

        loadStylesheet(scene);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private Stage createPopupStage() {
        var stage = new Stage();
        stage.setWidth(POPUP_WIDTH);
        stage.setHeight(POPUP_HEIGHT);
        stage.setResizable(false);
        stage.setTitle("Saved Entries");
        return stage;
    }

    private ScrollPane createScrollPane(VBox content) {
        var scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        return scrollPane;
    }

    private VBox createEntryContainer(List<Entry> entries, Stage popupStage) {
        var container = new VBox(10);
        container.getStyleClass().add("container");

        if (entries.isEmpty()) {
            var noEntriesLabel = new Label("No entries yet.");
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
        var fullDescription = entry.getDescription() == null ? "" : entry.getDescription();
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

        var deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("button-delete");
        deleteButton.setOnAction(e -> {
            dbManager.deleteEntry(entry.getId());
            popupStage.close();
            showEntriesPopup();
        });

        var row = new HBox(10, nonEditableFields, descriptionContainer, deleteButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("entry-row");
        return row;
    }

    private String truncate(String text, int limit) {
        if (text == null) return "";
        return text.length() > limit ? text.substring(0, limit) + "..." : text;
    }

    private boolean isValid(String value, int characterLimit) {
        return value.matches("[\\w\\-\\s\\.\\?,!]+") && value.length() <= characterLimit;
    }

    private void showAlert(String title, String message) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
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


     */
}
