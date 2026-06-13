package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Track;
/**
 * Comando per la rimozione di una traccia dal catalogo.
 * Supporta l'annullamento tramite reinserimento della traccia rimossa.
 */
public class RemoveTrackCommand implements Command {

    private final Track track;
    private final MusicCatalog catalog;

    /**
     * @param track   la traccia da rimuovere dal catalogo
     * @param catalog il catalogo su cui operare
     */
    public RemoveTrackCommand(Track track, MusicCatalog catalog) {
        this.track = track;
        this.catalog = catalog;
    }

    /**
     * Rimuove la traccia dal catalogo.
     */
    @Override
    public void execute() {
        catalog.removeTrack(track.getId());
    }

    /**
     * Annulla la rimozione reinserendo la traccia nel catalogo.
     */
    @Override
    public void undo() {
        catalog.addTrack(track);
    }
}