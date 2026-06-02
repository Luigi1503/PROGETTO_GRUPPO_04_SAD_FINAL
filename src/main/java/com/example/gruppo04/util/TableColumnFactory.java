package com.example.gruppo04.util;

import com.example.gruppo04.interfaces.Track;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;

/**
 * Classe di utilità per la configurazione delle colonne
 * delle TableView che mostrano tracce musicali.
 * Evita la duplicazione del codice di setup tra le view
 * che visualizzano liste di tracce. A favore del principio DRY.
 * Garantisce un Single Point of Change: qualora si aggiungesse
 * o modificasse un campo della classe {@link com.example.gruppo04.interfaces.Track},
 * è sufficiente intervenire sulla presente classe senza dover
 * modificare tutte le view che mostrano tracce.
 */
public class TableColumnFactory {

    /** Costruttore privato — classe non istanziabile. */
    private TableColumnFactory() {}


    /**
     * Configura tutte le colonne standard di una TableView di tracce.
     *
     * @param colTitle    colonna del titolo
     * @param colAuthor   colonna dell'autore
     * @param colYear     colonna dell'anno
     * @param colGenre    colonna del genere
     * @param colDuration colonna della durata
     */
    public static void setupAllColumns(
            TableColumn<Track, String> colTitle,
            TableColumn<Track, String> colAuthor,
            TableColumn<Track, Integer> colYear,
            TableColumn<Track, String> colGenre,
            TableColumn<Track, String> colDuration) {

        colTitle.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTitle()));
        colAuthor.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAuthor()));
        colYear.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getYear()));
        colGenre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getGenre()));
        colDuration.setCellValueFactory(data ->
                new SimpleStringProperty(
                        TrackFormatter.formatDuration(data.getValue().getDuration())));
    }
}