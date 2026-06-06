package com.example.gruppo04.view;

import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.model.LoopStrategy;
import com.example.gruppo04.model.SequentialStrategy;
import com.example.gruppo04.model.ShuffleStrategy;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.controller.TrackController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.stream.Collectors;
import com.example.gruppo04.util.TableColumnFactory;

/**
 * Controller JavaFX del pannello di dettaglio di una playlist.
 * Mostra le tracce contenute nella playlist corrente e permette
 * di aggiungerne o rimuoverne tramite PlaylistController.
 * Implementa CatalogObserver (è un ConcreteObserver) per aggiornarsi automaticamente
 * quando il catalogo cambia.
 */
public class PlaylistDetailViewController implements CatalogObserver {

    /** Tabella che mostra le tracce contenute nella playlist corrente. */
    @FXML private TableView<Track> tableTracks;

    /** Colonna che mostra il titolo di ogni traccia. */
    @FXML private TableColumn<Track, String> colTitle;

    /** Colonna che mostra l'autore di ogni traccia. */
    @FXML private TableColumn<Track, String> colAuthor;

    /** Colonna che mostra l'anno di pubblicazione di ogni traccia. */
    @FXML private TableColumn<Track, Integer> colYear;

    /** Colonna che mostra il genere musicale di ogni traccia. */
    @FXML private TableColumn<Track, String> colGenre;

    /** Colonna che mostra la durata di ogni traccia nel formato mm:ss. */
    @FXML private TableColumn<Track, String> colDuration;

    /** Pulsante per rimuovere la traccia selezionata dalla playlist. Disabilitato se nessuna traccia è selezionata. */
    @FXML private Button btnRemoveTrack;

    /** Label che mostra il nome della playlist corrente. */
    @FXML private Label labelNamePlaylist;

    /** Label che mostra il numero di tracce presenti nella playlist. */
    @FXML private Label labelNumTracks;

    /** Label che mostra la durata totale di tutte le tracce della playlist in minuti. */
    @FXML private Label labelTotalDuration;

    /* Bottone per la Riproduzione Sequenziale */
    @FXML private Button btnPlayAll;

    /** Bottone per la Riproduzione in Shuffle*/
    @FXML private Button btnShuffle;

    /** Bottone per la Riproduzione in Loop*/
    @FXML private Button btnLoop;

    /** Controller MVC delle playlist, usato per aggiungere e rimuovere tracce. */
    private PlaylistController playlistController;

    /** Controller MVC delle tracce, usato per recuperare le tracce disponibili. */
    private TrackController trackController;

    /** Playlist attualmente visualizzata nel pannello. */
    private Playlist currentPlaylist;

    /** Traccia attualmente selezionata nella tabella. */
    private Track selectedTrack;

    /*Playback Controller per la gestione dello stato e della strategia di riproduzione*/
    private PlaybackController playbackController;

    /** Logger per la gestione degli errori di caricamento delle view. */
    private static final Logger logger =
            Logger.getLogger(PlaylistDetailViewController.class.getName());

    /**
     * Chiamato automaticamente da FXMLLoader dopo il caricamento dell'FXML.
     * Configura le colonne e il listener sulla selezione della tabella.
     */
    @FXML
    void initialize() {
        setupColumns();
        // Abilita btnRemoveTrack solo se una traccia è selezionata
        tableTracks.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    selectedTrack = newVal;
                    btnRemoveTrack.setDisable(newVal == null);
                });
    }

    /**
     * Inizializza il pannello con la playlist selezionata.
     * Registra questo controller come Observer del catalogo.
     * Da chiamare dopo il caricamento dell'FXML.
     *
     * @param playlist           la playlist da visualizzare
     * @param playlistController il controller MVC delle playlist
     * @param trackController    il controller MVC delle tracce
     * @param catalog            il catalogo musicale, usato per la registrazione dell'Observer
     * @param playbackController il controller del playback che gestisce stato di riproduzione e strategia
     */
    public void init(Playlist playlist, PlaylistController playlistController,
                     TrackController trackController, MusicCatalog catalog, PlaybackController playbackController) {
        this.currentPlaylist = playlist;
        this.playlistController = playlistController;
        this.playbackController = playbackController;
        this.trackController = trackController;
        catalog.registerObserver(this);
        updateView();
    }

    /**
     * Chiamato dal catalogo quando si verifica un cambiamento.
     * Aggiorna la vista se l'evento riguarda le tracce della playlist corrente
     * o la rimozione di una traccia dal catalogo.
     *
     * @param event l'evento contenente i dettagli del cambiamento
     */
    @Override
    public void onCatalogChanged(CatalogEvent event) {
        switch (event.getType()) {
            case PLAYLIST_TRACK_ADDED:
            case PLAYLIST_TRACK_REMOVED:
            case PLAYLIST_RENAMED:
            case TRACK_REMOVED:
                updateView();
                break;
            default:
                break;
        }
    }


    /**
     * Configura le cellValueFactory di ogni colonna.
     */
    private void setupColumns() {
        TableColumnFactory.setupAllColumns(colTitle, colAuthor, colYear, colGenre, colDuration);

        //per adattare le larghezza delle colonne presenti nella TableView:
        tableTracks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    /**
     * Aggiorna la vista con i dati aggiornati della playlist corrente.
     * Chiamato automaticamente dall'Observer quando il catalogo cambia.
     */
    public void updateView() {
        labelNamePlaylist.setText(currentPlaylist.getName());

        ObservableList<Track> tracks = FXCollections.observableArrayList(
                currentPlaylist.getTracks());

        tableTracks.setItems(tracks);
        labelNumTracks.setText(tracks.size() + " tracce");
        labelTotalDuration.setText(calculateTotalDuration(tracks) + " min");
    }

    /**
     * Gestisce il click su "Aggiungi traccia".
     * Apre il dialog di selezione traccia mostrando solo le tracce
     * del catalogo non ancora presenti nella playlist corrente.
     */
    @FXML
    void handleAddTrack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gruppo04/Views/TrackSelectionView.fxml"));
            VBox root = loader.load();
            TrackSelectionViewController controller = loader.getController();

            List<Track> availableTracks = trackController.getAllTracks().stream()
                    .filter(t -> !currentPlaylist.getTracks().contains(t))
                    .collect(Collectors.toList());

            // Lambda che implementa TrackSelectionListener e definisce
            // il comportamento da adottare con la traccia selezionata:
            // ovvero aggiungerla alla playlist corrente.
            controller.init(availableTracks, track ->
                    playlistController.addTrackToPlaylist(currentPlaylist, track));

            Stage dialog = new Stage();
            dialog.setTitle("Aggiungi traccia");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errore nel caricamento di TrackSelectionView", e);
        }
    }

    /**
     * Gestisce il click su "Rimuovi traccia".
     * Rimuove la traccia selezionata dalla playlist corrente.
     * La vista si aggiorna automaticamente tramite onCatalogChanged.
     * Qui selectedTrack è sempre non null: il bottone è abilitato
     * solo quando una traccia è selezionata nella tabella (vedi initialize).
     */
    @FXML
    void handleRemoveTrack(ActionEvent event) {
        playlistController.removeTrackFromPlaylist(currentPlaylist, selectedTrack);
    }

    /**
     * Calcola la durata totale della playlist in minuti.
     *
     * @param tracks la lista delle tracce
     * @return durata totale in minuti
     */
    private int calculateTotalDuration(ObservableList<Track> tracks) {
        return tracks.stream()
                .mapToInt(Track::getDuration)
                .sum() / 60;
    }

    /**
     * Avvia la riproduzione della playlist in modalità sequenziale.
     */
    @FXML
    private void handlePlayAll() {
        playbackController.play(currentPlaylist, new SequentialStrategy());
    }

    /**
     * Avvia la riproduzione della playlist in modalità shuffle.
     * Le tracce vengono riprodotte in ordine casuale senza ripetizioni.
     */
    @FXML
    private void handleShuffle() {
        playbackController.play(currentPlaylist, new ShuffleStrategy());
    }

    /**
     * Avvia la riproduzione della playlist in modalità loop.
     * Al termine dell'ultima traccia la riproduzione riparte automaticamente.
     */
    @FXML
    private void handleLoop() {
        playbackController.play(currentPlaylist, new LoopStrategy());
    }
}