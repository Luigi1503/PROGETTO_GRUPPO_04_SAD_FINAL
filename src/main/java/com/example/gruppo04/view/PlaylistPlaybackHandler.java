package com.example.gruppo04.view;

import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.interfaces.*;
import com.example.gruppo04.observer.CatalogEvent;

import javafx.scene.control.TableView;

/**
 * Gestisce tutta la logica relativa alla sincronizzazione tra
 * playback e vista della playlist.
 *
 * <p>Responsabilità:</p>
 * <ul>
 *     <li>Tracking della playlist attualmente in riproduzione</li>
 *     <li>Highlight della traccia corrente</li>
 *     <li>Sincronizzazione con eventi di playback</li>
 *     <li>Costruzione della coda di riproduzione</li>
 * </ul>
 */
public class PlaylistPlaybackHandler {

    private final PlaybackController playbackController;
    private final TableView<Track> tableTracks;

    private Playlist currentPlaylist;
    private boolean followingPlayback;

    /**
     * Costruttore.
     */
    public PlaylistPlaybackHandler(
            PlaybackController playbackController,
            TableView<Track> tableTracks,
            Playlist playlist) {

        this.playbackController = playbackController;
        this.tableTracks = tableTracks;
        this.currentPlaylist = playlist;
    }

    /**
     * Aggiorna la playlist corrente.
     */
    public void setCurrentPlaylist(Playlist playlist) {
        this.currentPlaylist = playlist;
    }

    /**
     * Evidenzia la traccia corrente se in riproduzione.
     */
    public void syncHighlight() {

        if (isActiveSource()) {
            highlight(playbackController.getCurrentTrack());
        } else {
            tableTracks.getSelectionModel().clearSelection();
        }
    }

    /**
     * Gestione evento PLAYBACK_STARTED.
     */
    public void handlePlaybackStarted(CatalogEvent event) {
        followingPlayback = true;
        syncWithPlayback();
    }

    /**
     * Gestione evento TRACK_CHANGED.
     */
    public void handleTrackChanged() {
        syncWithPlayback();
    }

    /**
     * Gestione evento PLAYBACK_STOPPED.
     */
    public void handlePlaybackStopped() {
        followingPlayback = false;
        tableTracks.getSelectionModel().clearSelection();
    }

    /**
     * Logica principale di sincronizzazione playback.
     */
    private void syncWithPlayback() {

        PlayableSource source = playbackController.getCurrentSource();

        if (followingPlayback
                && !playbackController.isStopped()
                && source instanceof Playlist playlist
                && !playlist.equals(currentPlaylist)) {

            currentPlaylist = playlist;
        }

        followingPlayback = isActiveSource();
        syncHighlight();
    }

    /**
     * Evidenzia una traccia nella tabella.
     */
    private void highlight(Track track) {

        if (track == null) {
            tableTracks.getSelectionModel().clearSelection();
            return;
        }

        int index = tableTracks.getItems().indexOf(track);

        if (index >= 0) {
            tableTracks.getSelectionModel().select(index);
            tableTracks.scrollTo(index);
        } else {
            tableTracks.getSelectionModel().clearSelection();
        }
    }

    /**
     * Verifica se la playlist è attiva in playback.
     */
    private boolean isActiveSource() {

        return playbackController != null
                && !playbackController.isStopped()
                && currentPlaylist.equals(playbackController.getCurrentSource());
    }

    /**
     * Gestisce eventi catalogo delegati dal controller.
     */
    public void handleEvent(CatalogEvent event, PlaylistDetailViewController view) {

        switch (event.getType()) {

            case PLAYBACK_STARTED:
                handlePlaybackStarted(event);
                break;

            case TRACK_CHANGED:
                handleTrackChanged();
                break;

            case PLAYBACK_STOPPED:
                handlePlaybackStopped();
                break;

            default:
                break;
        }
    }
}