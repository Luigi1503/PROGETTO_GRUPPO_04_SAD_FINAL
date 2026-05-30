package com.example.gruppo04.view;

import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.model.MusicCatalog;
import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.model.Playlist;
import com.example.gruppo04.model.Track;
import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.util.TrackFormatter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller JavaFX del pannello di dettaglio di una playlist.
 * Mostra le tracce contenute nella playlist corrente e permette
 * di aggiungerne o rimuoverne tramite PlaylistController.
 * Implementa CatalogObserver per aggiornarsi automaticamente
 * quando il catalogo cambia.
 */
public class PlaylistDetailViewController implements CatalogObserver {

    /** ResourceBundle fornito da FXMLLoader per la localizzazione. */
    @FXML private ResourceBundle resources;

    /** URL del file FXML associato a questo controller. */
    @FXML private URL location;

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

    /** Pulsante per aggiungere una traccia alla playlist corrente. */
    @FXML private Button btnAddTrack;

    /** Label che mostra il nome della playlist corrente. */
    @FXML private Label labelNamePlaylist;

    /** Label mostrata nella tabella quando la playlist non contiene tracce. */
    @FXML private Label labelEmptyPlaylist;

    /** Label che mostra il numero di tracce presenti nella playlist. */
    @FXML private Label labelNumTracks;

    /** Label che mostra la durata totale di tutte le tracce della playlist in minuti. */
    @FXML private Label labelTotalDuration;

    /** Controller MVC delle playlist, usato per aggiungere e rimuovere tracce. */
    private PlaylistController playlistController;

    /** Controller MVC delle tracce, usato per recuperare le tracce disponibili. */
    private TrackController trackController;

    /** Playlist attualmente visualizzata nel pannello. */
    private Playlist currentPlaylist;

    /** Traccia attualmente selezionata nella tabella. */
    private Track selectedTrack;

    /**
     * Costruttore senza parametri richiesto da FXMLLoader.
     */
    public PlaylistDetailViewController()  {
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
     */
    public void init(Playlist playlist, PlaylistController playlistController,
                     TrackController trackController, MusicCatalog catalog) {
        this.currentPlaylist = playlist;
        this.playlistController = playlistController;
        this.trackController = trackController;
        catalog.registerObserver(this);
        updateView();
    }

    /**
     * Chiamato dal catalogo quando si verifica un cambiamento.
     * Aggiorna la vista se l'evento riguarda le tracce della playlist corrente.
     *
     * @param event l'evento contenente i dettagli del cambiamento
     */
    @Override
    public void onCatalogChanged(CatalogEvent event) {
        switch (event.getType()) {
            case PLAYLIST_TRACK_ADDED:
            case PLAYLIST_TRACK_REMOVED:
            case PLAYLIST_RENAMED:
                updateView();
                break;
            default:
                break;
        }
    }

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
     * Configura le cellValueFactory di ogni colonna.
     */
    private void setupColumns() {
        colTitle.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTitle()));
        colAuthor.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAuthor()));
        colYear.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getYear()));
        colGenre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getGenre()));
        colDuration.setCellValueFactory(data ->
                new SimpleStringProperty(TrackFormatter.formatDuration(
                        data.getValue().getDuration())));
    }

    /**
     * Aggiorna la vista con i dati aggiornati della playlist corrente.
     * Chiamato automaticamente dall'Observer quando il catalogo cambia.
     */
    public void updateView() {
        labelNamePlaylist.setText(currentPlaylist.getNome());
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
                    getClass().getResource("/fxml/TrackSelectionView.fxml"));
            VBox root = loader.load();
            TrackSelectionViewController controller = loader.getController();

            List<Track> availableTracks = trackController.getAllTracks().stream()
                    .filter(t -> !currentPlaylist.getTracks().contains(t))
                    .collect(Collectors.toList());

            // Lambda che implementa TrackSelectionListener
            controller.init(availableTracks, track ->
                    playlistController.addTrackToPlaylist(currentPlaylist, track));

            Stage dialog = new Stage();
            dialog.setTitle("Aggiungi traccia");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestisce il click su "Rimuovi traccia".
     * Rimuove la traccia selezionata dalla playlist corrente.
     * La vista si aggiorna automaticamente tramite onCatalogChanged.
     * selectedTrack è sempre non null qui: il bottone è abilitato
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
}