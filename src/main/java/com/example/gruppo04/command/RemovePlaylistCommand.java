package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;

/**
 * Comando per la rimozione di una playlist dal catalogo.
 * Supporta l'annullamento ripristinando la <b>stessa istanza</b> di playlist
 * nella posizione originale: poiché l'oggetto (con le sue tracce e i riferimenti
 * esterni) non viene ricreato, l'identità è preservata e l'undo non può fallire
 * a metà se una traccia non è più nel catalogo.
 */
public class RemovePlaylistCommand implements Command {

    private final Playlist playlist;
    private final MusicCatalog catalog;
    private int originalIndex; // posizione originale per ripristinarla nello stesso punto
    private boolean removed;   // esito della rimozione

    /**
     * @param playlist la playlist da rimuovere
     * @param catalog  il catalogo su cui operare
     */
    public RemovePlaylistCommand(Playlist playlist, MusicCatalog catalog) {
        this.playlist = playlist;
        this.catalog = catalog;
    }

    /**
     * Memorizza la posizione originale della playlist e la rimuove dal catalogo.
     */
    @Override
    public void execute() {
        originalIndex = catalog.getPlaylists().indexOf(playlist);
        removed = catalog.deletePlaylist(playlist);
    }

    /**
     * Ripristina la stessa istanza di playlist nella posizione originale.
     */
    @Override
    public void undo() {
        catalog.addPlaylistAt(originalIndex, playlist);
    }

    /**
     * @return {@code true} se la playlist era presente ed è stata rimossa
     */
    @Override
    public boolean wasExecuted() {
        return removed;
    }
}
