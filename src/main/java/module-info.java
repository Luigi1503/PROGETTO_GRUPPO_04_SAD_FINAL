module com.example.gruppo04 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires mp3agic;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    requires java.logging;
    requires java.sql;
    requires jlayer;

    opens com.example.gruppo04 to javafx.fxml;
    opens com.example.gruppo04.model to javafx.base, javafx.fxml;
    opens com.example.gruppo04.controller to javafx.fxml;
    opens com.example.gruppo04.observer to javafx.fxml;
    opens com.example.gruppo04.interfaces to javafx.fxml;
    opens com.example.gruppo04.util to javafx.fxml;

    exports com.example.gruppo04;
    exports com.example.gruppo04.controller;
    exports com.example.gruppo04.model;
    exports com.example.gruppo04.interfaces;
    exports com.example.gruppo04.observer;
    exports com.example.gruppo04.util;
    exports com.example.gruppo04.model.factory_method;

    exports com.example.gruppo04.view to javafx.graphics;
    opens com.example.gruppo04.view to javafx.fxml;
    exports com.example.gruppo04.model.state;
    opens com.example.gruppo04.model.state to javafx.base, javafx.fxml;
    exports com.example.gruppo04.model.strategy;
    opens com.example.gruppo04.model.strategy to javafx.base, javafx.fxml;



}