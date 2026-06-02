package com.example.gruppo04.view;

import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.util.TrackFormatter;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

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

    @FXML
    private Button importMp3Button;
    @FXML
    private Label filePathLabel;

    private TrackController trackController;
    private Track currentSelectedTrack;

    private String tempFilePath = null;

    @FXML
    public void initialize() {
        genreComboBox.getItems().addAll("Pop", "Rock", "Jazz", "Classica", "Hip Hop", "Elettronica", "Reggae");

        SpinnerValueFactory<Integer> yearFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2026, 2024);
        yearSpinner.setValueFactory(yearFactory);

        SpinnerValueFactory<Integer> durationFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 7200, 180);
        durationFactory.setConverter(new javafx.util.StringConverter<Integer>() {

            @Override
            public String toString(Integer value) {
                if (value == null) {
                    return "00:00";
                }
                return TrackFormatter.formatDuration(value);
            }

            @Override
            public Integer fromString(String text) {
                if (text == null || text.isBlank()) {
                    return 0;
                }

                try {
                    String[] parts = text.trim().split(":");
                    int minutes = Integer.parseInt(parts[0]);
                    int seconds = Integer.parseInt(parts[1]);

                    return minutes * 60 + seconds;
                } catch (Exception e) {
                    return 0;
                }
            }
        });
        durationSpinner.setValueFactory(durationFactory);

        setCampiBloccati(true);
        addButton.setDisable(true);
        updateButton.setDisable(true);
        yearSpinner.getEditor().clear();
        durationSpinner.getEditor().clear();
    }

    public void setTrackController(TrackController trackController) {
        this.trackController = trackController;
    }

    /**
     * @brief Metodo di supporto per bloccare/sbloccare i campi di input in blocco.
     * @param bloccato true per disabilitare i campi, false per renderli scrivibili.
     */
    private void setCampiBloccati(boolean bloccato) {
        titleField.setDisable(bloccato);
        authorField.setDisable(bloccato);
        genreComboBox.setDisable(bloccato);
        yearSpinner.setDisable(bloccato);
        durationSpinner.setDisable(bloccato);
    }

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

    private void estraiDatiDaMp3(File file) {
        try {
            Mp3File mp3file = new Mp3File(file.getAbsolutePath());

            if (mp3file.getLengthInSeconds() > 0) {
                durationSpinner.getValueFactory().setValue((int) mp3file.getLengthInSeconds());
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
            int duration = durationSpinner.getValue();

            trackController.addTrack(title, author, genre, year, duration, tempFilePath);

            System.out.println("Successo: Traccia aggiunta correttamente al catalogo.");

            javafx.stage.Stage stage = (javafx.stage.Stage) addButton.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            System.err.println("Errore di validazione nell'interfaccia: " + e.getMessage());
        }
    }

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
            int newDuration = durationSpinner.getValue();

            trackController.updateTrack(currentSelectedTrack, newTitle, newAuthor, newGenre, newYear, newDuration);

            System.out.println("Modifica completata con successo!");
            clearFormFields();

        } catch (IllegalArgumentException e) {
            System.err.println("Errore di validazione durante la modifica: " + e.getMessage());
        }
    }

    private void clearFormFields() {
        titleField.clear();
        authorField.clear();
        genreComboBox.getSelectionModel().clearSelection();
        yearSpinner.getValueFactory().setValue(2024);
        durationSpinner.getValueFactory().setValue(180);
        currentSelectedTrack = null;
        tempFilePath = null;
        filePathLabel.setText("Nessun file selezionato");

        setCampiBloccati(true);
        addButton.setDisable(true);
        updateButton.setDisable(true);
        yearSpinner.getEditor().clear();
        durationSpinner.getEditor().clear();
    }

    public void populateFormForEdit(Track track) {
        this.currentSelectedTrack = track;
        titleField.setText(track.getTitle());
        authorField.setText(track.getAuthor());
        genreComboBox.setValue(track.getGenre());
        yearSpinner.getValueFactory().setValue(track.getYear());
        durationSpinner.getValueFactory().setValue(track.getDuration());

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