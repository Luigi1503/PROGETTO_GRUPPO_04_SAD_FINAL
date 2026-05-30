package com.example.gruppo04.view;

import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.model.MusicCatalog;
import com.example.gruppo04.model.Playlist;
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
public class PlaylistViewController {

    /** Contenitore delle card, iniettato dall'FXML. */
    @FXML
    private FlowPane playlistGrid;

    /** Icona condivisa da tutte le card: l'{@link Image} si può condividere, l'ImageView no. */
    private final Image noteIcon =
            new Image(getClass().getResourceAsStream("/img/note.png"));

    private final PlaylistController playlistController;
    private final MusicCatalog catalog;

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
        // catalog.addObserver(this);   // quando hai l'interfaccia di Francesco
        renderPlaylists();
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

        addButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Inserisci il nome della playlist");
            dialog.showAndWait().ifPresent(nome -> {
                try {
                    if (!playlistController.createPlaylist(nome)) {
                        showError("Esiste già una playlist con questo nome.");
                    }
                } catch (IllegalArgumentException ex) {
                    showError("Il nome non può essere vuoto.");
                }
            });
        });

        return addButton;
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
        ImageView icona = new ImageView(noteIcon);
        icona.setFitWidth(48);
        icona.setFitHeight(48);
        icona.setPreserveRatio(true);

        Label nome = new Label(playlist.getNome());

        Button rinomina = new Button("Rinomina");
        rinomina.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(playlist.getNome());
            dialog.setHeaderText("Nuovo nome della playlist");
            dialog.showAndWait().ifPresent(nuovoNome -> {
                try {
                    if (!playlistController.renamePlaylist(playlist, nuovoNome)) {
                        showError("Esiste già una playlist con questo nome.");
                    }
                } catch (IllegalArgumentException ex) {
                    showError("Il nome non può essere vuoto.");
                }
            });
        });

        Button elimina = new Button("Elimina");
        elimina.setOnAction(e -> {
            playlistController.deletePlaylist(playlist);
           //collegamento Observer Francesco
        });

        HBox azioni = new HBox(5, rinomina, elimina);

        VBox card = new VBox(5, icona, nome, azioni);
        card.setOnMouseClicked(e -> {
            // selezione → piloterà il dettaglio di Annamaria
        });

        return card;
    }
}