package com.example.gruppo04.model.FactoryMethod;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.PlaylistImpl;

/**
 * @brief ConcreteCreator del pattern Factory Method per il criterio genere.
 * @details Genera una playlist contenente esclusivamente le tracce
 * il cui genere corrisponde a quello specificato nel costruttore.
 */
public class GenrePlaylistGenerator extends AutoPlaylistGenerator {

    /** @brief Il genere musicale usato come criterio di filtraggio. */
    private final String genre;


    /**
     * @brief Costruisce un generatore per il genere specificato.
     *
     * @param genre il genere musicale da usare come criterio
     */
    public GenrePlaylistGenerator(String genre) {
        this.genre = genre;
    }

    /**
     * @brief Crea una playlist con tutte le tracce del genere specificato.
     * @details Itera su tutte le tracce del catalogo e aggiunge alla playlist
     * quelle il cui genere corrisponde (confronto case-insensitive) al criterio.
     *
     * @param catalog il catalogo musicale da cui estrarre le tracce
     * @return la playlist generata contenente le tracce del genere specificato
     */
    @Override
    public Playlist createPlaylist(MusicCatalog catalog) {
        Playlist playlist = new PlaylistImpl(getCriterionName());
        for (Track track : catalog.getAllTracks()) {
            if (track.getGenre().equalsIgnoreCase(genre)) {
                playlist.addTrack(track);
            }
        }
        return playlist;
    }

    /**
     * @brief Restituisce il nome del criterio di generazione.
     *
     * @return il nome del genere musicale
     */
    @Override
    public String getCriterionName() {
        return genre;
    }
}