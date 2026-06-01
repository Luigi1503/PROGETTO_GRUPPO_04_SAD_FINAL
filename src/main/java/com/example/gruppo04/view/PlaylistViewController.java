package com.example.gruppo04.view;

import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.observer.CatalogEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.event.Event;

import javafx.scene.layout.Region;

/**
 * Controller di vista (layer View) del pannello di gestione delle playlist.
 *
 * Disegna le playlist come una griglia di "card", più una card "+" per la
 * creazione, e delega ogni azione (crea / rinomina / elimina) al
 * {@link PlaylistController}. Non contiene logica di dominio né validazioni:
 * quelle vivono nel catalogo.
 *
 * È associato all'FXML in modo imperativo (loader.setController(...)), perciò
 * le dipendenze arrivano dal costruttore e l'FXML non dichiara un fx:controller.
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

    private final PlaylistController playlistController;
    private final MusicCatalog catalog;
    private Consumer<Playlist> onPlaylistSelected = p -> {};
    /**
     * @param playlistController orchestratore a cui delegare le azioni sulle playlist
     * @param catalog            sorgente dati di sola lettura usata per disegnare la griglia
     */
    public PlaylistViewController(PlaylistController playlistController, MusicCatalog catalog) {
        this.playlistController = playlistController;
        this.catalog = catalog;
    }

    /**
     * Inizializzazione post-FXML: invocata da JavaFX dopo l'iniezione dei campi
     * {@code @FXML}. Qui (non nel costruttore, dove i campi sarebbero ancora null)
     * si lavora sui nodi e ci si registrerà come observer del catalogo.
     */
    @FXML
    public void initialize() {
        catalog.registerObserver(this);
        playlistGrid.getStyleClass().add("playlist-grid");
        renderPlaylists();
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

        Button rename = new Button("\u270E"); //matita
        rename.getStyleClass().add("card-action");
        rename.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(playlist.getName());
            dialog.setHeaderText("Nuovo nome della playlist");
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

        Button delete = new Button("\uD83D\uDDD1");     // cestino
        delete.getStyleClass().add("card-action");
        delete.setOnAction(e -> playlistController.deletePlaylist(playlist));

        HBox actions = new HBox(8, rename, delete);
        actions.getStyleClass().add("card-actions");
        actions.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);  // non riempire StackPane
        StackPane.setAlignment(actions, Pos.BOTTOM_RIGHT);
        actions.setVisible(false);
        actions.addEventHandler(MouseEvent.MOUSE_PRESSED, Event::consume); // i bottoni non fanno scattare la selezione

        StackPane art = new StackPane(icon, actions);
        art.getStyleClass().add("card-art");

        Label name = new Label(playlist.getName());
        name.getStyleClass().add("card-title");

        VBox card = new VBox(art, name);
        card.setPickOnBounds(true);            // <-- tutta la card riceve i click
        card.getStyleClass().add("playlist-card");
        card.setOnMouseEntered(e -> actions.setVisible(true));
        card.setOnMouseExited(e -> actions.setVisible(false));
        // 1. la selezione su MOUSE_PRESSED invece che CLICKED
        card.setOnMousePressed(e -> {
            System.out.println(">> click card: " + playlist.getName());
            onPlaylistSelected.accept(playlist);
        });
        // 2. i bottoni consumano MOUSE_PRESSED, così non fanno scattare la selezione
        actions.addEventHandler(MouseEvent.MOUSE_PRESSED, Event::consume);
        return card;

    }
    /** Imposta cosa fare quando l'utente seleziona una playlist (lo collega MainView). */
    public void setOnPlaylistSelected(Consumer<Playlist> handler) {
        System.out.println(">> setOnPlaylistSelected chiamato");
        this.onPlaylistSelected = handler;
    }
}