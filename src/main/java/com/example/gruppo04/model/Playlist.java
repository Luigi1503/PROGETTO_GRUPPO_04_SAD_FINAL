package com.example.gruppo04.model;

import java.util.List;
/**
 * Rappresenta una playlist: una collezione <b>ordinata</b> di tracce, priva di
 * duplicati, identificata in modo stabile da un id immutabile.
 * <p>
 * Le tracce sono mantenute nell'ordine di inserimento; l'unicità all'interno della
 * playlist è garantita dall'implementazione.
 */
public interface Playlist {
    /**
     * Restituisce il nome della playlist.
     *
     * @return il nome corrente (mai {@code null} né vuoto)
     */
    String getNome();

    /**
     * Rinomina la playlist.
     *
     * @param nome il nuovo nome
     * @throws IllegalArgumentException se {@code nome} è {@code null} o vuoto
     */
    void setNome(String nome);

    /**
     * Aggiunge una traccia in coda alla playlist, se non già presente.
     *
     * @param track la traccia da aggiungere
     * @return {@code true} se la traccia è stata aggiunta, {@code false} se era
     *         già presente (in tal caso la playlist resta invariata)
     */
    boolean addTrack(TrackImpl track);

    /**
     * Rimuove una traccia dalla playlist, se presente.
     *
     * @param track la traccia da rimuovere
     * @return {@code true} se la traccia è stata rimossa, {@code false} se non
     *         era presente
     */
    boolean removeTrack(TrackImpl track);

    /**
     * Restituisce le tracce nell'ordine di inserimento.
     *
     * @return una vista in <b>sola lettura</b> delle tracce; ogni tentativo di
     *         modificarla solleva {@link UnsupportedOperationException}
     */
    List<TrackImpl> getTracks();
}
