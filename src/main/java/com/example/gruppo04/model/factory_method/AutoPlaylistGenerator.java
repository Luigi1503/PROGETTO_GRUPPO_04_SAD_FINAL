package com.example.gruppo04.model.factory_method;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @brief Creator astratto del pattern Factory Method.
 * @details Definisce il metodo factory astratto {@link #createPlaylist(MusicCatalog)}
 * che ogni ConcreteCreator deve implementare per generare una playlist
 * secondo il proprio criterio di filtraggio.
 */
public abstract class AutoPlaylistGenerator {

    /**
     * @brief Metodo factory astratto.
     * @details Ogni sottoclasse implementa questo metodo per creare una playlist
     * contenente esclusivamente le tracce che soddisfano il criterio specifico.
     *
     * @param catalog il catalogo musicale da cui estrarre le tracce
     * @return la playlist generata secondo il criterio del generatore
     */
    public abstract Playlist createPlaylist(MusicCatalog catalog);

    /**
     * @brief Restituisce il nome del criterio di generazione.
     * @details Usato per identificare il tipo di playlist generata
     * (es. "Rock", "2020–2024", "Preferite").
     *
     * @return il nome del criterio
     */
    public abstract String getCriterionName();

    /**
     * @brief Metodo di utilità comune a tutti i generatori.
     * @details Itera su tutte le tracce del catalogo e restituisce
     * quelle che soddisfano il predicato fornito dalla sottoclasse.
     *
     * @param catalog   il catalogo da cui estrarre le tracce
     * @param predicate il criterio di filtraggio
     * @return lista delle tracce che soddisfano il criterio
     */
    protected List<Track> filterTracks(MusicCatalog catalog, Predicate<Track> predicate) {
        List<Track> result = new ArrayList<>();
        for (Track track : catalog.getAllTracks()) {
            if (predicate.test(track)) {
                result.add(track);
            }
        }
        return result;
    }
}