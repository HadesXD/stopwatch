module com.example.diploma {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.sql;

    opens com.example.diploma to javafx.fxml;
    exports com.example.diploma;
}