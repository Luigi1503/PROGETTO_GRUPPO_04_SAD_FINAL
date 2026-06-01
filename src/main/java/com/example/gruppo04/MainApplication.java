package com.example.gruppo04;

import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.model.TrackImpl;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import com.example.gruppo04.view.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // ── Crea il catalogo ──────────────────────
        ConcreteMusicCatalog catalog = new ConcreteMusicCatalog();

        // ── Libreria iniziale di esempio ──────────
        catalog.addTrack(new TrackImpl("Bohemian Rhapsody", "Queen",     "Rock",      1975, 354, null));
        catalog.addTrack(new TrackImpl("Hotel California",  "Eagles",    "Rock",      1977, 391, null));
        catalog.addTrack(new TrackImpl("Clair de Lune",     "Debussy",   "Classical", 1905, 300, null));
        catalog.addTrack(new TrackImpl("Midnight City",     "M83",       "Electronic",2011, 243, null));
        catalog.addTrack(new TrackImpl("Teardrop",          "Massive Attack", "Trip-Hop", 1998, 330, null));

        // ── Crea playlist di esempio ──────────────
        catalog.createPlaylist("Rock Classics");
        catalog.createPlaylist("Late Night Vibes");

        // ── Crea i controller ─────────────────────
        TrackController trackController       = new TrackController(catalog);
        PlaylistController playlistController = new PlaylistController(catalog);

        // ── Carica MainView ───────────────────────
        FXMLLoader loader = new FXMLLoader(
                MainApplication.class.getResource("/com/example/gruppo04/Views/MainView.fxml"));
        BorderPane root = loader.load();
        MainViewController controller = loader.getController();
        controller.init(catalog, trackController, playlistController);

        // ── Avvia la finestra ─────────────────────
        stage.setTitle("Music Playlist Manager");
        stage.setScene(new Scene(root, 1100, 700));
        stage.show();
    }
}