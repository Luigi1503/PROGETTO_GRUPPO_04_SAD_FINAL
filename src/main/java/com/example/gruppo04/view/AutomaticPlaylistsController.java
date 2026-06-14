package com.example.gruppo04.view;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.model.factory_method.AutomaticPlaylistService;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller JavaFX responsabile della visualizzazione delle playlist automatiche.
 *
 * <p>Questa classe osserva il {@link MusicCatalog} e rigenera dinamicamente
 * l’elenco delle playlist automatiche ogni volta che il catalogo cambia.</p>
 *
 * <p>Le playlist vengono generate tramite {@link AutomaticPlaylistService}
 * e visualizzate come pulsanti all’interno di un contenitore {@link VBox}.</p>
 *
 * <p>Quando l’utente seleziona una playlist, viene notificato un callback
 * {@link Consumer} fornito dal controller padre.</p>
 */
public class AutomaticPlaylistsController implements CatalogObserver {

    /** Contenitore UI che ospita dinamicamente i bottoni delle playlist automatiche. */
    @FXML
    private VBox playlistsContainer;

    /** Catalogo musicale osservato per aggiornamenti. */
    private MusicCatalog catalog;

    /** Servizio che genera playlist automatiche a partire dal catalogo. */
    private AutomaticPlaylistService automaticPlaylistService;

    /**
     * Callback eseguito quando l’utente seleziona una playlist.
     * Riceve la playlist selezionata come parametro.
     */
    private Consumer<Playlist> onSelect;

    /**
     * Metodo di inizializzazione FXML.
     *
     * <p>Attualmente non utilizzato, ma mantenuto per compatibilità con JavaFX.</p>
     */
    @FXML
    public void initialize() {
        // Nessuna inizializzazione automatica necessaria
    }

    /**
     * Inizializza il controller con il catalogo e il callback di selezione.
     *
     * <p>Registra il controller come {@link CatalogObserver} e genera
     * immediatamente le playlist automatiche disponibili.</p>
     *
     * @param catalog   catalogo musicale osservato
     * @param onSelect  callback invocato quando l’utente seleziona una playlist
     */
    public void init(MusicCatalog catalog, Consumer<Playlist> onSelect) {
        this.catalog = catalog;
        this.automaticPlaylistService = AutomaticPlaylistService.getInstance();
        this.onSelect = onSelect;

        this.catalog.registerObserver(this);
        generateAllPlaylists();
    }

    /**
     * Genera tutte le playlist automatiche disponibili nel catalogo.
     *
     * <p>La UI viene aggiornata nel thread JavaFX tramite {@link Platform#runLater(Runnable)}.</p>
     */
    private void generateAllPlaylists() {
        List<Button> items = new ArrayList<>();

        for (Playlist playlist : automaticPlaylistService.refresh(catalog)) {
            items.add(createPlaylistButton(playlist));
        }

        Platform.runLater(() -> {
            playlistsContainer.getChildren().clear();
            playlistsContainer.getChildren().addAll(items);
        });
    }

    /**
     * Crea un bottone UI associato a una playlist automatica.
     *
     * @param playlist playlist da rappresentare
     * @return bottone configurato con stile e handler
     */
    private Button createPlaylistButton(Playlist playlist) {
        Button btn = new Button("\u266a  " + playlist.getName());
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A8DADC; -fx-font-size: 12px; -fx-alignment: CENTER_LEFT; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        btn.setOnAction(e -> handlePlaylistSelection(playlist));
        return btn;
    }

    /**
     * Gestisce la selezione di una playlist automatica.
     *
     * <p>Se è presente un callback {@link Consumer}, viene invocato
     * con la playlist selezionata.</p>
     *
     * @param playlist playlist selezionata dall’utente
     */
    private void handlePlaylistSelection(Playlist playlist) {
        if (onSelect != null) {
            onSelect.accept(playlist);
        } else {
            System.out.println("Playlist selezionata: " + playlist.getName());
        }
    }

    /**
     * Metodo chiamato automaticamente quando il catalogo cambia.
     *
     * <p>Rigenera le playlist automatiche se il cambiamento riguarda
     * tracce o playlist del sistema.</p>
     *
     * @param event evento generato dal catalogo
     */
    @Override
    public void onCatalogChanged(CatalogEvent event) {
        switch (event.getType()) {
            case TRACK_ADDED:
            case TRACK_REMOVED:
            case TRACK_UPDATED:
            case TRACK_CHANGED:
            case PLAYLIST_ADDED:
            case PLAYLIST_REMOVED:
            case PLAYLIST_RENAMED:
            case PLAYLIST_CONTENT_CHANGED:
            case PLAYLIST_REORDERED:
            case PLAYLIST_TRACK_ADDED:
            case PLAYLIST_TRACK_REMOVED:
                generateAllPlaylists();
                break;
            default:
                break;
        }
    }
}