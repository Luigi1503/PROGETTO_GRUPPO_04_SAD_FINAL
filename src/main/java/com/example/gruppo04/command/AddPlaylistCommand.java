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
     * Crea la playlist nel catalogo e ne salva l'istanza esatta per l'undo.
     * Usa il valore di ritorno di {@code createPlaylist} invece di {@code getLast()},
     * così da preservare l'identità dell'oggetto ed evitare di agganciare per errore
     * una playlist diversa (es. generata nel frattempo).
     */
    @Override
    public void execute() {
        playlistCreated = catalog.createPlaylist(name);
    }

    /**
     * Annulla la creazione eliminando la playlist dal catalogo.
     * Se la creazione non era andata a buon fine (nome duplicato), l'undo è un no-op.
     */
    @Override
    public void undo() {
        if (playlistCreated != null) {
            catalog.deletePlaylist(playlistCreated);
        }
    }

    /**
     * @return {@code true} se la playlist è stata effettivamente creata,
     *         {@code false} se il nome era duplicato
     */
    @Override
    public boolean wasExecuted() {
        return playlistCreated != null;
    }
}