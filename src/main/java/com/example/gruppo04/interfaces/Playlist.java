package com.example.gruppo04.interfaces;

import java.util.List;
/**
 * Rappresenta una playlist: una collezione <b>ordinata</b> di tracce, priva di
 * duplicati, identificata in modo stabile da un id immutabile.
 * <p>
 * Le tracce sono mantenute nell'ordine di inserimento; l'unicità all'interno della
 * playlist è garantita dall'implementazione.
 */
public interface Playlist extends PlayableSource {
    /**
     * Restituisce il nome della playlist.
     *
     * @return il nome corrente (mai {@code null} né vuoto)
     */
    String getName();

    /**
     * Rinomina la playlist.
     *
     * @param name il nuovo nome
     * @throws IllegalArgumentException se {@code nome} è {@code null} o vuoto
     */
    void setName(String name);

    /**
     * Aggiunge una traccia in coda alla playlist, se non già presente.
     *
     * @param track la traccia da aggiungere
     * @return {@code true} se la traccia è stata aggiunta, {@code false} se era
     *         già presente (in tal caso la playlist resta invariata)
     */
    boolean addTrack(Track track);

    /**
     * Rimuove una traccia dalla playlist, se presente.
     *
     * @param track la traccia da rimuovere
     * @return {@code true} se la traccia è stata rimossa, {@code false} se non
     *         era presente
     */
    boolean removeTrack(Track track);

    /**
     * Restituisce le tracce nell'ordine di inserimento.
     *
     * @return una vista in <b>sola lettura</b> delle tracce; ogni tentativo di
     *         modificarla solleva {@link UnsupportedOperationException}
     */
    List<Track> getTracks();

    /**
     * Incrementa il numero di riproduzioni della playlist.
     */
    void incrementPlayCount();

    /**
     * Restituisce il numero di riproduzioni della playlist.
     *
     * @return numero di riproduzioni registrate
     */
    int getPlayCount();

    /**
     * Sposta la traccia dalla posizione {@code from} alla posizione {@code to}.
     *
     * @param from indice di partenza della traccia
     * @param to   indice di destinazione della traccia
     */
    void moveTrack(int from, int to);
}
