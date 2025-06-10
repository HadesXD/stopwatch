package com.example.diploma;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        UIManager uiManager = new UIManager();
        VBox layout = uiManager.createUI();

        Scene scene = new Scene(layout, 400, 550);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.entryList.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Stopwatch Tracker");
        primaryStage.show();

        setupTray(primaryStage); // Enable system tray
    }

    private void setupTray(Stage stage) {
        if (!SystemTray.isSupported()) {
            System.err.println("⚠️ System tray not supported on this platform.");
            return;
        }

        try {
            // Load tray icon image from resources
            InputStream imageStream = getClass().getResourceAsStream("/icons/tray.png");
            System.out.println("Path: " + imageStream);
            if (imageStream == null) {
                System.err.println("❌ Could not find tray icon resource at /icons/tray.png!");
                return;
            }
            BufferedImage trayIconImage = ImageIO.read(imageStream);

            // Create popup menu
            PopupMenu popupMenu = new PopupMenu();

            // Create tray icon
            TrayIcon trayIcon = new TrayIcon(trayIconImage, "Stopwatch Tracker", popupMenu);
            trayIcon.setImageAutoSize(true);

            // Open menu item
            MenuItem openItem = new MenuItem("Open");
            openItem.addActionListener(e -> Platform.runLater(stage::show));

            // Exit menu item
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                SystemTray.getSystemTray().remove(trayIcon);
                Platform.exit();
                System.exit(0);
            });

            popupMenu.add(openItem);
            popupMenu.addSeparator();
            popupMenu.add(exitItem);

            // Show tray icon
            trayIcon.addActionListener(e -> Platform.runLater(stage::show));
            SystemTray.getSystemTray().add(trayIcon);

            // Hide stage on close
            stage.setOnCloseRequest(event -> {
                event.consume();
                stage.hide();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}