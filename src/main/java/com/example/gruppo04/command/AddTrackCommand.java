package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Track;
/**
 * Comando per l'aggiunta di una traccia al catalogo.
 * Supporta l'annullamento tramite rimozione della traccia aggiunta.
 */
public class AddTrackCommand implements Command {

    private final Track track;
    private final MusicCatalog catalog;

    /**
     * @param track   la traccia da aggiungere al catalogo
     * @param catalog il catalogo su cui operare
     */
    public AddTrackCommand(Track track, MusicCatalog catalog) {
        this.track = track;
        this.catalog = catalog;
    }

    /**
     * Aggiunge la traccia al catalogo.
     */
    @Override
    public void execute() {
        catalog.addTrack(track);
    }

    /**
     * Annulla l'aggiunta rimuovendo la traccia dal catalogo.
     */
    @Override
    public void undo() {
        catalog.removeTrack(track.getId());
    }
}