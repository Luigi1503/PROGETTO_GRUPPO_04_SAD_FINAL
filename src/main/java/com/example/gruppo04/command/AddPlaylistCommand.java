package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
/**
 * Comando per la creazione di una nuova playlist nel catalogo.
 * Supporta l'annullamento tramite eliminazione della playlist creata.
 */
public class AddPlaylistCommand implements Command {

    private final MusicCatalog catalog;
    private final String name;
    private Playlist playlistCreated; // riferimento salvato per l'undo

    /**
     * @param catalog il catalogo su cui operare
     * @param name    il nome della playlist da creare
     */
    public AddPlaylistCommand(MusicCatalog catalog, String name) {
        this.catalog = catalog;
        this.name = name;
    }

    /**
     * Crea la playlist nel catalogo e ne salva il riferimento per l'undo.
     */
    @Override
    public void execute() {
        catalog.createPlaylist(name);
        playlistCreated = catalog.getPlaylists().getLast();
    }

    /**
     * Annulla la creazione eliminando la playlist dal catalogo.
     */
    @Override
    public void undo() {
        catalog.deletePlaylist(playlistCreated);
    }
}