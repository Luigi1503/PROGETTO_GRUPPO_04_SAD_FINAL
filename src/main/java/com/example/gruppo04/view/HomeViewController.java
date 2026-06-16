package com.example.gruppo04.view;

import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.factory_method.AutomaticPlaylistService;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller della Home view.
 *
 * <p>Mostra le sezioni "Tracce più ascoltate" e "Playlist più ascoltate",
 * ordinate in modo decrescente per numero di riproduzioni.</p>
 *
 * <p>Si registra come {@link CatalogObserver} per ricevere aggiornamenti
 * in tempo reale dal {@link MusicCatalog} e aggiornare le classifiche.</p>
 */
public class HomeViewController implements CatalogObserver {

    @FXML private ListView<Track> tracksListView;
    @FXML private ListView<Playlist> playlistsListView;

    private MusicCatalog catalog;
    private PlaybackController playbackController;
    private MainViewController mainViewController;
    private AutomaticPlaylistService automaticPlaylistService;

    private final ObservableList<Track> tracksModel = FXCollections.observableArrayList();
    private final ObservableList<Playlist> playlistsModel = FXCollections.observableArrayList();

    /**
     * Inizializza il controller con i controller di dominio e il catalogo musicale.
     *
     * <p>Registra anche questo controller come observer del catalogo e configura
     * le liste della UI.</p>
     *
     * @param trackController controller delle tracce (attualmente non usato direttamente)
     * @param playlistController controller delle playlist (attualmente non usato direttamente)
     * @param playbackController controller responsabile della riproduzione
     * @param catalog catalogo musicale dell’applicazione
     */
    public void init(TrackController trackController,
                     PlaylistController playlistController,
                     PlaybackController playbackController,
                     MusicCatalog catalog,
                     MainViewController mainViewController) {

        this.playbackController = playbackController;
        this.catalog = catalog;
        this.mainViewController = mainViewController;
        this.automaticPlaylistService = AutomaticPlaylistService.getInstance();

        if (this.catalog != null) {
            this.catalog.registerObserver(this);
        }

        configureLists();
        refreshLists();
    }

    /**
     * Configura le ListView della UI:
     * <ul>
     *     <li>Binding con gli ObservableList</li>
     *     <li>Definizione delle celle personalizzate</li>
     *     <li>Gestione del doppio click per avviare la riproduzione</li>
     * </ul>
     */
    private void configureLists() {
        if (tracksListView != null) {
            tracksListView.setItems(tracksModel);

            tracksListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Track item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getTitle() + " — " +
                                item.getAuthor() + " (" +
                                item.getPlayCount() + ")");
                    }
                }
            });

            tracksListView.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2) {
                    Track t = tracksListView.getSelectionModel().getSelectedItem();
                    if (t != null && playbackController != null) {
                        java.util.ArrayList<com.example.gruppo04.interfaces.PlayableSource> q =
                                new java.util.ArrayList<>();
                        q.addAll(catalog.getAllTracks());
                        playbackController.play(q, t, null);
                        if (mainViewController != null) {
                            mainViewController.handleAllTracks(null);
                        }
                    }
                }
            });
        }

        if (playlistsListView != null) {
            playlistsListView.setItems(playlistsModel);

            playlistsListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Playlist item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName() + " (" + item.getPlayCount() + ")");
                    }
                }
            });

            playlistsListView.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2) {
                    Playlist p = playlistsListView.getSelectionModel().getSelectedItem();
                    if (p != null && playbackController != null) {
                        playbackController.playPlaylist(p, null);
                    }
                }
            });
        }
    }

    /**
     * Aggiorna le liste delle "Top Tracks" e "Top Playlists"
     * ordinandole per numero di riproduzioni.
     *
     * <p>Il refresh viene eseguito sul JavaFX Application Thread
     * tramite {@link Platform#runLater(Runnable)}.</p>
     */
    private void refreshLists() {
        if (catalog == null) return;

        List<Track> topTracks = catalog.getAllTracks().stream()
                .sorted(Comparator.comparingInt(Track::getPlayCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<Playlist> allPlaylists = new ArrayList<>(catalog.getPlaylists());
        allPlaylists.addAll(automaticPlaylistService.refresh(catalog));

        List<Playlist> topPlaylists = allPlaylists.stream()
                .sorted(Comparator.comparingInt(Playlist::getPlayCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            tracksModel.setAll(topTracks);
            playlistsModel.setAll(topPlaylists);
        });
    }

    /**
     * Callback invocata quando il catalogo musicale cambia.
     *
     * <p>In risposta a qualsiasi evento rilevante, la vista viene aggiornata
     * ricalcolando le classifiche.</p>
     *
     * @param event evento di modifica del catalogo
     */
    @Override
    public void onCatalogChanged(CatalogEvent event) {
        switch (event.getType()) {
            case TRACK_ADDED:
            case TRACK_REMOVED:
            case TRACK_UPDATED:
            case PLAYLIST_ADDED:
            case PLAYLIST_REMOVED:
            case PLAYLIST_RENAMED:
            case PLAYLIST_CONTENT_CHANGED:
            case PLAYLIST_REORDERED:
            case PLAYLIST_TRACK_ADDED:
            case PLAYLIST_TRACK_REMOVED:
            case PLAYBACK_STARTED:
            case TRACK_CHANGED:
            case PLAYBACK_STOPPED:
            default:
                refreshLists();
                break;
        }
    }
}
