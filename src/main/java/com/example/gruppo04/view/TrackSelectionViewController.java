package com.example.gruppo04.view;

import com.example.gruppo04.model.Track;
import com.example.gruppo04.model.Playlist;
import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.util.TrackFormatter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller JavaFX del dialog di selezione di una traccia da aggiungere alla playlist.
 * Mostra le tracce disponibili nel catalogo (escluse quelle già
 * presenti nella playlist corrente) e permette all'utente di
 * selezionarne una da aggiungere.
 */
public class TrackSelectionViewController {

    /** Tabella che mostra le tracce disponibili per l'aggiunta. */
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

    /** Pulsante per confermare l'aggiunta della traccia selezionata. Disabilitato se nessuna traccia è selezionata. */
    @FXML private Button btnAdd;

    /** Pulsante per annullare e chiudere il dialog senza aggiungere tracce. */
    @FXML private Button btnCancel;

    /** Playlist a cui aggiungere la traccia selezionata. */
    private Playlist playlist;

    /** Controller MVC delle playlist, usato per aggiungere la traccia selezionata. */
    private PlaylistController playlistController;

    /**
     * Costruttore senza parametri richiesto da FXMLLoader.
     */
    public TrackSelectionViewController() {
    }

    /**
     * Inizializza il dialog con la lista delle tracce disponibili,
     * la playlist corrente e il controller MVC.
     * Da chiamare dopo il caricamento dell'FXML.
     *
     * @param availableTracks    le tracce del catalogo non ancora presenti nella playlist
     * @param playlist           la playlist a cui aggiungere la traccia selezionata
     * @param playlistController il controller MVC delle playlist
     */
    public void init(List<Track> availableTracks, Playlist playlist, PlaylistController playlistController) {
        this.playlist = playlist;
        this.playlistController = playlistController;
        tableTracks.setItems(FXCollections.observableArrayList(availableTracks));
    }

    /**
     * Chiamato automaticamente da FXMLLoader dopo il caricamento dell'FXML.
     * Configura le colonne e il listener sulla selezione della tabella.
     */
    @FXML
    void initialize() {
        setupColumns();
        // Abilita btnAdd solo se una traccia è selezionata
        tableTracks.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) ->
                        btnAdd.setDisable(newVal == null));
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
     * Gestisce il click su "Aggiungi".
     * Notifica il listener con la traccia selezionata e chiude il dialog.
     * selectedTrack è sempre non null qui: il bottone è abilitato
     * solo quando una traccia è selezionata nella tabella (vedi initialize).
     */
    @FXML
    void handleAdd(ActionEvent event) {
        Track selected = tableTracks.getSelectionModel().getSelectedItem();
        playlistController.addTrackToPlaylist(playlist, selected);
        closeDialog();
    }

    /**
     * Gestisce il click su "Annulla".
     * Chiude il dialog senza notificare nessuna selezione.
     */
    @FXML
    void handleCancel(ActionEvent event) {
        closeDialog();
    }

    /**
     * Chiude il dialog corrente.
     */
    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}