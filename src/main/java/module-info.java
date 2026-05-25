module com.example.gruppo04 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.gruppo04 to javafx.fxml;
    exports com.example.gruppo04;
}