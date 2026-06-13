package com.example.gruppo04.model.FactoryMethod;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;

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
}