package com.example.gruppo04.view;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.controller.TrackController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Rappresenta la componente View secondo il pattern MVC.
 * <p>
 * Si occupa esclusivamente della presentazione visiva e dell'aggiornamento
 * dei nodi grafici iniettati dall'FXML. Implementa {@link CatalogObserver}
 * per reagire alle variazioni del Model.
 * </p>
 */
public class TrackView implements CatalogObserver {

    @FXML
    private TableView<Track> trackTable;
    @FXML
    private TableColumn<Track, String> titleCol;
    @FXML
    private TableColumn<Track, String> artistCol;
    @FXML
    private TableColumn<Track, Integer> durationCol;
    @FXML
    private TableColumn<Track, Integer> yearCol;
    @FXML
    private TableColumn<Track, String> genreCol;
    @FXML
    private Label statusLabel;

    private final ObservableList<Track> tableModel = FXCollections.observableArrayList();

    // Riferimenti architetturali a Controller e Model
    private final TrackController controller;
    private final MusicCatalog catalog;

    /**
     * Costruttore della View. Riceve le dipendenze in modo imperativo.
     *
     * @param controller il componente Controller a cui delegare le azioni di input
     * @param catalog    il componente Model da cui leggere lo stato in sola lettura
     */
    public TrackView(TrackController controller, MusicCatalog catalog) {
        this.controller = controller;
        this.catalog = catalog;
    }

    /**
     * Inizializzazione della View post-caricamento FXML.
     */
    @FXML
    public void initialize() {
        trackTable.setItems(tableModel);
        trackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        configureColumns();

        // La View si iscrive al Model (Subject) per osservare i cambiamenti
        this.catalog.registerObserver(this);

        reloadTableData();
    }

    private void configureColumns() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
    }

    private void reloadTableData() {
        tableModel.clear();
        tableModel.addAll(catalog.getAllTracks());

        int count = tableModel.size();
        statusLabel.setText(count == 1 ? "1 traccia nel catalogo" : count + " tracce totali nel catalogo");
    }

    /**
     * Intercetta l'evento grafico di eliminazione.
     * In MVC puro, la View NON modifica il modello, ma delega l'azione al Controller.
     */
    @FXML
    private void onDeleteButtonClicked() {
        Track selected = trackTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Delega formale al controller passandogli l'ID della risorsa
            controller.removeTrack(selected.getId());
        } else {
            showError("Seleziona prima una traccia da eliminare.");
        }
    }

    public void showError(String messaggio) {
        new Alert(Alert.AlertType.ERROR, messaggio).showAndWait();
    }

    @Override
    public void onCatalogChanged(CatalogEvent event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                case TRACK_ADDED:
                case TRACK_REMOVED:
                case TRACK_UPDATED:
                    reloadTableData();
                    break;
                default:
                    break;
            }
        });
    }
}