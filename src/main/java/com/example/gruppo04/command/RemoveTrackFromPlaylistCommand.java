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
    private int originalIndex; // posizione originale nella playlist per il ripristino
    private boolean removed;   // esito della rimozione

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
     * Memorizza la posizione della traccia e la rimuove dalla playlist.
     */
    @Override
    public void execute() {
        originalIndex = playlist.getTracks().indexOf(track);
        removed = catalog.removeTrackFromPlaylist(playlist, track);
    }

    /**
     * Annulla la rimozione reinserendo la traccia nella stessa posizione di prima.
     */
    @Override
    public void undo() {
        if (originalIndex >= 0) {
            catalog.addTrackToPlaylistAt(playlist, track, originalIndex);
        } else {
            catalog.addTrackToPlaylist(playlist, track);
        }
    }

    /**
     * @return {@code true} se la traccia era presente nella playlist ed è stata rimossa
     */
    @Override
    public boolean wasExecuted() {
        return removed;
    }
}

