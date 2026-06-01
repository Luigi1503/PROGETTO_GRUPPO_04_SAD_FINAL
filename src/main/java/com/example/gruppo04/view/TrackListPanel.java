package com.example.gruppo04.view;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.controller.TrackController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Rappresenta la componente View secondo il pattern MVC.
 * <p>
 * Si occupa esclusivamente della presentazione visiva e dell'aggiornamento
 * dei nodi grafici iniettati dall'FXML. Implementa {@link CatalogObserver}
 * per reagire alle variazioni del Model.
 * </p>
 */
public class TrackListPanel implements CatalogObserver {

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
    @FXML
    private TextField searchField;
    @FXML
    private Button addTrackButton;

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
    public TrackListPanel(TrackController controller, MusicCatalog catalog) {
        this.controller = controller;
        this.catalog = catalog;
    }

    /**
     * Inizializzazione della View post-caricamento FXML.
     */
    @FXML
    public void initialize() {
        trackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        if (addTrackButton != null) {
            addTrackButton.setOnAction(event -> onAddTrackButtonClicked());
        }
        configureColumns();

        // La View si iscrive al Model (Subject) per osservare i cambiamenti
        this.catalog.registerObserver(this);

        // Imposto il filtro/search: uso FilteredList + SortedList per aggiornare la tabella
        FilteredList<Track> filtered = new FilteredList<>(tableModel, t -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String text = newVal == null ? "" : newVal.trim().toLowerCase();
                if (text.isEmpty()) {
                    filtered.setPredicate(t -> true);
                } else {
                    filtered.setPredicate(t -> {
                        if (t.getTitle() != null && t.getTitle().toLowerCase().contains(text)) return true;
                        if (t.getAuthor() != null && t.getAuthor().toLowerCase().contains(text)) return true;
                        if (t.getGenre() != null && t.getGenre().toLowerCase().contains(text)) return true;
                        return false;
                    });
                }
            });
        }

        SortedList<Track> sorted = new SortedList<>(filtered);
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

        reloadTableData();
    }

    private void configureColumns() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
    }

    private void reloadTableData() {
        tableModel.clear();
        tableModel.addAll(catalog.getAllTracks());

        int count = tableModel.size();
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
            // 1. Carica il file FXML del form
            // ATTENZIONE al percorso: verifica che corrisponda alla tua cartella resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gruppo04/Views/TrackForm.fxml"));
            javafx.scene.Parent root = loader.load();

            // 2. Crea un nuovo "Stage" (una nuova finestra)
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Aggiungi Nuovo Brano");
            stage.setScene(new javafx.scene.Scene(root));

            // 3. (Opzionale ma consigliato) Blocca la finestra principale finché questa non viene chiusa
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // 4. Mostra la finestra
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossibile aprire la finestra di aggiunta: " + e.getMessage());
        }
    }



}