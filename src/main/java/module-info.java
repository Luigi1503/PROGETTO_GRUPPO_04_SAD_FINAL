module com.example.gruppo04 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.gruppo04 to javafx.fxml;
    exports com.example.gruppo04;

    exports com.example.gruppo04.view to javafx.graphics;   // per costruire l'Application
    opens   com.example.gruppo04.view to javafx.fxml;       // per iniettare i @FXML + initialize()
}