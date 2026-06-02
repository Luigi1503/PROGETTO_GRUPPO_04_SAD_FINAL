package com.example.gruppo04.view;

import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.util.TableColumnFactory;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller JavaFX del pannello di selezione di una traccia.
 * Mostra un certo insieme di tracce e permette all'utente di selezionarne
 * una. Notifica il chiamante tramite {@link TrackSelectionListener}
 * senza conoscere il contesto in cui viene usato — questo lo rende
 * riusabile in scenari diversi (aggiunta a playlist, riproduzione, ecc.).
 */
public class TrackSelectionViewController {

    /** Tabella che mostra le tracce disponibili per la selezione. */
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

    /** Pulsante per confermare la selezione della traccia. Disabilitato se nessuna traccia è selezionata. */
    @FXML private Button btnSelection;

    /** Pulsante per annullare e chiudere il pannello senza selezionare alcuna traccia. */
    @FXML private Button btnCancel;

    /**
     * Listener notificato quando l'utente conferma la selezione di una traccia.
     * Chi apre il pannello decide il comportamento da adottare in merito alla traccia selezionata.
     */
    private TrackSelectionListener listener;


    /**
     * Chiamato automaticamente da FXMLLoader dopo il caricamento dell'FXML.
     * Configura le colonne e il listener sulla selezione della tabella.
     */
    @FXML
    void initialize() {
        setupColumns();
        // Abilita btnSelection solo se una traccia è selezionata
        tableTracks.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) ->
                        btnSelection.setDisable(newVal == null));
    }

    /**
     * Inizializza il pannello con la lista delle tracce disponibili
     * e il listener da notificare alla conferma della selezione.
     * Da chiamare dopo il caricamento dell'FXML.
     *
     * @param availableTracks le tracce disponibili per la selezione
     * @param listener        il listener da notificare quando l'utente conferma
     */
    public void init(List<Track> availableTracks, TrackSelectionListener listener) {
        this.listener = listener;
        tableTracks.setItems(FXCollections.observableArrayList(availableTracks));
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
     * Gestisce il click su "Selection".
     * Notifica il listener con la traccia selezionata e chiude il pannello.
     * Qui selected è sempre non null: il bottone è abilitato
     * solo quando una traccia è selezionata nella tabella (vedi initialize).
     */
    @FXML
    void handleSelection(ActionEvent event) {
        Track selected = tableTracks.getSelectionModel().getSelectedItem();
        if (listener != null) {
            listener.onTrackSelected(selected);
        }
        closePanel();
    }

    /**
     * Gestisce il click su "Cancel".
     * Chiude il pannello senza notificare nessuna selezione.
     */
    @FXML
    void handleCancel(ActionEvent event) {
        closePanel();
    }

    /**
     * Chiude il dialog corrente.
     */
    private void closePanel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}