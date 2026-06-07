package com.example.gruppo04.observer;

import com.example.gruppo04.interfaces.Track;

/**
 * Payload trasportato dall'evento {@link CatalogEventType#PLAYBACK_STARTED}.
 * <p>
 * Contiene le informazioni minime che la barra di riproduzione
 * ({@code PlaybackBarViewController}) necessita per aggiornarsi
 * senza dover interrogare direttamente il {@code PlaybackController}:
 * </p>
 * <ul>
 *   <li>la traccia con cui inizia la riproduzione</li>
 *   <li>un flag che indica se la sorgente è una playlist o una traccia singola del catalogo,
 *       usato per decidere se rendere visibile il bottone "Skip Playlist"</li>
 * </ul>
 */
public class PlaybackStartedPayload {

    /** La traccia corrente all'avvio della riproduzione. */
    private final Track currentTrack;

    /**
     * {@code true} se la sorgente è una playlist (il bottone Skip Playlist
     * deve essere visibile), {@code false} se è una traccia singola del catalogo.
     */
    private final boolean isPlaylist;

    /**
     * Il nome della playlist in riproduzione.
     * {@code null} se la sorgente non è una playlist.
     */
    private final String playlistName;

    /**
     * Costruisce un payload per l'evento PLAYBACK_STARTED.
     *
     * @param currentTrack la traccia corrente all'avvio; non deve essere {@code null}
     * @param isPlaylist   {@code true} se si sta riproducendo una playlist
     *  @param playlistName il nome della playlist, {@code null} se non è una playlist
     */
    public PlaybackStartedPayload(Track currentTrack, boolean isPlaylist, String playlistName) {
        this.currentTrack = currentTrack;
        this.isPlaylist = isPlaylist;
        this.playlistName = playlistName;
    }

    /**
     * @return la traccia corrente all'avvio della riproduzione
     */
    public Track getCurrentTrack() {
        return currentTrack;
    }

    /**
     * @return {@code true} se la sorgente è una playlist, {@code false} se è una traccia del catalogo
     */
    public boolean isPlaylist() {
        return isPlaylist;
    }

    /**
     * @return il nome della playlist in riproduzione,
     *         {@code null} se la sorgente non è una playlist
     */
    public String getPlaylistName() {return playlistName;}
}