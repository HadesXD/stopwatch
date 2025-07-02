package com.example.diploma;

import com.example.diploma.UIManager.LoginUI;
import com.example.diploma.UIManager.UIManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.net.URL;

public class Main extends Application {

    private static String theUsername = "";

    @Override
    public void start(Stage loginStage) {
        var loginUI = new LoginUI(loggedInUsername -> {
            loginStage.close();
            theUsername = loggedInUsername;
            launchMainUI(loggedInUsername);
        });

        VBox layout = loginUI.createUI();

        var loginScene = new Scene(layout, 300, 275);
        loginScene.getStylesheets().add(getClass().getResource("/styles/styles.entryList.css").toExternalForm());
        loginStage.setScene(loginScene);
        loginStage.setTitle("Login");
        loginStage.show();
    }

    private void launchMainUI(String username) {
        var mainStage = new Stage();
        var uiManager = new UIManager();
        VBox layout = uiManager.createUI(username);

        var mainScene = new Scene(layout, 400, 550);
        mainScene.getStylesheets().add(getClass().getResource("/styles/styles.entryList.css").toExternalForm());
        mainStage.setScene(mainScene);
        mainStage.setTitle("Stopwatch Tracker");
        mainStage.show();

        setupTray(mainStage);
    }

    private static final UIManager uiManager = new UIManager(); // Reuse same UIManager
    private Stage currentStage = null;

    private void setupTray(Stage stage) {
        if (!SystemTray.isSupported()) {
            System.err.println("⚠️ System tray not supported on this platform.");
            return;
        }

        Platform.setImplicitExit(false); // Prevent app from exiting when stage is hidden

        try {
            // Load tray icon
            URL iconUrl = getClass().getResource("/icons/stopwatch.png");
            if (iconUrl == null) {
                System.err.println("❌ Could not find tray icon: /icons/stopwatch.png");
                return;
            }
            var trayIconImage = ImageIO.read(iconUrl);

            var popup = new PopupMenu();
            var openItem = new MenuItem("Open");
            var exitItem = new MenuItem("Exit");
            var trayIcon = new TrayIcon(trayIconImage, "Stopwatch Tracker", popup);
            trayIcon.setImageAutoSize(true);

            // Restore window logic
            Runnable restoreWindow = () -> Platform.runLater(() -> {
                if (currentStage != null && currentStage.isShowing()) {
                    System.out.println("🔄 Focusing current stage");
                    currentStage.toFront();
                    currentStage.requestFocus();
                    return;
                }

                if (currentStage != null) {
                    System.out.println("🔄 Restoring hidden stage");
                    currentStage.show();
                    currentStage.toFront();
                    currentStage.requestFocus();
                    return;
                }

                System.out.println("🟢 Creating new stage");
                try {
                    var newStage = new Stage();
                    var layout = uiManager.createUI(theUsername);
                    var scene = new Scene(layout, 400, 550);
                    scene.getStylesheets().add(getClass().getResource("/styles/styles.entryList.css").toExternalForm());

                    newStage.setScene(scene);
                    newStage.setTitle("Stopwatch Tracker");

                    newStage.setOnCloseRequest(ev -> {
                        System.out.println("🟡 Stage hidden to tray");
                        ev.consume();
                        newStage.hide(); // ✅ Preserve state (e.g. stopwatch)
                    });

                    currentStage = newStage;
                    newStage.show();
                } catch (Exception ex) {
                    System.err.println("❌ Failed to create stage: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            openItem.addActionListener(e -> restoreWindow.run());

            trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getButton() == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                        System.out.println("🖱️ Single click tray restore");
                        restoreWindow.run();
                    }
                }
            });

            exitItem.addActionListener(e -> {
                System.out.println("🔴 Tray exit");
                SystemTray.getSystemTray().remove(trayIcon);
                Platform.exit();
                System.exit(0);
            });

            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            SystemTray.getSystemTray().add(trayIcon);
            System.out.println("✅ Tray icon added");

            stage.setOnCloseRequest(e -> {
                System.out.println("🟡 Initial stage hidden to tray");
                e.consume();
                stage.hide(); // ✅ Keeps stopwatch and UIManager alive
            });

            currentStage = stage;

        } catch (Exception e) {
            System.err.println("❌ Tray setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}