package com.example.gruppo04.view;

import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.util.TrackFormatter;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;

import java.io.File;

import static javafx.scene.paint.Color.web;

public class TrackFormViewController {

    @FXML
    private Button addButton;
    @FXML
    private TextField authorField;
    @FXML
    private TextField durationSpinner;
    @FXML
    private ComboBox<String> genreComboBox;
    @FXML
    private TextField titleField;
    @FXML
    private Button updateButton;
    @FXML
    private Spinner<Integer> yearSpinner;

    @FXML
    private Button importMp3Button;
    @FXML
    private Label filePathLabel;

    /** @brief Riferimento al controller principale per la gestione della logica di business. */
    private TrackController trackController;

    /** @brief Traccia attualmente selezionata (utilizzata solo in fase di modifica). */
    private Track currentSelectedTrack;

    /** @brief Percorso temporaneo del file MP3 importato, pronto per essere salvato. */
    private String tempFilePath = null;

    private int duration;

    /**
     * @brief Inizializza i componenti grafici della View subito dopo il caricamento del file FXML.
     * * @details Popola la ComboBox dei generi musicali, imposta i limiti per gli Spinner
     * dell'anno (1900-corrente) e della durata (in secondi), applicando a quest'ultimo
     * un converter personalizzato per mostrare il formato "MM:SS" invece dei secondi grezzi.
     * Infine, disabilita i campi in attesa che l'utente importi un file o abiliti l'inserimento manuale.
     */
    @FXML
    public void initialize() {
        genreComboBox.getItems().addAll("Pop", "Rock", "Jazz", "Classica", "Hip Hop", "Electronic", "Reggae");

        SpinnerValueFactory<Integer> yearFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2026, 2024);
        yearSpinner.setValueFactory(yearFactory);



        setCampiBloccati(true);
        addButton.setDisable(true);
        updateButton.setDisable(true);
        yearSpinner.getEditor().clear();
        durationSpinner.clear();
    }


    /**
     * @brief Inietta il controller delle tracce all'interno della View.
     * @param trackController L'istanza del controller per effettuare operazioni CRUD.
     */
    public void setTrackController(TrackController trackController) {
        this.trackController = trackController;
    }

    /**
     * @brief Metodo di supporto per bloccare o sbloccare i campi di input in blocco.
     * @param bloccato Se {@code true} i campi vengono disabilitati, se {@code false} resi modificabili.
     */
    private void setCampiBloccati(boolean bloccato) {
        titleField.setDisable(bloccato);
        authorField.setDisable(bloccato);
        genreComboBox.setDisable(bloccato);
        yearSpinner.setDisable(bloccato);
        durationSpinner.setDisable(bloccato);
    }





    /**
     * @brief Gestisce l'evento di clic sul bottone per l'importazione di un file MP3.
     * * @details Apre una finestra di dialogo di sistema (FileChooser) filtrata per l'estensione .mp3.
     * Se l'utente seleziona un file, aggiorna la UI, sblocca i campi e invoca il metodo
     * per l'estrazione automatica dei metadati (ID3 Tag).
     * * @param event L'evento scatenato dal clic sul bottone.
     */
    @FXML
    void handleImportMp3Action(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona un brano MP3");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("File Audio MP3", "*.mp3")
        );

        javafx.stage.Stage stage = (javafx.stage.Stage) titleField.getScene().getWindow();
        File fileSelezionato = fileChooser.showOpenDialog(stage);

        if (fileSelezionato != null) {
            this.tempFilePath = fileSelezionato.getAbsolutePath();

            String nomeFile = fileSelezionato.getName();
            if(nomeFile.length() > 30) nomeFile = nomeFile.substring(0, 27) + "...";
            filePathLabel.setText(nomeFile);

            titleField.clear();
            authorField.clear();
            genreComboBox.getSelectionModel().clearSelection();

            estraiDatiDaMp3(fileSelezionato);


            setCampiBloccati(false);

            if (currentSelectedTrack == null) {
                addButton.setDisable(false);
            }
        }
    }



    /**
     * @brief Legge e inserisce automaticamente i metadati estratti da un file MP3 nei campi del form.
     * * @details Sfrutta la libreria mp3agic per analizzare i tag ID3v2 del file audio.
     * Estrae informazioni come durata, titolo, artista, genere e anno di pubblicazione,
     * popolando i relativi campi dell'interfaccia grafica.
     * * @param file L'oggetto File corrispondente al brano MP3 selezionato dall'utente.
     */
    private void estraiDatiDaMp3(File file) {
        try {
            Mp3File mp3file = new Mp3File(file.getAbsolutePath());

            if (mp3file.getLengthInSeconds() > 0) {
                this.duration = (int) mp3file.getLengthInSeconds();
                durationSpinner.setText(TrackFormatter.formatDuration(this.duration));
            }

            if (mp3file.hasId3v2Tag()) {
                ID3v2 tag = mp3file.getId3v2Tag();

                if (tag.getTitle() != null && !tag.getTitle().trim().isEmpty()) {
                    titleField.setText(tag.getTitle());
                } else {
                    titleField.setText(file.getName().replace(".mp3", ""));
                }

                if (tag.getArtist() != null && !tag.getArtist().trim().isEmpty()) {
                    authorField.setText(tag.getArtist());
                }

                if (tag.getGenreDescription() != null) {
                    genreComboBox.setValue(tag.getGenreDescription());
                }

                if (tag.getYear() != null && tag.getYear().length() >= 4) {
                    try {
                        int anno = Integer.parseInt(tag.getYear().substring(0, 4));
                        yearSpinner.getValueFactory().setValue(anno);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("Errore o file MP3 non valido: " + e.getMessage());
        }
    }





    /**
     * @brief Gestisce l'aggiunta di una nuova traccia al catalogo.
     * * @details Raccoglie i dati inseriti nei campi del form e li invia al {@link TrackController}.
     * Se la validazione nel Modello fallisce (es. titolo vuoto o duplicato), intercetta
     * l'eccezione {@code IllegalArgumentException} e mostra un Alert personalizzato con tema scuro e
     * un'icona vettoriale di avviso.
     * * @param event L'evento scatenato dal clic sul bottone "Aggiungi".
     */
    @FXML
    void handleAddAction(ActionEvent event) {
        if (trackController == null) {
            return;
        }

        try {
            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreComboBox.getValue();
            int year = yearSpinner.getValue();
            trackController.addTrack(title, author, genre, year, this.duration, tempFilePath);


            System.out.println("Successo: Traccia aggiunta correttamente al catalogo.");

            javafx.stage.Stage stage = (javafx.stage.Stage) addButton.getScene().getWindow();
            stage.close();

        }catch (IllegalArgumentException e) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore di Validazione");
        alert.setHeaderText("Impossibile salvare la traccia");

        //creazione dell'SVG per disegnare il triangolo di errore al posto della X
        SVGPath warningIcon = new SVGPath();
        warningIcon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
        warningIcon.setFill(web("#FFC107"));
        warningIcon.setScaleX(1.5);
        warningIcon.setScaleY(1.5);

        StackPane iconContainer = new StackPane(warningIcon);
        iconContainer.setPadding(new Insets(15));
        alert.setGraphic(iconContainer);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().addAll(titleField.getScene().getRoot().getStylesheets());
        dialogPane.getStyleClass().add("dark-alert");

        alert.setContentText(e.getMessage());

        alert.showAndWait();
    }
    }











    /**
     * @brief Gestisce l'aggiornamento di una traccia esistente nel catalogo.
     * * @details Sovrascrive i dati della traccia correntemente selezionata con i nuovi
     * valori presenti nel form, avvalendosi del {@link TrackController}. In caso di
     * violazione delle regole di dominio, gestisce l'errore tramite un Alert dedicato.
     * * @param event L'evento scatenato dal clic sul bottone "Modifica".
     */
    @FXML
    void handleUpdateAction(ActionEvent event) {
        if (trackController == null || currentSelectedTrack == null) {
            return;
        }

        try {
            String newTitle = titleField.getText();
            String newAuthor = authorField.getText();
            String newGenre = genreComboBox.getValue();
            int newYear = yearSpinner.getValue();
            int newDuration = duration;


            String newFilePath = (tempFilePath != null) ? tempFilePath : currentSelectedTrack.getFilePath();
            trackController.updateTrack(currentSelectedTrack, newTitle, newAuthor, newGenre, newYear, newDuration, newFilePath);

            System.out.println("Modifica completata con successo!");
            clearFormFields();

            javafx.stage.Stage stage = (javafx.stage.Stage) titleField.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore di Validazione");
            alert.setHeaderText("Impossibile modificare la traccia");
            alert.setContentText(e.getMessage());

            SVGPath warningIcon = new SVGPath();
            warningIcon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
            warningIcon.setFill(web("#FFC107"));
            warningIcon.setScaleX(1.5);
            warningIcon.setScaleY(1.5);

            StackPane iconContainer = new StackPane(warningIcon);
            iconContainer.setPadding(new Insets(15));
            alert.setGraphic(iconContainer);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().addAll(titleField.getScene().getRoot().getStylesheets());
            dialogPane.getStyleClass().add("dark-alert");

            alert.showAndWait();
        }
    }

    /**
     * @brief Ripulisce tutti i campi del form e ripristina lo stato iniziale.
     * * @details Svuota i campi di testo, resetta la selezione della ComboBox,
     * imposta valori di default per gli Spinner e blocca nuovamente l'interfaccia
     * in attesa di un nuovo inserimento o selezione.
     */
    private void clearFormFields() {
        titleField.clear();
        authorField.clear();
        genreComboBox.getSelectionModel().clearSelection();
        yearSpinner.getValueFactory().setValue(2024);

        durationSpinner.clear();

        currentSelectedTrack = null;
        tempFilePath = null;
        filePathLabel.setText("Nessun file selezionato");

        setCampiBloccati(true);
        addButton.setDisable(true);
        updateButton.setDisable(true);
        yearSpinner.getEditor().clear();
    }


    /**
     * @brief Popola il form con i dati di una traccia esistente per consentirne la modifica.
     * * @details Disabilita il bottone di "Aggiunta" e abilita quello di "Modifica". Sblocca
     * i campi di input permettendo all'utente di alterare i valori correnti.
     * * @param track L'oggetto {@link Track} selezionato dall'utente nella tabella principale.
     */
    public void populateFormForEdit(Track track) {
        this.currentSelectedTrack = track;
        titleField.setText(track.getTitle());
        authorField.setText(track.getAuthor());
        genreComboBox.setValue(track.getGenre());
        yearSpinner.getValueFactory().setValue(track.getYear());
        this.duration = track.getDuration();
        durationSpinner.setText(TrackFormatter.formatDuration(this.duration));
        if(track.getFilePath() != null) {
            File f = new File(track.getFilePath());
            filePathLabel.setText(f.getName());
        } else {
            filePathLabel.setText("Nessun file associato");
        }


        setCampiBloccati(false);
        addButton.setDisable(true);
        updateButton.setDisable(false);
    }
}