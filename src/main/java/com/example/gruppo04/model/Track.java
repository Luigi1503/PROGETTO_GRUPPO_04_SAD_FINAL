package com.example.gruppo04.model;
import java.util.UUID;
import java.time.Year;

/**
 * @brief Implementazione concreta dell'interfaccia ITrack.
 * * Questa classe rappresenta fisicamente una traccia musicale nel sistema.
 * Gestisce lo stato interno dei dati e applica le regole di validazione
 * al momento dell'inserimento o della modifica.
 */
public class Track implements ITrack  {

        private final UUID id; //Questo UUID è stato inserito in modo da dare un id univoco alla traccia in modo tale che quando verranno modificati i campi dall'utente non ci saranno ambiguità nelle playlist e non verranno perse le tracce
        private String title;
        private String author;
        private String genre;
        private int year;
        private int duration;

    /**
     * @brief Costruisce una nuova traccia assegnandole un UUID generato automaticamente.
     * * @param title    Il titolo della traccia.
     * @param author   L'autore o l'artista.
     * @param genre    Il genere musicale.
     * @param year     L'anno di pubblicazione.
     * @param duration La durata totale in secondi.
     */
        public Track(String title, String author, String genre, int year, int duration) {
            this.id = UUID.randomUUID();
            this.title = title;
            this.author = author;
            this.genre = genre;
            this.year = year;
            this.duration = duration;
        }


        @Override
        public UUID getId() {
            return this.id;
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public void setTitle(String title) {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Il titolo non può essere vuoto");
            }
            this.title = title;
        }

        @Override
        public String getAuthor() {
            return this.author;
        }

        @Override
        public void setAuthor(String author) {
            if (author == null || author.trim().isEmpty()) {
                throw new IllegalArgumentException("L'autore non può essere vuoto");
            }
            this.author = author;
        }

        @Override
        public String getGenre() {
            return this.genre;
        }

        @Override
        public void setGenre(String genre) {
            this.genre = genre;
        }

        @Override
        public int getYear() {
            return this.year;
        }

        @Override
        public void setYear(int year) {
            int annoAttuale = Year.now().getValue();

            if (year < 1900 || year > annoAttuale) {
                throw new IllegalArgumentException("Anno non valido. Deve essere compreso tra 1900 e " + annoAttuale);
            }

            this.year = year;
        }


        @Override
        public int getDuration() {
            return this.duration;
        }

        @Override
        public void setDuration(int duration) {
            if (duration <= 0) {
                throw new IllegalArgumentException("La durata deve essere maggiore di zero");
            }
            this.duration = duration;
        }
    }


