package com.example.gruppo04;

import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.model.state.PlaybackState;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import com.example.gruppo04.persistence.AutoSaveObserver;
import com.example.gruppo04.persistence.PersistenceManager;
import com.example.gruppo04.view.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApplication extends Application {
    // ── Crea il catalogo ──────────────────────
    private final MusicCatalog catalog = ConcreteMusicCatalog.getInstance();

    /** Logger per la gestione degli errori. */
    private static final Logger logger =
            Logger.getLogger(MainApplication.class.getName());

    @Override
    public void start(Stage stage) throws IOException {

        // ── Caricamento stato persistente ─────────
        PersistenceManager pm = PersistenceManager.getInstance();
        try {
            pm.load();
            logger.log(Level.INFO, "Stato del catalogo caricato con successo.");
        } catch (IOException | ClassNotFoundException e) {
            // file non esiste — prima volta che si avvia l'app
            logger.log(Level.INFO, "Nessun salvataggio trovato, avvio con catalogo di esempio.");
        }

        // ── Registra Observer per salvataggio automatico ──
        AutoSaveObserver autoSave = new AutoSaveObserver(catalog);
        catalog.registerObserver(autoSave);


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