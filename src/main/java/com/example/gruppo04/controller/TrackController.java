package com.example.gruppo04.controller;

import com.example.gruppo04.command.AddTrackCommand;
import com.example.gruppo04.command.Command;
import com.example.gruppo04.command.CommandManager;
import com.example.gruppo04.command.RemoveTrackCommand;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.TrackImpl;

import java.util.Collection;
import java.util.UUID;

/**
 * @brief Controller deputato alla gestione del ciclo di vita delle tracce musicali.
 *
 * Questa classe funge da intermediario logico tra l'interfaccia grafica (View)
 * e il dominio dei dati (Model). Il suo scopo è intercettare gli input grezzi
 * inviati dai componenti della View, delegare la validazione dei vincoli di consistenza
 * strutturale direttamente alle classi del modello e orchestrare le conseguenti
 * operazioni di inserimento, modifica ed eliminazione sul modulo del catalogo centrale.
 */
public class TrackController {

    /**
     * @brief Riferimento al catalogo musicale centrale.
     *
     * Viene espresso mediante l'interfaccia astratta MusicCatalog al fine di
     * garantire un basso accoppiamento e favorire l'intercambiabilità delle
     * implementazioni concrete in fase di testing o refactoring.
     */
    private final MusicCatalog catalog;
    private CommandManager managerTrack;

    /**
     * @brief Costruttore del controller che inizializza la dipendenza del catalogo.
     *
     * Riceve un'istanza del catalogo musicale tramite Dependency Injection dall'esterno,
     * accertandosi preliminarmente che il riferimento fornito non sia nullo.
     *
     * @param catalog L'istanza condivisa del catalogo musicale su cui operare.
     * @throws IllegalArgumentException Se il parametro catalog passato in ingresso è nullo.
     */
    public TrackController(MusicCatalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("Il catalogo non può essere nullo.");
        }
        this.catalog = catalog;
        this.managerTrack = new CommandManager();
    }

    /**
     * @brief Coordina la creazione e l'aggiunta di una nuova traccia musicale nel sistema.
     *
     * Tenta di istanziare un oggetto concreto della classe TrackImpl fornendo i dati grezzi
     * inseriti dall'utente nei campi di testo dell'interfaccia. Se i valori violano le regole
     * di dominio (es. stringhe vuote, anni fuori range o durate negative), l'eccezione lanciata
     * dal costruttore di TrackImpl si propaga verso l'alto, consentendo alla View di intercettarla.
     * Se la traccia è valida, viene formalmente sottomessa al catalogo centrale.
     *
     * @param title    Il titolo testuale della traccia immesso nel modulo grafico.
     * @param author   Il nome dell'autore o dell'artista associato al brano.
     * @param genre    Il genere musicale di riferimento per il brano.
     * @param year     L'anno cronologico di pubblicazione dell'opera.
     * @param duration La durata complessiva del brano espressa in secondi totali.
     * @param filePath Percorso file dove si trova il file della canzone
     * @throws IllegalArgumentException Se le stringhe o i valori numerici violano i vincoli del dominio.
     */
    public void addTrack(String title, String author, String genre, int year, int duration, String filePath) {
        Track newTrack = new TrackImpl(title, author, genre, year, duration, filePath);
        Command cmd = new AddTrackCommand(newTrack, catalog);
        managerTrack.executeCommand(cmd);
    }

    /**
     * @brief Esegue l'aggiornamento dei dati relativi a una traccia esistente e precedentemente selezionata.
     *
     * Il metodo opera direttamente sul riferimento dell'oggetto Track estratto dall'interfaccia a seguito
     * dell'interazione dell'utente (clic sulla lista). Invoca in sequenza i metodi setter definiti dal contratto
     * dell'interfaccia Track; ciascuno di questi metodi applicherà immediatamente le proprie logiche di
     * validazione. Successivamente, notifica esplicitamente al catalogo l'avvenuto mutamento dello stato dell'oggetto,
     * consentendo l'attivazione dei meccanismi di notifica per gli osservatori (Observer) legati alla UI.
     *
     * @param trackToUpdate L'istanza dell'oggetto Track selezionato dall'utente per la modifica.
     * @param newTitle      Il nuovo valore testuale da assegnare al titolo della traccia.
     * @param newAuthor     Il nuovo valore testuale da assegnare all'autore della traccia.
     * @param newGenre      Il nuovo genere musicale da assegnare alla traccia.
     * @param newYear       Il nuovo anno di pubblicazione da assegnare alla traccia.
     * @param newDuration   La nuova durata in secondi da assegnare alla traccia.
     * @param newFilePath   Il nuovo filepath
     * @throws IllegalArgumentException Se l'oggetto trackToUpdate risulta nullo o se i parametri violano i vincoli.
     */
    public void updateTrack(Track trackToUpdate, String newTitle, String newAuthor, String newGenre, int newYear, int newDuration, String newFilePath) {
        if (trackToUpdate == null) {
            throw new IllegalArgumentException("Nessuna traccia selezionata per la modifica.");
        }

        String oldTitle = trackToUpdate.getTitle();
        String oldAuthor = trackToUpdate.getAuthor();
        String oldGenre = trackToUpdate.getGenre();
        int oldYear = trackToUpdate.getYear();
        int oldDuration = trackToUpdate.getDuration();
        String oldFilePath = trackToUpdate.getFilePath();

        try {
            trackToUpdate.setTitle(newTitle);
            trackToUpdate.setAuthor(newAuthor);
            trackToUpdate.setGenre(newGenre);
            trackToUpdate.setYear(newYear);
            trackToUpdate.setDuration(newDuration);
            trackToUpdate.setFilePath(newFilePath);

            catalog.updateTrack(trackToUpdate);

        } catch (IllegalArgumentException e) {
            trackToUpdate.setTitle(oldTitle);
            trackToUpdate.setAuthor(oldAuthor);
            trackToUpdate.setGenre(oldGenre);
            trackToUpdate.setYear(oldYear);
            trackToUpdate.setDuration(oldDuration);
            trackToUpdate.setFilePath(oldFilePath);

            throw e;
        }
    }

    /**
     * @brief Rimuove definitivamente una determinata traccia musicale dal sistema.
     *
     * Riceve il riferimento all'oggetto Track che si intende eliminare, ne estrae l'identificativo
     * univoco immutabile (UUID) e delega l'operazione di rimozione fisica dal catalogo e la conseguente
     * cancellazione a cascata dalle playlist direttamente al MusicCatalog.
     *
     * @param trackToRemove L'istanza della traccia musicale selezionata per l'eliminazione.
     */
    public void removeTrack(Track trackToRemove) {
        if (trackToRemove != null) {
            Command cmd = new RemoveTrackCommand(trackToRemove, catalog);
            managerTrack.executeCommand(cmd);
        }
    }

    /**
     * @brief Restituisce tutte le tracce presenti nel catalogo musicale.
     *
     * Delega la richiesta direttamente al catalogo centrale, restituendo
     * una vista in sola lettura della collezione di tracce disponibili.
     * Utilizzato dalla View per popolare le liste e il pannello di selezione.
     *
     * @return Una collezione non modificabile di tutte le tracce del catalogo.
     */
    public Collection<Track> getAllTracks(){
        return catalog.getAllTracks();
    }

    public CommandManager getManager() {
        return this.managerTrack;
    }


}