package com.example.gruppo04.view;

import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.observer.CatalogEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    /** Icona condivisa da tutte le card. */
    private final Image noteIcon =
            new Image(getClass().getResourceAsStream("/img/note.png"));

    private  PlaylistController playlistController;
    private  MusicCatalog catalog;
    private Consumer<Playlist> onPlaylistSelected = p -> {};
    /**
     * @param playlistController orchestratore a cui delegare le azioni sulle playlist
     * @param catalog            sorgente dati di sola lettura usata per disegnare la griglia
     */
    public void init(PlaylistController playlistController, MusicCatalog catalog) {
        this.playlistController = playlistController;
        this.catalog = catalog;
        catalog.registerObserver(this);
        renderPlaylists();
    }

    /**
     * Inizializzazione post-FXML: invocata da JavaFX dopo l'iniezione dei campi
     * {@code @FXML}. Qui si lavora sui nodi e ci si registrerà come observer del catalogo.
     */
    @FXML
    public void initialize() {
        playlistGrid.getStyleClass().add("playlist-grid");

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
        switch (event.getType()) {
            case PLAYLIST_ADDED, PLAYLIST_REMOVED, PLAYLIST_RENAMED -> renderPlaylists();
            default -> { }
        }
    }

    /**
     * Ridisegna l'intera griglia da zero: una card per ogni playlist del
     * catalogo, con la card "+" sempre in coda (a destra).
     */
    private void renderPlaylists() {
        playlistGrid.getChildren().clear();

        for (Playlist playlist : catalog.getPlaylists()) {
            playlistGrid.getChildren().add(buildPlaylistCard(playlist));
        }
        playlistGrid.getChildren().add(buildAddCard());
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
     * Costruisce la card di una singola playlist: icona, nome e le azioni
     * Rinomina/Elimina, ciascuna delegata al controller sulla playlist data.
     * Il click sulla card servirà a selezionarla per il pannello di dettaglio.
     *
     * @param playlist la playlist da rappresentare
     * @return il nodo della card
     */
    private Node buildPlaylistCard(Playlist playlist) {
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

        // click sinistro → apre il dettaglio
        card.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                onPlaylistSelected.accept(playlist);
            }
        });

        return card;
    }
    /** Imposta cosa fare quando l'utente seleziona una playlist (lo collega MainView). */
    public void setOnPlaylistSelected(Consumer<Playlist> handler) {
        //System.out.println(">> setOnPlaylistSelected chiamato");
        this.onPlaylistSelected = handler;
    }

    private void styleDialog(Dialog<?> dialog) {
        dialog.getDialogPane().getStyleClass().add("playlist-dialog");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/gruppo04/Views/playlist.css").toExternalForm());
        dialog.setGraphic(null);   // toglie il "?" blu nell'header
    }
}