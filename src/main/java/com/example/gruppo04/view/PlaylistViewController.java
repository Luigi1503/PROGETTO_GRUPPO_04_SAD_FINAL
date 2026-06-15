package com.example.gruppo04.view;

import com.example.gruppo04.command.CommandManager;
import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.model.factory_method.AutomaticPlaylistService;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.observer.CatalogEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.event.Event;

import javafx.scene.input.MouseButton;

import javafx.scene.layout.Region;

/**
 * Controller di vista (layer View) del pannello di gestione delle playlist.
 *
 * Disegna le playlist come una griglia di "card", più una card "+" per la
 * creazione, e delega ogni azione (crea / rinomina / elimina) al
 * {@link PlaylistController}. Non contiene logica di dominio né validazioni:
 * quelle vivono nel catalogo.
 *
 * L'aggiornamento della griglia avverrà tramite Observer, quando il catalogo
 * notifica un cambiamento.
 */
public class PlaylistViewController implements CatalogObserver {

    /** Contenitore delle card, iniettato dall'FXML. */
    @FXML
    private FlowPane playlistGrid;

    @FXML
    private FlowPane automaticPlaylistGrid;

    @FXML
    private Label manualSectionTitle;

    @FXML
    private Label automaticSectionTitle;

    /** Bottone per la riproduzione di tutte le playlist */
    @FXML
    private Button btnPlayAll;

    /** Pulsante per fare undo dell'inserimento di inserimento di playlist */
    @FXML private Button undoBtn;



    /** Icona condivisa da tutte le card. */
    private final Image noteIcon =
            new Image(getClass().getResourceAsStream("/img/note.png"));

    private  PlaylistController playlistController;
    private  MusicCatalog catalog;
    private Consumer<Playlist> onPlaylistSelected = p -> {};
    private AutomaticPlaylistService automaticPlaylistService;
    private boolean automaticOnly;

    /** Playback controller per la gestione della riproduzione delle playlist*/
    private PlaybackController playbackController;
    /**
     * @param playlistController orchestratore a cui delegare le azioni sulle playlist
     * @param catalog            sorgente dati di sola lettura usata per disegnare la griglia
     */
    public void init(PlaylistController playlistController, MusicCatalog catalog, PlaybackController playbackController) {
        this.playlistController = playlistController;
        this.catalog = catalog;
        this.playbackController = playbackController;
        this.automaticPlaylistService = AutomaticPlaylistService.getInstance();
        this.automaticOnly = false;
        catalog.registerObserver(this);
        renderPlaylists();

        //Aggiungiamo in listener per disabilitare il bottone quando nno puo essere premuto il comando di undo
        CommandManager manager = playlistController.getManagerPlaylist();
        boolean statoAttuale = manager.canUndoProperty().get();
        undoBtn.setDisable(!statoAttuale);

        manager.canUndoProperty().addListener((observable, vecchioValore, nuovoValore) -> {
            if (nuovoValore == true) {
                undoBtn.setDisable(false);
            } else {
                undoBtn.setDisable(true);
            }
        });

    }

    /**
     * Inizializza la vista in modalità "solo playlist automatiche".
     * <p>
     * Esegue l'inizializzazione standard tramite {@link #init(PlaylistController, MusicCatalog, PlaybackController)}
     * e successivamente:
     * <ul>
     *     <li>abilita la modalità {@code automaticOnly};</li>
     *     <li>nasconde il pulsante di undo;</li>
     *     <li>mostra esclusivamente le playlist generate automaticamente.</li>
     * </ul>
     * </p>
     *
     * @param playlistController controller responsabile delle operazioni sulle playlist
     * @param catalog catalogo musicale da osservare e visualizzare
     * @param playbackController controller responsabile della riproduzione
     */
    public void initAutomaticOnly(PlaylistController playlistController, MusicCatalog catalog, PlaybackController playbackController) {
        init(playlistController, catalog, playbackController);
        this.automaticOnly = true;
        undoBtn.setVisible(false);
        undoBtn.setManaged(false);
        renderPlaylists();
    }

    /**
     * Gestisce la richiesta di annullamento dell'ultima operazione eseguita
     * sulle playlist tramite il {@link CommandManager}.
     * <p>
     * Se disponibile un comando annullabile, viene eseguito il metodo
     * {@code undo()} del manager associato al controller delle playlist.
     * </p>
     */
    @FXML
    private void handleUndo(){
        playlistController.getManagerPlaylist().undo();
    }

    /**
     * Inizializzazione post-FXML: invocata da JavaFX dopo l'iniezione dei campi
     * {@code @FXML}. Qui si lavora sui nodi e ci si registrerà come observer del catalogo.
     */
    @FXML
    public void initialize() {
        playlistGrid.getStyleClass().add("playlist-grid");
        automaticPlaylistGrid.getStyleClass().add("playlist-grid");

        playlistGrid.sceneProperty().addListener((obs, oldS, scene) -> {
            if (scene != null) {
                String css = getClass()
                        .getResource("/com/example/gruppo04/Views/playlist.css")
                        .toExternalForm();
                if (!scene.getStylesheets().contains(css)) {
                    scene.getStylesheets().add(css);
                }
            }
        });


    }
    /**
     * Callback dell'Observer: ridisegna la griglia quando cambiano le playlist
     * (aggiunta, rinomina, eliminazione). Ignora gli eventi sulle tracce.
     */
    @Override
    public void onCatalogChanged(CatalogEvent event) {
        // I cambi di riproduzione possono arrivare dal thread audio: marshalla sul thread JavaFX.
        Platform.runLater(() -> {
            switch (event.getType()) {
                case PLAYLIST_ADDED, PLAYLIST_REMOVED, PLAYLIST_RENAMED,
                     TRACK_ADDED, TRACK_REMOVED, TRACK_UPDATED -> renderPlaylists();
                case PLAYBACK_STARTED, TRACK_CHANGED, PLAYBACK_STOPPED -> highlightActivePlaylist();
                default -> { }
            }
        });
    }

    /**
     * Evidenzia la card corrispondente alla playlist attualmente in riproduzione.
     * <p>
     * Se la riproduzione è ferma oppure la sorgente attiva non è una playlist,
     * tutte le evidenziazioni vengono rimosse.
     * </p>
     */
    private void highlightActivePlaylist() {
        PlayableSource source = (playbackController != null && !playbackController.isStopped())
                ? playbackController.getCurrentSource() : null;

        for (Node node : playlistGrid.getChildren()) {
            highlightNode(node, source);
        }
        for (Node node : automaticPlaylistGrid.getChildren()) {
            highlightNode(node, source);
        }
    }

    /**
     * Applica o rimuove lo stile grafico che evidenzia la playlist
     * attualmente in riproduzione.
     *
     * @param node nodo grafico associato a una playlist
     * @param source sorgente di riproduzione attualmente attiva; può essere {@code null}
     *               se la riproduzione è ferma
     */
    private void highlightNode(Node node, PlayableSource source) {
        Object data = node.getUserData();
        boolean active = (data instanceof Playlist) && data.equals(source);
        node.getStyleClass().remove("playlist-card-active");
        if (active) {
            node.getStyleClass().add("playlist-card-active");
        }
    }

    /**
     * Ridisegna l'intera griglia da zero: una card per ogni playlist del
     * catalogo, con la card "+" sempre in coda (a destra).
     */
    private void renderPlaylists() {
        playlistGrid.getChildren().clear();
        automaticPlaylistGrid.getChildren().clear();

        List<Playlist> automaticPlaylists = automaticPlaylistService.refresh(catalog);

        manualSectionTitle.setVisible(!automaticOnly);
        manualSectionTitle.setManaged(!automaticOnly);
        playlistGrid.setVisible(!automaticOnly);
        playlistGrid.setManaged(!automaticOnly);
        undoBtn.setVisible(!automaticOnly);
        undoBtn.setManaged(!automaticOnly);

        if (!automaticOnly) {
            for (Playlist playlist : catalog.getPlaylists()) {
                playlistGrid.getChildren().add(buildPlaylistCard(playlist, true));
            }
            playlistGrid.getChildren().add(buildAddCard());
        }

        for (Playlist playlist : automaticPlaylists) {
            automaticPlaylistGrid.getChildren().add(buildPlaylistCard(playlist, false));
        }

        // Ripristina l'evidenziazione della playlist in riproduzione dopo ogni ridisegno
        // (anche tornando su questa vista mentre una playlist è in riproduzione).
        highlightActivePlaylist();
    }

    /**
     * Costruisce la card "+": apre un dialog per il nome e delega la creazione
     * al controller, segnalando un errore se il nome è duplicato o vuoto.
     *
     * @return il nodo della card di creazione
     */
    private Node buildAddCard() {
        Button addButton = new Button("+");
        addButton.getStyleClass().add("add-button");
        addButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Inserisci il nome della playlist");
            styleDialog(dialog);
            dialog.showAndWait().ifPresent(name -> {
                try {
                    if (!playlistController.createPlaylist(name)) {
                        showError("Esiste già una playlist con questo nome.");
                    }
                } catch (IllegalArgumentException ex) {
                    showError("Il nome non può essere vuoto.");
                }
            });
        });
        StackPane art = new StackPane(addButton);
        art.getStyleClass().add("card-art");

        VBox card = new VBox(art);
        card.getStyleClass().addAll("playlist-card", "add-card");
        return card;

    }

    /**
     * Mostra un messaggio di errore bloccante all'utente.
     *
     * @param messaggio testo da mostrare
     */
    private void showError(String messaggio) {
        new Alert(Alert.AlertType.ERROR, messaggio).showAndWait();
    }

    /**
     * Costruisce e restituisce la rappresentazione grafica di una playlist.
     * <p>
     * La card mostra:
     * <ul>
     *     <li>l'icona della playlist;</li>
     *     <li>il nome della playlist;</li>
     *     <li>un menu contestuale per rinomina ed eliminazione (se modificabile);</li>
     *     <li>la selezione della playlist tramite click sinistro.</li>
     * </ul>
     * </p>
     *
     * @param playlist playlist da rappresentare
     * @param editable {@code true} se la playlist può essere rinominata o eliminata,
     *                 {@code false} altrimenti
     * @return nodo JavaFX che rappresenta la card della playlist
     */
    private Node buildPlaylistCard(Playlist playlist, boolean editable) {
        ImageView icon = new ImageView(noteIcon);
        icon.setFitWidth(64);
        icon.setFitHeight(64);
        icon.setPreserveRatio(true);

        StackPane art = new StackPane(icon);
        art.getStyleClass().add("card-art");

        Label name = new Label(playlist.getName());
        name.getStyleClass().add("card-title");

        VBox card = new VBox(art, name);
        card.getStyleClass().add("playlist-card");
        card.setPickOnBounds(true);
        // Associa la playlist alla card per poterla evidenziare quando è in riproduzione.
        card.setUserData(playlist);

        if (editable) {
        // menu contestuale (tasto destro)
        MenuItem rename = new MenuItem("Rinomina");
        rename.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(playlist.getName());
            dialog.setHeaderText("Nuovo nome della playlist");
            styleDialog(dialog);
            dialog.showAndWait().ifPresent(newName -> {
                try {
                    if (!playlistController.renamePlaylist(playlist, newName)) {
                        showError("Esiste già una playlist con questo nome.");
                    }
                } catch (IllegalArgumentException ex) {
                    showError("Il nome non può essere vuoto.");
                }
            });
        });

        MenuItem delete = new MenuItem("Elimina");
        delete.setOnAction(e -> playlistController.deletePlaylist(playlist));
        delete.getStyleClass().add("danger");

        ContextMenu menu = new ContextMenu(rename, delete);
        menu.getStyleClass().add("playlist-context-menu");
        card.setOnContextMenuRequested(e -> menu.show(card, e.getScreenX(), e.getScreenY()));   // si apre da solo col tasto destro
        }

        // click sinistro → apre il dettaglio
        card.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                onPlaylistSelected.accept(playlist);
            }
        });

        return card;
    }

    /**
     * Registra il comportamento da eseguire quando l'utente seleziona una playlist.
     * <p>
     * Il consumer ricevuto viene invocato ogni volta che l'utente effettua
     * un click sinistro su una card playlist.
     * </p>
     *
     * @param handler funzione da eseguire alla selezione di una playlist
     */
    public void setOnPlaylistSelected(Consumer<Playlist> handler) {
        //System.out.println(">> setOnPlaylistSelected chiamato");
        this.onPlaylistSelected = handler;
    }

    /**
     * Applica lo stile CSS personalizzato a una finestra di dialogo.
     * <p>
     * Il metodo aggiunge il foglio di stile dedicato alle playlist e
     * rimuove eventuali elementi grafici predefiniti.
     * </p>
     *
     * @param dialog finestra di dialogo da personalizzare
     */
    private void styleDialog(Dialog<?> dialog) {
        dialog.getDialogPane().getStyleClass().add("playlist-dialog");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/gruppo04/Views/playlist.css").toExternalForm());
        dialog.setGraphic(null);
    }



    /**
     * Avvia la riproduzione di tutte le playlist del catalogo
     * partendo dalla prima, usando la strategia attualmente selezionata.
     * <p>
     * Passa {@code null} come traccia di partenza — la riproduzione
     * inizia dalla prima traccia della prima playlist in coda.
     * </p>
     */
    @FXML
    private void handlePlayAll() {
        List<PlayableSource> queue = automaticOnly
                ? new ArrayList<>(automaticPlaylistService.refresh(catalog))
                : new ArrayList<>(catalog.getPlaylists());
        if (!automaticOnly) {
            queue.addAll(automaticPlaylistService.refresh(catalog));
        }
        if (!queue.isEmpty()) {
            playbackController.play(queue, queue.get(0), null);
        }
    }
}
