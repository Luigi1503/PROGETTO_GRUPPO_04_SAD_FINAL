package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;

/**
 * Comando per lo spostamento di una traccia all'interno di una playlist.
 * Supporta l'annullamento tramite lo spostamento inverso.
 */
public class MoveTrackInPlaylistCommand implements Command {

    private final Playlist playlist;
    private final int from;
    private final int to;
    private final MusicCatalog catalog;

    /**
     * @param playlist la playlist in cui spostare la traccia
     * @param from     indice originale della traccia
     * @param to       indice di destinazione della traccia
     * @param catalog  il catalogo su cui operare (per la notifica agli observer)
     */
    public MoveTrackInPlaylistCommand(Playlist playlist, int from, int to, MusicCatalog catalog) {
        this.playlist = playlist;
        this.from = from;
        this.to = to;
        this.catalog = catalog;
    }

    /**
     * Sposta la traccia dalla posizione {@code from} a {@code to}.
     */
    @Override
    public void execute() {
        catalog.moveTrackInPlaylist(playlist, from, to);
    }

    /**
     * Annulla lo spostamento riportando la traccia alla posizione originale.
     */
    @Override
    public void undo() {
        catalog.moveTrackInPlaylist(playlist, to, from);
    }
}
