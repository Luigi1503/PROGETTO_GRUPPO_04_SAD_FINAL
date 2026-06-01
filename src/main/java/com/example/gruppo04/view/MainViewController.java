package com.example.gruppo04.view;

import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Controller JavaFX della finestra principale dell'applicazione.
 * Assembla tutte le view e gestisce la navigazione tra i pannelli
 * tramite la sidebar sinistra. Implementa CatalogObserver per
 * aggiornare la lista delle playlist nella sidebar.
 */
public class MainViewController implements CatalogObserver {

    /** Area centrale che contiene il pannello attualmente visualizzato. */
    @FXML private StackPane contentArea;

    /** Lista delle playlist nella sidebar sinistra. */
    @FXML private VBox sidebarPlaylistList;

    /** Pulsante di navigazione Home. */
    @FXML private Button btnHome;

    /** Pulsante di navigazione All Tracks. */
    @FXML private Button btnAllTracks;

    /** Pulsante di navigazione Playlists. */
    @FXML private Button btnPlaylists;

    /** Catalogo musicale centrale. */
    private MusicCatalog catalog;

    /** Controller MVC delle tracce. */
    private TrackController trackController;

    /** Controller MVC delle playlist. */
    private PlaylistController playlistController;


    /**
     * Inizializza la MainWindow con le dipendenze necessarie.
     * Registra questo controller come Observer del catalogo e
     * carica la vista iniziale (All Tracks).
     * Da chiamare dopo il caricamento dell'FXML.
     *
     * @param catalog            il catalogo musicale
     * @param trackController    il controller MVC delle tracce
     * @param playlistController il controller MVC delle playlist
     */
    public void init(MusicCatalog catalog, TrackController trackController,
                     PlaylistController playlistController) {
        this.catalog = catalog;
        this.trackController = trackController;
        this.playlistController = playlistController;
        catalog.registerObserver(this);
        updateSidebarPlaylists();
        showAllTracks();
    }

    /**
     * Chiamato dal catalogo quando si verifica un cambiamento.
     * Aggiorna la sidebar se cambiano le playlist.
     *
     * @param event l'evento contenente i dettagli del cambiamento
     */
    @Override
    public void onCatalogChanged(CatalogEvent event) {
        switch (event.getType()) {
            case PLAYLIST_ADDED:
            case PLAYLIST_REMOVED:
            case PLAYLIST_RENAMED:
                updateSidebarPlaylists();
                break;
            default:
                break;
        }
    }

    /**
     * Aggiorna la lista delle playlist nella sidebar sinistra.
     */
    private void updateSidebarPlaylists() {
        sidebarPlaylistList.getChildren().clear();
        for (Playlist playlist : catalog.getPlaylists()) {
            Button btn = new Button("♪  " + playlist.getName());
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> showPlaylistDetail(playlist));
            btn.setStyle("-fx-background-color: transparent;" +
                    "-fx-text-fill: #8AABAE;" +
                    "-fx-font-size: 12px;" +
                    "-fx-alignment: CENTER_LEFT;" +
                    "-fx-padding: 8 20 8 20;" +
                    "-fx-cursor: hand;");
            sidebarPlaylistList.getChildren().add(btn);
        }
    }

    /**
     * Mostra il pannello All Tracks nell'area centrale.
     * Usa setControllerFactory in quanto TrackView ha un costruttore con parametri.
     */
    private void showAllTracks() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gruppo04/Views/track_list_view.fxml"));
            Node view = loader.load();
            TrackListViewController controller = loader.getController();
            controller.init(trackController, catalog);
            setContent(view);
            setActiveButton(btnAllTracks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra il pannello Playlists nell'area centrale.
     * Usa setControllerFactory perché PlaylistViewController ha un costruttore con parametri.
     */
    private void showPlaylists() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gruppo04/Views/PlaylistView.fxml"));
            Node view = loader.load();
            PlaylistViewController controller = loader.getController();
            controller.init(playlistController, catalog);
            controller.setOnPlaylistSelected(this::showPlaylistDetail);
            setContent(view);
            setActiveButton(btnPlaylists);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra il pannello di dettaglio di una playlist nell'area centrale.
     *
     * @param playlist la playlist selezionata dall'utente
     */
    private void showPlaylistDetail(Playlist playlist) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gruppo04/Views/PlaylistDetailView.fxml"));
            Node view = loader.load();
            PlaylistDetailViewController controller = loader.getController();
            controller.init(playlist, playlistController, trackController, catalog);
            setContent(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sostituisce il contenuto dell'area centrale con il nodo fornito.
     *
     * @param node il pannello da mostrare
     */
    private void setContent(Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }

    /**
     * Imposta lo stile del bottone attivo nella sidebar.
     *
     * @param active il bottone da evidenziare
     */
    private void setActiveButton(Button active) {
        for (Button btn : new Button[]{btnHome, btnAllTracks, btnPlaylists}) {
            btn.setStyle("-fx-background-color: transparent;" +
                    "-fx-text-fill: #E8EDF0;" +
                    "-fx-font-size: 13px;" +
                    "-fx-alignment: CENTER_LEFT;" +
                    "-fx-padding: 10 20 10 20;" +
                    "-fx-cursor: hand;");
        }
        active.setStyle("-fx-background-color: #1B2A2F;" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: CENTER_LEFT;" +
                "-fx-padding: 10 20 10 20;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;");
    }

    /**
     * Gestisce il click su "Home".
     * TODO: Sprint 2 — mostrare Most Played e Recent Playlists.
     */
    @FXML
    void handleHome(ActionEvent event) {
        // TODO: Sprint 2 — implementare la home page con Most Played
        showAllTracks();
        setActiveButton(btnHome);
    }

    /**
     * Gestisce il click su "All Tracks".
     */
    @FXML
    void handleAllTracks(ActionEvent event) {
        showAllTracks();
    }

    /**
     * Gestisce il click su "Playlists".
     */
    @FXML
    void handlePlaylists(ActionEvent event) {
        showPlaylists();
    }
}
