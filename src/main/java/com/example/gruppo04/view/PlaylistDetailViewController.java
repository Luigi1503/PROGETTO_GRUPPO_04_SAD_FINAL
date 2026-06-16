package com.example.gruppo04.view;

import com.example.gruppo04.command.CommandManager;
import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.util.TableColumnFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Controller JavaFX della vista di dettaglio di una playlist.
 *
 * <p>Responsabilità principali:</p>
 * <ul>
 *     <li>Gestione della UI della playlist (tabella, bottoni, label)</li>
 *     <li>Aggiunta/rimozione e riordinamento tracce</li>
 *     <li>Interazione con PlaylistController e TrackController</li>
 *     <li>Delegare la logica di playback a PlaylistPlaybackHandler</li>
 *     <li>Ascolto eventi del catalogo (CatalogObserver)</li>
 * </ul>
 */
public class PlaylistDetailViewController implements CatalogObserver {

    /** Tabella delle tracce della playlist corrente */
    @FXML private TableView<Track> tableTracks;

    /** Colonne tabella */
    @FXML private TableColumn<Track, String> colTitle;
    @FXML private TableColumn<Track, String> colAuthor;
    @FXML private TableColumn<Track, Integer> colYear;
    @FXML private TableColumn<Track, String> colGenre;
    @FXML private TableColumn<Track, String> colDuration;

    /** Pulsanti UI */
    @FXML private Button btnRemoveTrack;
    @FXML private Button btnAddTrack;
    @FXML private Button undoBtn;
    @FXML private Button btnMoveUp;
    @FXML private Button btnMoveDown;

    /** Label informativi */
    @FXML private Label labelNamePlaylist;
    @FXML private Label labelNumTracks;
    @FXML private Label labelTotalDuration;

    /** Controller applicativi */
    private PlaylistController playlistController;
    private TrackController trackController;
    private PlaybackController playbackController;

    /** Catalogo musicale */
    private MusicCatalog catalog;

    /** Playlist attualmente visualizzata */
    private Playlist currentPlaylist;

    /** Traccia selezionata nella tabella */
    private Track selectedTrack;

    /** Flag playlist automatica */
    private boolean automaticPlaylist;

    /** Handler dedicato alla logica di playback */
    private PlaylistPlaybackHandler playbackHandler;

    /** Logger per errori UI */
    private static final Logger logger =
            Logger.getLogger(PlaylistDetailViewController.class.getName());

    /**
     * Metodo di inizializzazione chiamato da FXMLLoader.
     * Configura UI, listener e drag & drop.
     */
    @FXML
    void initialize() {
        setupColumns();

        tableTracks.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    selectedTrack = newVal;
                    btnRemoveTrack.setDisable(automaticPlaylist || newVal == null);
                    updateMoveButtonsState();
                });

        setupDragAndDrop();
    }

    /**
     * Configura i cell value factory delle colonne della tabella.
     */
    private void setupColumns() {
        colTitle.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue() != null ? cell.getValue().getTitle() : ""));

        colAuthor.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue() != null ? cell.getValue().getAuthor() : ""));

        colYear.setCellValueFactory(cell -> {
            Track t = cell.getValue();
            return new SimpleIntegerProperty(t != null ? t.getYear() : 0).asObject();
        });

        colGenre.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue() != null ? cell.getValue().getGenre() : ""));

        colDuration.setCellValueFactory(cell -> {
            Track t = cell.getValue();
            if (t == null) return new SimpleStringProperty("");
            int sec = t.getDuration();
            String formatted = String.format("%d:%02d", sec / 60, sec % 60);
            return new SimpleStringProperty(formatted);
        });
    }

    /**
     * Configura drag & drop per il riordinamento delle tracce.
     */
    private void setupDragAndDrop() {
        tableTracks.setRowFactory(tv -> new TableRow<>() {

            {
                setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !isEmpty()) {
                        handlePlayTrack();
                    }
                });

                setOnDragDetected(e -> {
                    if (isEmpty()) return;

                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(getIndex()));
                    db.setContent(content);

                    e.consume();
                });

                setOnDragOver(e -> {
                    if (e.getGestureSource() != this && e.getDragboard().hasString()) {
                        e.acceptTransferModes(TransferMode.MOVE);
                    }
                });

                setOnDragDropped(e -> {
                    Dragboard db = e.getDragboard();
                    if (!db.hasString()) return;

                    int from = Integer.parseInt(db.getString());
                    int to = isEmpty()
                            ? tableTracks.getItems().size() - 1
                            : getIndex();

                    playlistController.moveTrackInPlaylist(currentPlaylist, from, to);
                });
            }
        });
    }

    /**
     * Inizializza il controller con le dipendenze necessarie.
     */
    public void init(Playlist playlist,
                     PlaylistController playlistController,
                     TrackController trackController,
                     MusicCatalog catalog,
                     PlaybackController playbackController) {

        this.currentPlaylist = playlist;
        this.playlistController = playlistController;
        this.trackController = trackController;
        this.catalog = catalog;
        this.playbackController = playbackController;

        this.automaticPlaylist =
                !catalog.getPlaylists().contains(playlist);

        this.playbackHandler = new PlaylistPlaybackHandler(
                playbackController,
                catalog,
                tableTracks,
                currentPlaylist
        );

        catalog.registerObserver(this);

        updateView();
        playbackHandler.syncHighlight();
        setupUndoBinding();
    }

    /**
     * Binding del bottone undo.
     */
    private void setupUndoBinding() {
        CommandManager manager = playlistController.getManagerTrackPlaylist();

        undoBtn.setDisable(!manager.canUndo());

        manager.addCanUndoListener(canUndo -> undoBtn.setDisable(!canUndo));
    }

    /**
     * Aggiorna la vista della playlist.
     */
    public void updateView() {

        labelNamePlaylist.setText(currentPlaylist.getName());

        ObservableList<Track> tracks =
                FXCollections.observableArrayList(currentPlaylist.getTracks());

        tableTracks.setItems(tracks);

        labelNumTracks.setText(tracks.size() + " tracce");
        labelTotalDuration.setText(
                tracks.stream().mapToInt(Track::getDuration).sum() / 60 + " min"
        );
    }

    /**
     * Aggiorna stato bottoni move.
     */
    private void updateMoveButtonsState() {

        if (selectedTrack == null) {
            btnMoveUp.setDisable(true);
            btnMoveDown.setDisable(true);
            return;
        }

        int index = tableTracks.getItems().indexOf(selectedTrack);
        int size = tableTracks.getItems().size();

        btnMoveUp.setDisable(index <= 0);
        btnMoveDown.setDisable(index < 0 || index >= size - 1);
    }

    /**
     * Observer del catalogo musicale.
     */
    @Override
    public void onCatalogChanged(CatalogEvent event) {
        Platform.runLater(() -> {
            playbackHandler.handleEvent(event, this);

            // Aggiorna la vista se l'evento riguarda la playlist corrente
            switch (event.getType()) {
                case PLAYLIST_TRACK_ADDED,
                     PLAYLIST_TRACK_REMOVED,
                     PLAYLIST_REORDERED,
                     PLAYLIST_RENAMED,
                     PLAYLIST_CONTENT_CHANGED -> updateView();
                default -> {}
            }
        });
    }

    /**
     * Aggiunge una traccia alla playlist.
     */
    @FXML
    private void handleAddTrack(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gruppo04/Views/TrackSelectionView.fxml"));

            VBox root = loader.load();
            TrackSelectionViewController controller = loader.getController();

            List<Track> available = trackController.getAllTracks().stream()
                    .filter(t -> !currentPlaylist.getTracks().contains(t))
                    .collect(Collectors.toList());

            controller.init(available,
                    track -> playlistController.addTrackToPlaylist(currentPlaylist, track));

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Errore caricamento TrackSelectionView", ex);
        }
    }

    /**
     * Rimuove la traccia selezionata.
     */
    @FXML
    private void handleRemoveTrack(ActionEvent e) {
        playlistController.removeTrackFromPlaylist(currentPlaylist, selectedTrack);
    }

    /**
     * Gestore per l'undo (richiama il manager dei comandi).
     */
    @FXML
    private void handleUndo() {
        CommandManager manager = playlistController.getManagerTrackPlaylist();
        if (manager != null && manager.canUndo()) {
            manager.undo();
        }
    }

    /**
     * Sposta la traccia selezionata verso l'alto.
     */
    @FXML
    private void handleMoveUp() {
        if (selectedTrack == null) return;
        int idx = tableTracks.getItems().indexOf(selectedTrack);
        if (idx > 0) {
            playlistController.moveTrackInPlaylist(currentPlaylist, idx, idx - 1);
            updateMoveButtonsState();
        }
    }

    /**
     * Sposta la traccia selezionata verso il basso.
     */
    @FXML
    private void handleMoveDown() {
        if (selectedTrack == null) return;
        int idx = tableTracks.getItems().indexOf(selectedTrack);
        int size = tableTracks.getItems().size();
        if (idx >= 0 && idx < size - 1) {
            playlistController.moveTrackInPlaylist(currentPlaylist, idx, idx + 1);
            updateMoveButtonsState();
        }
    }

    /**
     * Avvia riproduzione playlist.
     */
    @FXML
    private void handlePlay() {
        playbackController.play(
                playbackHandler.buildQueue(),
                currentPlaylist,
                null
        );
    }

    /**
     * Avvia riproduzione dalla traccia selezionata.
     */
    @FXML
    private void handlePlayTrack() {
        if (selectedTrack != null) {
            playbackController.play(
                    playbackHandler.buildQueue(),
                    currentPlaylist,
                    selectedTrack
            );
        }
    }
}