package com.example.gruppo04.interfaces;

import java.util.UUID;

/**
 * @brief Rappresenta il contratto per una traccia musicale all'interno del sistema.
 * * Questa interfaccia definisce i metodi necessari per leggere e manipolare
 * le informazioni di un brano. L'identità di ogni traccia è garantita
 * da un identificatore UUID immutabile.
 */
public interface Track extends PlayableSource {

    /**
     * @brief Restituisce l'identificativo univoco della traccia.
     * * L'ID nasce insieme alla traccia e non può mai essere modificato.
     * È il parametro fondamentale su cui basare l'uguaglianza (equals/hashCode) degli oggetti.
     * * @return L'UUID immutabile associato alla traccia.
     */
    UUID getId();

    /**
     * @brief Restituisce il titolo della traccia.
     * * @return Una stringa contenente il titolo.
     */
    String getTitle();

    /**
     * @brief Imposta un nuovo titolo per la traccia.
     * * @param title Il nuovo titolo da assegnare.
     * @throws IllegalArgumentException se il parametro in ingresso è nullo o vuoto.
     */
    void setTitle(String title);

    /**
     * @brief Restituisce l'autore o l'artista della traccia.
     * * @return Una stringa contenente il nome dell'autore.
     */
    String getAuthor();

    /**
     * @brief Imposta un nuovo autore per la traccia.
     * * @param author Il nuovo autore da assegnare.
     * @throws IllegalArgumentException se il parametro in ingresso è nullo o vuoto.
     */
    void setAuthor(String author);

    /**
     * @brief Restituisce il genere musicale della traccia.
     * * @return Una stringa contenente il genere.
     */
    String getGenre();

    /**
     * @brief Imposta un nuovo genere musicale per la traccia.
     * * @param genre Il nuovo genere da assegnare.
     */
    void setGenre(String genre);

    /**
     * @brief Restituisce l'anno di pubblicazione della traccia.
     * * @return L'anno di pubblicazione (es. 1990).
     */
    int getYear();

    /**
     * @brief Imposta un nuovo anno di pubblicazione per la traccia.
     * * @param year L'anno da assegnare.
     * @throws IllegalArgumentException se l'anno è precedente al 1900 o se è nel futuro.
     */
    void setYear(int year);

    /**
     * @brief Restituisce la durata della traccia espressa in secondi totali.
     * * @return La durata totale in secondi.
     */
    int getDuration();

    /**
     * @brief Imposta una nuova durata per la traccia.
     * * @param duration La durata espressa in secondi totali.
     * @throws IllegalArgumentException se la durata passata è minore o uguale a zero.
     */
    void setDuration(int duration);

    /**
     * @brief Restituisce il percorso fisico del file audio associato alla traccia.
     * @return Una stringa contenente il percorso assoluto, o null se non presente.
     */
    String getFilePath();

    /**
     * @brief Imposta il percorso fisico del file audio.
     * @param filePath Il percorso assoluto del file.
     */
    void setFilePath(String filePath);

}