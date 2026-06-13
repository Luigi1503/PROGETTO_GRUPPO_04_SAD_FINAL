package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando per la rimozione di una playlist dal catalogo.
 * Supporta l'annullamento tramite ripristino della playlist con le sue tracce.
 */
public class RemovePlaylistCommand implements Command {

    private final Playlist playlist;
    private final MusicCatalog catalog;
    private List<Track> tracksBackup; // salva le tracce per il ripristino

    /**
     * @param playlist la playlist da rimuovere
     * @param catalog  il catalogo su cui operare
     */
    public RemovePlaylistCommand(Playlist playlist, MusicCatalog catalog) {
        this.playlist = playlist;
        this.catalog = catalog;
    }

    /**
     * Salva le tracce della playlist e la rimuove dal catalogo.
     */
    @Override
    public void execute() {
        tracksBackup = new ArrayList<>(playlist.getTracks());
        catalog.deletePlaylist(playlist);
    }

    /**
     * Ripristina la playlist con le tracce che conteneva prima della rimozione.
     */
    @Override
    public void undo() {
        catalog.createPlaylist(playlist.getName());
        Playlist reloaded = catalog.getPlaylists().getLast();
        for (Track t : tracksBackup) {
            catalog.addTrackToPlaylist(reloaded, t);
        }
    }
}
