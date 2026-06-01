package com.example.gruppo04.view;

import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.model.TrackImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

/**
 * @brief Controller della View deputato alla gestione dell'interfaccia grafica del form tracce.
 *
 * Questa classe gestisce i componenti visuali definiti nel file FXML. Recupera gli input
 * immessi dall'utente, configura i vincoli grafici dei controlli all'avvio e delega
 * le azioni di business al TrackController logico.
 */
public class TrackFormViewController {

    @FXML
    private Button addButton;

    @FXML
    private TextField authorField;


    @FXML
    private Spinner<Integer> durationSpinner;

    @FXML
    private ComboBox<String> genreComboBox;

    @FXML
    private TextField titleField;

    @FXML
    private Button updateButton;

    @FXML
    private Spinner<Integer> yearSpinner;


    /**
     * @brief Riferimento al controller logico.
     */
    private TrackController trackController;

    /**
     * @brief Traccia attualmente selezionata per la modifica o l'eliminazione.
     * Se è null, significa che stiamo inserendo una nuova traccia.
     */
    private TrackImpl currentSelectedTrack;










    /**
     * @brief Metodo di inizializzazione eseguito automaticamente da JavaFX al caricamento della View.
     *
     * Viene utilizzato per configurare lo stato iniziale dei componenti grafici,
     * popolando la lista dei generi disponibili e definendo i range numerici ammissibili
     * per gli spinner di anno e durata.
     */
    @FXML
    public void initialize() {
        // Popoliamo la ComboBox con una lista chiusa di generi musicali standard
        genreComboBox.getItems().addAll("Pop", "Rock", "Jazz", "Classica", "Hip Hop", "Elettronica", "Reggae");

        // Configura lo Spinner dell'anno: range 1900-2026, valore iniziale 2024, incremento di 1
        SpinnerValueFactory<Integer> yearFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2026, 2024);
        yearSpinner.setValueFactory(yearFactory);

        // Configura lo Spinner della durata: range 1-7200 secondi (fino a 2 ore), valore iniziale 180, incremento di 1
        SpinnerValueFactory<Integer> durationFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 7200, 180);
        durationSpinner.setValueFactory(durationFactory);
        // Bottoni allo stato base
        addButton.setDisable(false);
        updateButton.setDisable(true);

    }






    /**
     * @brief Inietta il controller logico all'interno del controller visivo.
     *
     * @param trackController L'istanza del TrackController logico da associare alla View.
     */
    public void setTrackController(TrackController trackController) {
        this.trackController = trackController;
    }







    /**
     * @brief Gestisce l'azione di clic sul bottone "Aggiungi".
     *
     * Raccoglie i dati inseriti nei campi della View e invoca il metodo addTrack
     * del controller logico. Se i dati violano le regole del dominio, cattura l'eccezione
     * stampando l'errore per evitare il crash dell'applicazione.
     *
     * @param event L'evento di click generato dal bottone.
     */
    @FXML
    void handleAddAction(ActionEvent event) {
        if (trackController == null) {
            System.err.println("Errore: TrackController non iniettato nella View.");
            return;
        }

        try {
            // Estrazione dei dati dai componenti grafici
            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreComboBox.getValue();

            // Usiamo getValue() sapendo che ora gli spinner restituiscono Integer puliti
            int year = yearSpinner.getValue();
            int duration = durationSpinner.getValue();

            // Chiamata al controller logico
            trackController.addTrack(title, author, genre, year, duration);

            System.out.println("Successo: Traccia aggiunta correttamente al catalogo.");

            javafx.stage.Stage stage = (javafx.stage.Stage) addButton.getScene().getWindow();
            // Chiudiamo la finestra
            stage.close();

        } catch (IllegalArgumentException e) {
            // Intercettiamo gli errori sollevati dalle regole di validazione di TrackImpl
            System.err.println("Errore di validazione nell'interfaccia: " + e.getMessage());
        }
    }










    /**
     * @brief Gestisce l'azione di clic sul bottone "Salva Modifiche".
     *
     * @param event L'evento di click generato dal bottone.
     */
    @FXML
    void handleUpdateAction(ActionEvent event) {
        if (trackController == null || currentSelectedTrack == null) {
            return;
        }

        try {
            // 1. Leggiamo i nuovi dati appena modificati dall'utente
            String newTitle = titleField.getText();
            String newAuthor = authorField.getText();
            String newGenre = genreComboBox.getValue();
            int newYear = yearSpinner.getValue();
            int newDuration = durationSpinner.getValue();

            // 2. Passiamo al TrackController logico la vecchia traccia e i nuovi dati
            trackController.updateTrack(currentSelectedTrack, newTitle, newAuthor, newGenre, newYear, newDuration);

            System.out.println("Modifica completata con successo!");

            // 3. Svuotiamo il form per tornare allo stato di inserimento
            clearFormFields();

        } catch (IllegalArgumentException e) {
            System.err.println("Errore di validazione durante la modifica: " + e.getMessage());
        }
    }











    /**
     * @brief Svuota i campi di testo dell'interfaccia grafica e resetta lo stato.
     */
    private void clearFormFields() {
        titleField.clear();
        authorField.clear();
        genreComboBox.getSelectionModel().clearSelection();

        // Resettiamo gli spinner ai valori di default
        yearSpinner.getValueFactory().setValue(2024);
        durationSpinner.getValueFactory().setValue(180);

        // Svuotiamo la memoria della traccia selezionata
        currentSelectedTrack = null;

        // Ripristiniamo i bottoni allo stato base
        addButton.setDisable(false);
        updateButton.setDisable(true);
    }


    /**
     * @brief Popola il form con i dati di una traccia esistente.
     *
     * Viene chiamato dall'esterno (es. listener della lista tracce) quando l'utente
     * seleziona un brano. Riempie i campi, salva la traccia in memoria e scambia
     * l'abilitazione dei bottoni.
     * * @param track La traccia selezionata da modificare o eliminare.
     */
    public void populateFormForEdit(TrackImpl track) {
        this.currentSelectedTrack = track;

        // Riempiamo i campi grafici con i dati dell'oggetto
        titleField.setText(track.getTitle());
        authorField.setText(track.getAuthor());
        genreComboBox.setValue(track.getGenre());
        yearSpinner.getValueFactory().setValue(track.getYear());
        durationSpinner.getValueFactory().setValue(track.getDuration());

        // Disabilitiamo "Aggiungi" e abilitiamo "Salva Modifiche" ed "Elimina"
        addButton.setDisable(true);
        updateButton.setDisable(false);
    }



}
