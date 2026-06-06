package com.example.gruppo04.view;

import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.controller.TrackController;

import com.example.gruppo04.util.TableColumnFactory;
import com.example.gruppo04.util.TrackFormatter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta la componente View secondo il pattern MVC.
 * <p>
 * Si occupa esclusivamente della presentazione visiva e dell'aggiornamento
 * dei nodi grafici iniettati dall'FXML. Implementa {@link CatalogObserver}
 * per reagire alle variazioni del Model.
 * </p>
 */
public class TrackListViewController implements CatalogObserver {

    @FXML
    private TableView<Track> trackTable;
    @FXML
    private TableColumn<Track, String> titleCol;
    @FXML
    private TableColumn<Track, String> artistCol;
    @FXML
    private TableColumn<Track, String> durationCol;
    @FXML
    private TableColumn<Track, Integer> yearCol;
    @FXML
    private TableColumn<Track, String> genreCol;
    @FXML
    private Button addTrackBtn;
    @FXML
    private MenuItem menuEdit;
    @FXML
    private MenuItem menuPlay;

    private PlaybackController playbackController;

    private final ObservableList<Track> tableModel = FXCollections.observableArrayList();

    // Riferimenti architetturali a Controller e Model
    private TrackController controller;
    private MusicCatalog catalog;
    private boolean observerRegistered;

    public TrackListViewController() {
    }

    public TrackListViewController(TrackController controller, MusicCatalog catalog, PlaybackController playbackController) {
        this.controller = controller;
        this.catalog = catalog;

    }

    /**
     * Inizializza la View assegnando le dipendenze necessarie.
     *
     * @param controller il controller a cui la View delega la gestione delle azioni dell'utente
     * @param catalog il catalogo musicale da cui la View legge lo stato dell'applicazione in sola lettura
     */
    @FXML
    public void init(TrackController controller, MusicCatalog catalog, PlaybackController playbackController) {
        this.controller = controller;
        this.catalog = catalog;
        this.playbackController = playbackController;
        this.catalog.registerObserver(this);
        registerCatalogObserver();
        reloadTableData();
    }

    /**
     * Inizializzazione della View post-caricamento FXML.
     */
    @FXML
    public void initialize() {
        trackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        if (addTrackBtn != null) {
            addTrackBtn.setOnAction(event -> onAddTrackButtonClicked());
        }
        if (menuEdit != null) {
            menuEdit.setOnAction(event -> onEditTrackButtonClicked());
        }
        configureColumns();

        // La View si iscrive al Model (Subject) per osservare i cambiamenti
        registerCatalogObserver();

        // Collega direttamente la SortedList alla tableModel (senza passare dal filtro)
        SortedList<Track> sorted = new SortedList<>(tableModel);
        sorted.comparatorProperty().bind(trackTable.comparatorProperty());
        trackTable.setItems(sorted);

        // Se l'utente fa click col tasto destro su una riga, selezionala prima
        trackTable.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                Node node = event.getPickResult().getIntersectedNode();
                // risali la gerarchia dei nodi fino a trovare la TableRow
                while (node != null && !(node instanceof TableRow)) {
                    node = node.getParent();
                }
                if (node instanceof TableRow) {
                    TableRow<?> row = (TableRow<?>) node;
                    trackTable.getSelectionModel().select(row.getIndex());
                }
            }
        });
    }

    private void configureColumns() {
        TableColumnFactory.setupAllColumns(titleCol, artistCol, yearCol, genreCol, durationCol);
    }

    private void reloadTableData() {
        if (catalog == null) {
            return;
        }

        tableModel.clear();
        tableModel.addAll(catalog.getAllTracks());
    }

    private void registerCatalogObserver() {
        if (catalog != null && !observerRegistered) {
            catalog.registerObserver(this);
            observerRegistered = true;
        }
    }

    /**
     * Intercetta l'evento grafico di eliminazione.
     * In MVC puro, la View NON modifica il modello, ma delega l'azione al Controller.
     */
    @FXML
    private void onDeleteButtonClicked() {
        Track selected = trackTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            controller.removeTrack(selected);
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

    @FXML
    private void onAddTrackButtonClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gruppo04/Views/TrackForm.fxml"));
            javafx.scene.Parent root = loader.load();
            TrackFormViewController formController = loader.getController();
            formController.setTrackController(controller);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Aggiungi Nuovo Brano");
            stage.setScene(new javafx.scene.Scene(root));

            stage.setWidth(550);
            stage.setHeight(580);
            stage.setMinWidth(550);
            stage.setMinHeight(580);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossibile aprire la finestra di aggiunta: " + e.getMessage());
        }
    }

    @FXML
    private void onEditTrackButtonClicked() {
        Track selected = trackTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Seleziona prima una traccia da modificare.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gruppo04/Views/TrackForm.fxml"));
            javafx.scene.Parent root = loader.load();

            TrackFormViewController formController = loader.getController();
            formController.setTrackController(controller);
            formController.populateFormForEdit(selected);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Modifica Brano");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossibile aprire la finestra di modifica: " + e.getMessage());
        }
    }

    /**
     * Avvia la riproduzione della traccia selezionata dal catalogo.
     * <p>
     * La coda viene costruita con tutte le tracce del catalogo,
     * partendo dalla traccia selezionata. Al termine della coda
     * la riproduzione si ferma secondo la modalità attiva.
     * La strategia non viene passata qui: è già impostata nel
     * {@link PlaybackController} tramite i bottoni Sequential/Shuffle/Loop
     * presenti nella barra di riproduzione.
     * </p>
     */
    @FXML
    private void handlePlayTrack() {
        Track track = trackTable.getSelectionModel().getSelectedItem();
        if (track != null) {
            List<PlayableSource> queue = new ArrayList<>();
            queue.addAll(catalog.getAllTracks());
            playbackController.play(queue, track, null);
        }
    }
}