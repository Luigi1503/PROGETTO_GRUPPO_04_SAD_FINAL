package com.example.gruppo04;

import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.model.TrackImpl;
import com.example.gruppo04.model.state.PlaybackState;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import com.example.gruppo04.view.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    // ── Crea il catalogo ──────────────────────
    private final MusicCatalog catalog = ConcreteMusicCatalog.getInstance();

    @Override
    public void start(Stage stage) throws IOException {

        // ── Registra Observer per salvataggio automatico ──
        AutoSaveObserver autoSave = new AutoSaveObserver(catalog);
        catalog.registerObserver(autoSave);

        // ── Caricamento stato persistente ─────────
        PersistenceManager pm = PersistenceManager.getInstance();
        try {
            pm.load();
            logger.log(Level.INFO, "Stato del catalogo caricato con successo.");
        } catch (IOException | ClassNotFoundException e) {
            // file non esiste — prima volta che si avvia l'app
            logger.log(Level.INFO, "Nessun salvataggio trovato, avvio con catalogo di esempio.");

            // ── Libreria iniziale di esempio ──────────
            catalog.addTrack(new TrackImpl("Hold Back The River", "James Bay",    "Rock",      2014, 354, null));
            catalog.addTrack(new TrackImpl("Someday",             "OneRepublic",  "Pop",       2021, 391, null));
            catalog.addTrack(new TrackImpl("As It Was",           "Harry Styles", "Pop",       2022, 300, null));
            catalog.addTrack(new TrackImpl("Levitating",          "Dua Lipa",     "Pop",       2020, 203, null));
            catalog.addTrack(new TrackImpl("Blinding Lights",     "The Weeknd",   "Synth-Pop", 2019, 200, null));

            catalog.createPlaylist("Rock Classics");
            catalog.createPlaylist("Late Night Vibes");
        }


        // ── Crea i controller ─────────────────────
        TrackController trackController       = new TrackController(catalog);
        PlaylistController playlistController = new PlaylistController(catalog);

        PlaybackState state = new PlaybackState();
        PlaybackController playbackController = new PlaybackController(state);

        // ── Carica MainView ───────────────────────
        FXMLLoader loader = new FXMLLoader(
                MainApplication.class.getResource("/com/example/gruppo04/Views/MainView.fxml"));
        BorderPane root = loader.load();
        MainViewController controller = loader.getController();
        controller.init(catalog, trackController, playlistController, playbackController);

        // ── Avvia la finestra ─────────────────────
        stage.setTitle("Music Playlist Manager");
        stage.setScene(new Scene(root, 1100, 700));
        stage.show();
    }
}