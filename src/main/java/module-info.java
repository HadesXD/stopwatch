module com.example.diploma {
    // JavaFX
    requires javafx.controls;

    // Styling (BootstrapFX)
    requires org.kordamp.bootstrapfx.core;
    // System Tray support (AWT)
    requires java.desktop;
    // Database (JDBC)
    requires java.sql;

    // Export public API
    exports com.example.diploma;
    exports com.example.diploma.databaseManager;
    exports com.example.diploma.UIManager;
    exports com.example.diploma.entities;
}