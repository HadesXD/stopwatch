package com.example.diploma.UIManager;

import com.example.diploma.databaseManager.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LoginUI {
    private final Consumer<String> onLoginSuccess;
    private static final String NO_UNIQUE_CHARACTERS = "[a-zA-Z0-9]*";
    private static final Integer MAX_CHARACTERS = 10;
    private static DatabaseManager databaseManager = new DatabaseManager();

    public LoginUI(Consumer<String> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    public VBox createUI() {
        var usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("input-field");
        restrictInput(usernameField);

        var passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("input-field");
        restrictInput(passwordField);

        var centerContent = createContent(usernameField, passwordField);

        // Set into a layout and return as VBox
        var layout = new BorderPane();
        layout.setCenter(centerContent);
        return new VBox(layout);
    }

    private Button createButton(String text) {
        var button = new Button(text);
        button.getStyleClass().add("styled-button");
        return button;
    }

    private VBox createContent(TextField username, PasswordField password) {
        var inputBox = new VBox(10, username, password);
        inputBox.setAlignment(Pos.CENTER);

        Button loginButton = createButton("Login");
        loginButton.setOnAction(e -> handleLogin(username.getText().trim(), password.getText().trim()));

        Button registerButton = createButton("Register");
        registerButton.setOnAction(e -> handleRegister(username, password));

        var buttonBox = new VBox(10, loginButton, registerButton);
        buttonBox.setAlignment(Pos.CENTER);

        var combinedBox = new VBox(20, inputBox, buttonBox);
        combinedBox.setAlignment(Pos.CENTER);
        combinedBox.setPadding(new Insets(40));
        combinedBox.getStyleClass().add("center-content");
        return combinedBox;
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please enter both a username and password.");
            return;
        }

        if (databaseManager.validateUser(username, password)) {
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(username);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login failed: Incorrect username or password.");
        }
    }


    private void handleRegister(TextField usernameField, PasswordField passwordField) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please enter both a username and password.");
            return;
        }

        try {
            var user = databaseManager.registerUser(username, password);
            if (user) {
                showAlert(Alert.AlertType.INFORMATION, "User registered successfully!");
                usernameField.clear();
                passwordField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "User already exists!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Registration failed: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Registration");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void restrictInput(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(NO_UNIQUE_CHARACTERS) || newValue.length() > MAX_CHARACTERS) {
                field.setText(oldValue);
            }
        });
    }

}
