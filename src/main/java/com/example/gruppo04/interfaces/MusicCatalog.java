package com.example.gruppo04.interfaces;

import com.example.gruppo04.model.strategy.PlaybackStrategy;
import com.example.gruppo04.observer.CatalogObserver;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MusicCatalog {

    /**
     * Aggiunge una nuova traccia al catalogo.
     * In caso di successo notifica gli observer con l'evento {@code TRACK_ADDED}.
     *
     * @param track la traccia da aggiungere; non deve essere {@code null}
     * @throws IllegalArgumentException se {@code track} è {@code null} o se esiste
     * già una traccia con lo stesso titolo nel catalogo
     */
    void addTrack(Track track);

    /**
     * Rimuove una traccia dal catalogo dato il suo identificatore univoco (UUID).
     * <p>
     * <b>Effetto a cascata:</b> La rimozione di una traccia dal catalogo la elimina
     * automaticamente da tutte le playlist in cui è inclusa.
     * </p>
     * Se la traccia è trovata e rimossa, notifica gli observer con l'evento {@code TRACK_REMOVED}.
     *
     * @param trackId l'identificatore univoco della traccia da rimuovere
     */
    void removeTrack(UUID trackId);

    /**
     * Crea una nuova playlist con il name indicato e la aggiunge al catalogo.
     * In caso di successo notifica gli observer con l'evento {@code PLAYLIST_ADDED}.
     *
     * @param name il nome della nuova playlist; non {@code null}, non vuoto e
     *             univoco rispetto alle playlist già presenti
     * @return {@code true} se la playlist è stata creata, {@code false} se
     *         esiste già una playlist con lo stesso name
     * @throws IllegalArgumentException se {@code name} è {@code null} o vuoto
     */

    /**
     * Aggiorna i dati di una traccia esistente nel catalogo.
     * In caso di successo notifica gli observer con l'evento {@code TRACK_UPDATED}.
     *
     * @param updatedTrack la traccia con i dati aggiornati; il suo ID deve corrispondere
     * a una traccia già presente nel catalogo
     * @throws IllegalArgumentException se {@code updatedTrack} è {@code null} o non
     * corrisponde a nessuna traccia nota nel catalogo
     */
    public void updateTrack(Track updatedTrack);

    boolean createPlaylist(String name);

    /**
     * Rinomina una playlist già presente nel catalogo.
     * In caso di successo notifica gli observer con l'evento {@code PLAYLIST_RENAMED}.
     *
     * @param playlist  la playlist da rinominare; deve appartenere al catalogo
     * @param newName il nuovo nome; non {@code null}, non vuoto e univoco
     *                  rispetto alle altre playlist
     * @return {@code true} se rinominata, {@code false} se la playlist non è nel
     *         catalogo o se il nuovo nome è già usato da un'altra playlist
     * @throws IllegalArgumentException se {@code newName} è {@code null} o vuoto
     */
    boolean renamePlaylist(Playlist playlist, String newName);

    /**
     * Rimuove una playlist dal catalogo. Elimina solo la playlist, non tocca le
     * tracce del catalogo. In caso di successo notifica gli observer con
     * l'evento {@code PLAYLIST_REMOVED}.
     *
     * @param playlist la playlist da rimuovere
     * @return {@code true} se rimossa, {@code false} se non era presente nel catalogo
     */
    boolean deletePlaylist(Playlist playlist);

    /**
     * Restituisce tutte le playlist del catalogo, nell'ordine di inserimento.
     *
     * @return una vista in sola lettura delle playlist (mai {@code null}; vuota
     *         se non ce ne sono); ogni tentativo di modifica solleva
     *         {@link UnsupportedOperationException}
     */
    List<Playlist> getPlaylists();

    /**
     * Aggiunge una traccia ad una playlist del catalogo.
     * In caso di successo notifica gli observer con l'evento {@code PLAYLIST_TRACK_ADDED}.
     *
     * @param playlist la playlist a cui aggiungere la traccia
     * @param track    la traccia da aggiungere
     * @return {@code true} se aggiunta, {@code false} se la traccia è già
     *         presente nella playlist
     */
    boolean addTrackToPlaylist(Playlist playlist, Track track);

    /**
     * Rimuove una traccia da una playlist del catalogo.
     * In caso di successo notifica gli observer con l'evento {@code PLAYLIST_TRACK_REMOVED}.
     *
     * @param playlist la playlist da cui rimuovere la traccia
     * @param track    la traccia da rimuovere
     * @return {@code true} se rimossa, {@code false} se la traccia non è
     *         presente nella playlist
     */
    boolean removeTrackFromPlaylist(Playlist playlist, Track track);

    /**
     * Restituisce tutte le tracce presenti nel catalogo, mantenendo l'ordine di inserimento.
     * @return Collezione di tutte le tracce presenti in catalogo
     */
    public Collection<Track> getAllTracks();

    /**
     * Registra un nuovo observer per ascoltare le modifiche del catalogo.
     *
     * @param observer l'ascoltatore da registrare; non {@code null}
     */
    void registerObserver(CatalogObserver observer);

    /**
     * Rimuove un observer precedentemente registrato.
     *
     * @param observer l'ascoltatore da rimuovere
     */
    void unregisterObserver(CatalogObserver observer);

    /**
     * Notifica tutti gli observer registrati del cambio di modalità
     * di riproduzione attiva.
     *
     * @param strategy la nuova strategia di riproduzione selezionata;
     *                 non deve essere {@code null}
     */
    public void notifyStrategyChanged(PlaybackStrategy strategy);


    /**
     * Notifica tutti gli observer registrati dell'inizio di una riproduzione
     *
     * @param currentTrack la prima canzone da cui iniziare la riproduzione
     * @param isPlaylist boolean che verifica se la riproduzione è in una playlist o è una playlist
     */
    public void notifyPlaybackStarted(Track currentTrack, boolean isPlaylist, PlayableSource currentSource);

    /**
     * Notifica tutti gli observer registrati dell'inizio di una riproduzione
     * per evidenziare la traccia attualmente in riproduzione
     *
     * @param track la canzone attuale
     */
    public void notifyTrackChanged(Track track);

    /**
     * Notifica tutti gli observer registrati che la riproduzione è stata
     * fermata del tutto, così che le viste possano azzerare l'evidenziazione
     * della traccia in riproduzione.
     */
    public void notifyPlaybackStopped();
}
