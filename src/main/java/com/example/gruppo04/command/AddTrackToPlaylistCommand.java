package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
/**
 * Comando per l'aggiunta di una traccia a una playlist.
 * Supporta l'annullamento tramite rimozione della traccia dalla playlist.
 */
public class AddTrackToPlaylistCommand implements Command {

    private final Track track;
    private final MusicCatalog catalog;
    private final Playlist playlist;

    /**
     * @param track    la traccia da aggiungere alla playlist
     * @param catalog  il catalogo su cui operare
     * @param playlist la playlist di destinazione
     */
    public AddTrackToPlaylistCommand(Track track, MusicCatalog catalog, Playlist playlist) {
        this.track = track;
        this.catalog = catalog;
        this.playlist = playlist;
    }

    /**
     * Aggiunge la traccia alla playlist.
     */
    @Override
    public void execute() {
        catalog.addTrackToPlaylist(playlist, track);
    }

    /**
     * Annulla l'aggiunta rimuovendo la traccia dalla playlist.
     */
    @Override
    public void undo() {
        catalog.removeTrackFromPlaylist(playlist, track);
    }
}