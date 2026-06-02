module com.example.gruppo04 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires mp3agic;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    opens com.example.gruppo04 to javafx.fxml;
    // Allow JavaFX modules to reflectively access model classes (PropertyValueFactory)
    opens com.example.gruppo04.model to javafx.base, javafx.fxml;
    exports com.example.gruppo04;

    exports com.example.gruppo04.view to javafx.graphics;   // per costruire l'Application
    opens   com.example.gruppo04.view to javafx.fxml;       // per iniettare i @FXML + initialize()
}