package com.example.gruppo04.view;

import com.example.gruppo04.command.CommandManager;
import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.observer.PlaybackStartedPayload;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
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

    /**
     * Pulsante per avviare la riproduzione dell'intera playlist.
     * La strategia usata è quella attualmente attiva nella barra di riproduzione.
     */
    @FXML private Button btnPlay;

    /** Pulsante per fare undo dell'inserimento di inserimento di una canzone nella playlist */
    @FXML private Button undoBtn;


    /** Controller MVC delle playlist, usato per aggiungere e rimuovere tracce. */
    private PlaylistController playlistController;

    /** Controller MVC delle tracce, usato per recuperare le tracce disponibili. */
    private TrackController trackController;

    /** Playlist attualmente visualizzata nel pannello. */
    private Playlist currentPlaylist;

    /**
     * Indica se questa vista sta "seguendo" la riproduzione, cioè se la playlist
     * mostrata coincide con la sorgente in riproduzione. Solo in tal caso la vista
     * evidenzia la traccia corrente e segue gli skip verso le playlist successive.
     */
    private boolean followingPlayback = false;

    /** Traccia attualmente selezionata nella tabella. */
    private Track selectedTrack;

    /*Playback Controller per la gestione dello stato e della strategia di riproduzione*/
    private PlaybackController playbackController;

    /*Catalog per l'elenco delle playlist*/
    private MusicCatalog catalog;

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

        tableTracks.setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handlePlayTrack();
                }
            });

            return row;
        });




    }


    /**
     * È il gestore dell'undo, permette di
     * tornare indietro ed elminare la scelta fatta in precedenza.
     */
    @FXML
    private void handleUndo(){
        playlistController.getManagerTrackPlaylist().undo();
    }


    /**
     * Inizializza il pannello con la playlist selezionata.
     * Registra questo controller come Observer del catalogo.
     * Da chiamare dopo il caricamento dell'FXML.
     *
     * @param playlist           la playlist da visualizzare
     * @param playlistController il controller MVC delle playlist
     * @param trackController    il controller MVC delle tracce
     * @param catalog            il catalogo musicale, usato per la registrazione dell'Observer e per la lista di playlist
     * @param playbackController il controller del playback che gestisce stato di riproduzione e strategia
     */
    public void init(Playlist playlist, PlaylistController playlistController,
                     TrackController trackController, MusicCatalog catalog, PlaybackController playbackController) {
        this.currentPlaylist = playlist;
        this.playlistController = playlistController;
        this.playbackController = playbackController;
        this.trackController = trackController;
        this.catalog = catalog;
        catalog.registerObserver(this);
        updateView();
        // Allinea lo stato di "following" e l'evidenziazione quando la vista (ri)appare:
        // l'indicatore deve essere presente solo se la playlist mostrata è quella in
        // riproduzione, e deve riapparire subito senza attendere un nuovo evento.
        followingPlayback = isActiveSource();
        syncHighlight();


        //Aggiungiamo in listener per disabilitare il bottone quando nno puo essere premuto il comando di undo
        CommandManager manager = playlistController.getManagerTrackPlaylist();
        boolean statoAttuale = manager.canUndoProperty().get();
        undoBtn.setDisable(!statoAttuale);

        manager.canUndoProperty().addListener((observable, vecchioValore, nuovoValore) -> {
            if (nuovoValore == true) {
                undoBtn.setDisable(false);
            } else {
                undoBtn.setDisable(true);
            }
        });

        // 1. Carica l'immagine (assicurati che il percorso sia corretto)
        Image undoIcon = new Image(getClass().getResourceAsStream("/img/undo.png"));

        // 2. Mettila in una ImageView e ridimensionala
        ImageView iconView = new ImageView(undoIcon);
        iconView.setFitHeight(20);
        iconView.setFitWidth(20);
        iconView.setPreserveRatio(true);

        // 3. Assegnala al bottone e togli il testo
        undoBtn.setGraphic(iconView);
        undoBtn.setText("");

    }

    /**
     * @return {@code true} se la playlist mostrata è la sorgente attualmente in
     *         riproduzione (quindi questa vista deve evidenziare la traccia corrente).
     */
    private boolean isActiveSource() {
        return playbackController != null
                && !playbackController.isStopped()
                && currentPlaylist.equals(playbackController.getCurrentSource());
    }

    /**
     * Evidenzia la traccia in riproduzione solo se questa vista mostra la sorgente
     * attiva; altrimenti azzera la selezione (l'evidenziazione non deve comparire
     * nelle playlist diverse da quella in riproduzione).
     */
    private void syncHighlight() {
        if (isActiveSource()) {
            highlightCurrentTrack(playbackController.getCurrentTrack());
        } else {
            tableTracks.getSelectionModel().clearSelection();
        }
    }

    /**
     * Reagisce a un evento di riproduzione (avvio/cambio traccia): se la vista sta
     * seguendo la riproduzione e la sorgente è passata a un'altra playlist, segue il
     * cambio mostrando la nuova playlist; quindi riallinea l'evidenziazione.
     */
    private void syncWithPlayback() {
        PlayableSource source = playbackController.getCurrentSource();
        if (followingPlayback && !playbackController.isStopped()
                && source instanceof Playlist && !source.equals(currentPlaylist)) {
            currentPlaylist = (Playlist) source;
            updateView();
        }
        followingPlayback = isActiveSource();
        syncHighlight();
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
        // I cambi di traccia possono essere notificati dal thread audio (fine traccia
        // naturale): marshalliamo sempre sul thread JavaFX prima di toccare la UI.
        Platform.runLater(() -> handleCatalogChanged(event));
    }

    private void handleCatalogChanged(CatalogEvent event) {
        switch (event.getType()) {
            case PLAYLIST_TRACK_ADDED:
            case PLAYLIST_TRACK_REMOVED:
            case PLAYLIST_RENAMED:
            case TRACK_REMOVED:
                updateView();
                syncHighlight();
                break;
            case PLAYBACK_STARTED:
                PlaybackStartedPayload payload = (PlaybackStartedPayload) event.getTarget();
                // Se è stata avviata la riproduzione della playlist mostrata, inizia a seguirla.
                if (payload.getCurrentSource() instanceof Playlist
                        && payload.getCurrentSource().equals(currentPlaylist)) {
                    followingPlayback = true;
                }
                syncWithPlayback();
                break;
            case TRACK_CHANGED:
                syncWithPlayback();
                break;
            case PLAYBACK_STOPPED:
                // Riproduzione terminata: smetti di seguire e azzera l'evidenziazione.
                followingPlayback = false;
                tableTracks.getSelectionModel().clearSelection();
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
     * Avvia la riproduzione dell'intera playlist usando la strategia
     * attualmente selezionata nella barra di riproduzione.
     *
     * <p>La coda viene costruita con tutte le playlist del catalogo,
     * partendo dalla playlist corrente. La strategia non viene passata qui:
     * è già impostata nel {@link PlaybackController} tramite i bottoni
     * Sequential/Shuffle/Loop presenti nella barra di riproduzione.</p>
     */
    @FXML
    private void handlePlay() {
        List<PlayableSource> queue = new ArrayList<>(catalog.getPlaylists());
        playbackController.play(queue, currentPlaylist, null);
    }

    /**
     * Avvia la riproduzione della traccia selezionata usando la strategia
     * attualmente selezionata nella barra di riproduzione.
     *
     * <p>La coda viene costruita con tutte le playlist del catalogo,
     * partendo dalla playlist corrente e dalla traccia selezionata.
     * Al termine delle tracce della playlist corrente, la riproduzione
     * continua con la playlist successiva nel catalogo.</p>
     */
    @FXML
    private void handlePlayTrack() {
        if (selectedTrack != null) {
            List<PlayableSource> queue = new ArrayList<>(catalog.getPlaylists());
            playbackController.play(queue, currentPlaylist, selectedTrack);
        }
    }

    /**
     * Evidenzia nella tabella la traccia attualmente in riproduzione.
     * Se la traccia non appartiene alla playlist corrente, deseleziona tutto.
     *
     * @param track la traccia corrente in riproduzione
     */
    private void highlightCurrentTrack(Track track) {
        if (track == null) {
            tableTracks.getSelectionModel().clearSelection();
            return;
        }
        List<Track> tracks = tableTracks.getItems();
        int index = tracks.indexOf(track);
        if (index >= 0) {
            tableTracks.getSelectionModel().select(index);
            tableTracks.scrollTo(index);
        } else {
            tableTracks.getSelectionModel().clearSelection();
        }
    }

}