package com.example.gruppo04.model.FactoryMethod;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.PlaylistImpl;
import com.example.gruppo04.model.TagType;

/**
 * @brief ConcreteCreator del pattern Factory Method per il criterio tag.
 * @details Genera una playlist contenente esclusivamente le tracce
 * che hanno il tag specificato nel costruttore.
 */
public class TagPlaylistGenerator extends AutoPlaylistGenerator {

    /** @brief Il tag usato come criterio di filtraggio. */
    private final TagType tag;

    /**
     * @brief Costruisce un generatore per il tag specificato.
     *
     * @param tag il tag da usare come criterio; non deve essere {@code null}
     */
    public TagPlaylistGenerator(TagType tag) {
        this.tag = tag;
    }

    /**
     * @brief Crea una playlist con tutte le tracce che hanno il tag specificato.
     * @details Itera su tutte le tracce del catalogo e aggiunge alla playlist
     * quelle che hanno il tag corrispondente al criterio.
     *
     * @param catalog il catalogo musicale da cui estrarre le tracce
     * @return la playlist generata contenente le tracce con il tag specificato
     */
    @Override
    public Playlist createPlaylist(MusicCatalog catalog) {
        Playlist playlist = new PlaylistImpl(getCriterionName());
        for (Track track : catalog.getAllTracks()) {
            if (track.hasTag(tag)) {
                playlist.addTrack(track);
            }
        }
        return playlist;
    }

    /**
     * @brief Restituisce il nome del criterio di generazione.
     *
     * @return il nome del tag in formato leggibile
     */
    @Override
    public String getCriterionName() {
        return switch (tag) {
            case FAVOURITE -> "Preferite";
            case EXPLICIT -> "Esplicite";
            case NEW_RELEASE -> "Nuove Uscite";
        };
    }
}