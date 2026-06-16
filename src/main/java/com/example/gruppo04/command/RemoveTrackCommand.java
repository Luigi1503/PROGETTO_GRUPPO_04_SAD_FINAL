package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * Comando per la rimozione di una traccia dal catalogo.
 * Supporta l'annullamento reinserendo la traccia nella stessa posizione del
 * catalogo e ripristinando le appartenenze alle playlist (con le rispettive
 * posizioni) da cui era stata rimossa a cascata.
 */
public class RemoveTrackCommand implements Command {

    private final Track track;
    private final MusicCatalog catalog;

    /** Posizione originale della traccia nell'ordine del catalogo. */
    private int catalogIndex;
    /** Playlist che contenevano la traccia, con la posizione che vi occupava. */
    private final Map<Playlist, Integer> playlistPositions = new LinkedHashMap<>();

    /**
     * @param track   la traccia da rimuovere dal catalogo
     * @param catalog il catalogo su cui operare
     */
    public RemoveTrackCommand(Track track, MusicCatalog catalog) {
        this.track = track;
        this.catalog = catalog;
    }

    /**
     * Memorizza la posizione nel catalogo e nelle playlist, poi rimuove la traccia.
     */
    @Override
    public void execute() {
        List<Track> allTracks = new ArrayList<>(catalog.getAllTracks());
        catalogIndex = allTracks.indexOf(track);

        playlistPositions.clear();
        for (Playlist p : catalog.getPlaylists()) {
            int idx = p.getTracks().indexOf(track);
            if (idx >= 0) {
                playlistPositions.put(p, idx);
            }
        }

        catalog.removeTrack(track.getId());
    }

    /**
     * Annulla la rimozione: reinserisce la traccia nel catalogo nella sua
     * posizione originale e la ripristina in tutte le playlist che la contenevano,
     * mantenendo l'ordine.
     */
    @Override
    public void undo() {
        catalog.addTrackAt(catalogIndex, track);

        for (Map.Entry<Playlist, Integer> entry : playlistPositions.entrySet()) {
            catalog.addTrackToPlaylistAt(entry.getKey(), track, entry.getValue());
        }

        // Forza una notifica di update per assicurare che tutte le view
        // (es. HomeView con la classifica "più ascoltate") ricalcolino
        // correttamente e mostrino la traccia ripristinata.
        catalog.notifyTrackUpdated(track);
    }
}