package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
/**
 * Comando per la rimozione di una traccia da una playlist.
 * Supporta l'annullamento tramite reinserimento della traccia nella playlist.
 */
public class RemoveTrackFromPlaylistCommand implements Command {

    private final Playlist playlist;
    private final Track track;
    private final MusicCatalog catalog;

    /**
     * @param playlist la playlist da cui rimuovere la traccia
     * @param track    la traccia da rimuovere
     * @param catalog  il catalogo su cui operare
     */
    public RemoveTrackFromPlaylistCommand(Playlist playlist, Track track, MusicCatalog catalog) {
        this.playlist = playlist;
        this.track = track;
        this.catalog = catalog;
    }

    /**
     * Rimuove la traccia dalla playlist.
     */
    @Override
    public void execute() {
        catalog.removeTrackFromPlaylist(playlist, track);
    }

    /**
     * Annulla la rimozione reinserendo la traccia nella playlist.
     */
    @Override
    public void undo() {
        catalog.addTrackToPlaylist(playlist, track);
    }
}

