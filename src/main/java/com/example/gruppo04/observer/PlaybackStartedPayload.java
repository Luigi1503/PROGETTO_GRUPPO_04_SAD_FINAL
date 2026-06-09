package com.example.gruppo04.observer;

import com.example.gruppo04.interfaces.PlayableSource;
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
    private final PlayableSource currentSource;

    /**
     * Costruisce un payload per l'evento PLAYBACK_STARTED.
     *
     * @param currentTrack la traccia corrente all'avvio; non deve essere {@code null}
     * @param isPlaylist   {@code true} se si sta riproducendo una playlist
     *  @param currentSource la sorgente corrente (playlist o traccia singola)
     */
    public PlaybackStartedPayload(Track currentTrack, boolean isPlaylist, PlayableSource currentSource) {
        this.currentTrack = currentTrack;
        this.isPlaylist = isPlaylist;
        this.currentSource = currentSource;
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
     * @return la source in riproduzione
     */
    public PlayableSource getCurrentSource() {
        return currentSource;
    }
}