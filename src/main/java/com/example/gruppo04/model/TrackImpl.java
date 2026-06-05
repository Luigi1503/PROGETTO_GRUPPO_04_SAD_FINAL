package com.example.gruppo04.model;
import com.example.gruppo04.interfaces.Track;

import java.util.List;
import java.util.UUID;
import java.time.Year;

/**
 * @brief Implementazione concreta dell'interfaccia Track.
 * * Questa classe rappresenta fisicamente una traccia musicale nel sistema.
 * Gestisce lo stato interno dei dati e applica le regole di validazione
 * al momento dell'inserimento o della modifica.
 */
public class TrackImpl implements Track {

    /** * @brief Identificatore univoco della traccia.
     * @details Generato automaticamente alla creazione. Previene ambiguità nelle playlist
     * anche nel caso in cui l'utente modifichi titolo o autore in un secondo momento.
     */
    private final UUID id;

    /** @brief Il titolo del brano musicale. */
    private String title;

    /** @brief Il nome dell'artista o del gruppo autore del brano. */
    private String author;

    /** @brief Il genere musicale di appartenenza (es. Rock, Pop, Jazz). */
    private String genre;

    /** @brief L'anno di pubblicazione o rilascio della traccia. */
    private int year;

    /** @brief La durata totale della traccia espressa in secondi. */
    private int duration;

    /** @brief Il percorso assoluto del file audio nel file system locale. */
    private String filePath;


    /**
     * @brief Costruisce una nuova traccia assegnandole un UUID generato automaticamente.
     * * @param title    Il titolo della traccia.
     * @param author   L'autore o l'artista.
     * @param genre    Il genere musicale.
     * @param year     L'anno di pubblicazione.
     * @param duration La durata totale in secondi.
     *
     */
    public TrackImpl(String title, String author, String genre, int year, int duration, String filePath) {
        this.id = UUID.randomUUID();
        setTitle(title);
        setAuthor(author);
        setGenre(genre);
        setYear(year);
        setDuration(duration);
        this.filePath = filePath;
    }


    /**
     * @brief Recupera l'identificatore univoco della traccia.
     * @return L'oggetto UUID associato alla traccia.
     */
        @Override
        public UUID getId() {
            return this.id;
        }

    /**
     * @brief Recupera il titolo della traccia.
     * @return Una stringa contenente il titolo.
     */
        @Override
        public String getTitle() {
            return this.title;
        }


    /**
     * @brief Modifica il titolo della traccia.
     * @param title Il nuovo titolo da assegnare.
     * @throws IllegalArgumentException Se la stringa passata è nulla, vuota o composta solo da spazi.
     */
        @Override
        public void setTitle(String title) {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Il titolo non può essere vuoto");
            }
            this.title = title;
        }

    /**
     * @brief Recupera il nome dell'autore o dell'artista.
     * @return Una stringa contenente l'autore.
     */
        @Override
        public String getAuthor() {
            return this.author;
        }

    /**
     * @brief Modifica il nome dell'autore o dell'artista.
     * @param author Il nuovo autore da assegnare.
     * @throws IllegalArgumentException Se la stringa passata è nulla, vuota o composta solo da spazi.
     */
        @Override
        public void setAuthor(String author) {
            if (author == null || author.trim().isEmpty()) {
                throw new IllegalArgumentException("L'autore non può essere vuoto");
            }
            this.author = author;
        }

    /**
     * @brief Recupera il genere musicale della traccia.
     * @return Una stringa contenente il genere.
     */
        @Override
        public String getGenre() {
            return this.genre;
        }

    /**
     * @brief Modifica il genere musicale della traccia.
     * @param genre Il nuovo genere da assegnare.
     */
        @Override
        public void setGenre(String genre) {
            this.genre = genre;
        }

    /**
     * @brief Recupera l'anno di pubblicazione della traccia.
     * @return Un intero rappresentante l'anno di pubblicazione.
     */
        @Override
        public int getYear() {
            return this.year;
        }

    /**
     * @brief Modifica l'anno di pubblicazione della traccia.
     * @param year Il nuovo anno da assegnare.
     * @throws IllegalArgumentException Se l'anno è precedente al 1900 o successivo all'anno solare corrente.
     */
        @Override
        public void setYear(int year) {
            int now = Year.now().getValue();

            if (year < 1900 || year > now) {
                throw new IllegalArgumentException("Anno non valido. Deve essere compreso tra 1900 e " + now);
            }

            this.year = year;
        }



    /**
     * @brief Recupera la durata della traccia in secondi.
     * @return Un intero rappresentante la durata totale.
     */
        @Override
        public int getDuration() {
            return this.duration;
        }

    /**
     * @brief Modifica la durata della traccia.
     * @param duration La nuova durata da assegnare, espressa in secondi.
     * @throws IllegalArgumentException Se la durata inserita è minore o uguale a zero.
     */
        @Override
        public void setDuration(int duration) {
            if (duration <= 0) {
                throw new IllegalArgumentException("La durata deve essere maggiore di zero");
            }
            this.duration = duration;
        }

    /**
     * @brief Confronta questa traccia con un altro oggetto per verificarne l'uguaglianza.
     * * L'uguaglianza tra due tracce deve essere basata esclusivamente sul confronto
     * dei loro identificatori univoci (UUID), ignorando del tutto campi come titolo o autore.
     * * @param o L'oggetto da confrontare con la traccia corrente.
     * @return true se gli oggetti hanno lo stesso UUID, false altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackImpl track = (TrackImpl) o;
        return this.id.equals(track.id);
    }

    /**
     * @brief Restituisce il codice hash della traccia.
     * * Per rispettare il contratto di uguaglianza, il codice hash deve essere
     * generato partendo esclusivamente dall'UUID della traccia.
     * * @return Il valore hash intero.
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    /**
     * @brief Restituisce una rappresentazione testuale della traccia.
     * * Utile principalmente per scopi di logging e debugging.
     * * @return Una stringa contenente lo stato interno della traccia.
     */
    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", year=" + year +
                ", duration=" + duration +
                '}';
    }

    /**
     * @brief Recupera il percorso fisico del file audio associato alla traccia.
     * * Questo metodo restituisce lo stato interno della variabile filePath,
     * che rappresenta l'esatta locazione del file nel sistema operativo.
     * È essenziale per il passaggio del file al motore del MediaPlayer.
     * * @return Il percorso assoluto come stringa, oppure null se la traccia
     * è stata creata solo testualmente senza file associato.
     */
    @Override
    public String getFilePath() {
        return this.filePath;
    }


    /**
     * @brief Modifica o inizializza il percorso fisico del file audio.
     * * Sovrascrive l'attuale locazione del file. Non applica validazioni
     * sull'effettiva esistenza del file sul disco in questa fase, la
     * responsabilità del controllo ricade su chi tenta di riprodurlo.
     * * @param filePath La stringa contenente il nuovo percorso assoluto.
     */
    @Override
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }








    @Override
    public List<Track> getTracks() {
        //Creaiamo una lista contenente solo questa traccia
        return java.util.Collections.singletonList(this);
    }

    }


