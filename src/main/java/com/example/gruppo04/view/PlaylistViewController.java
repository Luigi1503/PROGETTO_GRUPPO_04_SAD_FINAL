package com.example.gruppo04.view;

import com.example.gruppo04.command.AddAutoGeneratorCommand;
import com.example.gruppo04.command.Command;
import com.example.gruppo04.command.CommandManager;
import com.example.gruppo04.command.RemoveAutoGeneratorCommand;
import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.model.factory_method.*;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.observer.CatalogEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;

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
        undoBtn.setDisable(!manager.canUndo());

        manager.addCanUndoListener(canUndo -> undoBtn.setDisable(!canUndo));

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
        renderPlaylists();
        catalog.registerObserver(this);
        CommandManager manager = playlistController.getManagerPlaylist();
        undoBtn.setDisable(!manager.canUndo());

        manager.addCanUndoListener(canUndo -> undoBtn.setDisable(!canUndo));
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
                     PLAYLIST_CONTENT_CHANGED,
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
        automaticSectionTitle.setVisible(automaticOnly);
        automaticSectionTitle.setManaged(automaticOnly);
        playlistGrid.setVisible(!automaticOnly);
        playlistGrid.setManaged(!automaticOnly);

        if (!automaticOnly) {
            for (Playlist playlist : catalog.getPlaylists()) {
                playlistGrid.getChildren().add(buildPlaylistCard(playlist, true));
            }
            playlistGrid.getChildren().add(buildAddCard());
        }

        if(automaticOnly) {
            for (Playlist playlist : automaticPlaylists)
                automaticPlaylistGrid.getChildren().add(buildPlaylistCard(playlist, false));
            automaticPlaylistGrid.getChildren().add(buildAddAutoCard());
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
     * @brief Costruisce la card "+" per la creazione di una playlist automatica personalizzata.
     * @details Crea un nodo grafico identico alla card di aggiunta delle playlist manuali
     * ma con un'azione diversa al click: apre {@link #showAutoPlaylistDialog()} per
     * consentire all'utente di scegliere il criterio di generazione (genere, anno, tag) di una playlist.
     *
     * @return il nodo della card di creazione playlist automatica
     */
    private Node buildAddAutoCard() {
        // Bottone "+" che apre il dialog di selezione del criterio
        Button addButton = new Button("+");
        addButton.getStyleClass().add("add-button");
        addButton.setOnAction(e -> showAutoPlaylistDialog());

        // Contenitore dell'icona della card
        StackPane art = new StackPane(addButton);
        art.getStyleClass().add("card-art");

        // Card completa con stile visivo identico alle card esistenti
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
     * @param editable {@code true} se la playlist è manuale e può essere rinominata
     *                 o eliminata tramite menu contestuale;
     *                 {@code false} se è una playlist automatica — in tal caso
     *                 il menu contestuale offre solo la rimozione dal generatore
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
        else{
            MenuItem delete = new MenuItem("Rimuovi");
            delete.setOnAction(e -> {
                AutoPlaylistGenerator generator =
                        automaticPlaylistService.getGeneratorByCriterionName(playlist.getName());
                if (generator == null) {
                    showError("La playlist automatica non e' piu' disponibile.");
                    return;
                }
                Command cmd = new RemoveAutoGeneratorCommand(
                        automaticPlaylistService,
                        generator,
                        catalog
                );
                playlistController.getManagerPlaylist().executeCommand(cmd);
            });
            ContextMenu menu = new ContextMenu(delete);
            menu.getStyleClass().add("playlist-context-menu");
            card.setOnContextMenuRequested(e ->
                    menu.show(card, e.getScreenX(), e.getScreenY()));
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


    /**
     * @brief Mostra un dialog per la creazione di una playlist automatica personalizzata.
     * @details Permette all'utente di scegliere il criterio di generazione tra:
     * <ul>
     *     <li><b>Genere</b> — filtra le tracce per genere musicale</li>
     *     <li><b>Anno</b> — filtra le tracce per periodo di pubblicazione</li>
     *     <li><b>Tag</b> — filtra le tracce per tag assegnato</li>
     * </ul>
     * Al click su OK genera la playlist tramite il pattern Factory Method e
     * aggiorna la griglia delle playlist automatiche.
     */
    private void showAutoPlaylistDialog() {
        List<AutoPlaylistGenerator> available =
                automaticPlaylistService.getAvailableGenerators();

        if (available.isEmpty()) {
            showError("Tutte le playlist automatiche disponibili sono già state create.");
            return;
        }

        ComboBox<AutoPlaylistGenerator> genreCombo = new ComboBox<>();
        ComboBox<AutoPlaylistGenerator> yearCombo = new ComboBox<>();
        ComboBox<AutoPlaylistGenerator> tagCombo = new ComboBox<>();

        for (AutoPlaylistGenerator g : available) {
            if (g instanceof GenrePlaylistGenerator) genreCombo.getItems().add(g);
            else if (g instanceof YearPlaylistGenerator) yearCombo.getItems().add(g);
            else if (g instanceof TagPlaylistGenerator) tagCombo.getItems().add(g);
        }

        StringConverter<AutoPlaylistGenerator> converter =
                new javafx.util.StringConverter<>() {
                    @Override public String toString(AutoPlaylistGenerator g) {
                        return g != null ? g.getCriterionName() : "—";
                    }
                    @Override public AutoPlaylistGenerator fromString(String s) { return null; }
                };

        genreCombo.setConverter(converter);
        yearCombo.setConverter(converter);
        tagCombo.setConverter(converter);

        genreCombo.setMaxWidth(Double.MAX_VALUE);
        yearCombo.setMaxWidth(Double.MAX_VALUE);
        tagCombo.setMaxWidth(Double.MAX_VALUE);

        // Radio button per la selezione della categoria
        RadioButton rbGenre = new RadioButton("Genere");
        RadioButton rbYear  = new RadioButton("Anno");
        RadioButton rbTag   = new RadioButton("Tag");

        ToggleGroup group = new ToggleGroup();
        rbGenre.setToggleGroup(group);
        rbYear.setToggleGroup(group);
        rbTag.setToggleGroup(group);

        // nasconde le ComboBox non selezionate
        genreCombo.setVisible(false); genreCombo.setManaged(false);
        yearCombo.setVisible(false);  yearCombo.setManaged(false);
        tagCombo.setVisible(false);   tagCombo.setManaged(false);

        rbGenre.setOnAction(e -> {
            genreCombo.setVisible(true);  genreCombo.setManaged(true);
            yearCombo.setVisible(false);  yearCombo.setManaged(false);
            tagCombo.setVisible(false);   tagCombo.setManaged(false);
            if (!genreCombo.getItems().isEmpty()) genreCombo.setValue(genreCombo.getItems().get(0));
        });
        rbYear.setOnAction(e -> {
            genreCombo.setVisible(false); genreCombo.setManaged(false);
            yearCombo.setVisible(true);   yearCombo.setManaged(true);
            tagCombo.setVisible(false);   tagCombo.setManaged(false);
            if (!yearCombo.getItems().isEmpty()) yearCombo.setValue(yearCombo.getItems().get(0));
        });
        rbTag.setOnAction(e -> {
            genreCombo.setVisible(false); genreCombo.setManaged(false);
            yearCombo.setVisible(false);  yearCombo.setManaged(false);
            tagCombo.setVisible(true);    tagCombo.setManaged(true);
            if (!tagCombo.getItems().isEmpty()) tagCombo.setValue(tagCombo.getItems().get(0));
        });

        // seleziona il primo radio button disponibile
        if (!genreCombo.getItems().isEmpty())
            rbGenre.fire();
        else if (!yearCombo.getItems().isEmpty())
            rbYear.fire();
        else
            rbTag.fire();


        VBox content = new VBox(10,
                rbGenre, genreCombo,
                rbYear,  yearCombo,
                rbTag,   tagCombo
        );
        content.getStyleClass().add("auto-playlist-content");

        Dialog<AutoPlaylistGenerator> dialog = new Dialog<>();
        dialog.setHeaderText("Aggiungi playlist automatica");
        styleDialog(dialog);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            if (rbGenre.isSelected()) return genreCombo.getValue();
            if (rbYear.isSelected())  return yearCombo.getValue();
            if (rbTag.isSelected())   return tagCombo.getValue();
            return null;
        });

        dialog.showAndWait().ifPresent(generator -> {
            Command cmd = new AddAutoGeneratorCommand(
                    automaticPlaylistService,
                    generator,
                    catalog
            );

            playlistController.getManagerPlaylist().executeCommand(cmd);
        });
    }
}
