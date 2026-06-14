package com.example.gruppo04.model.factory_method;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.PlaylistImpl;

/**
 * @brief ConcreteCreator del pattern Factory Method per il criterio anno/periodo.
 * @details Genera una playlist contenente esclusivamente le tracce
 * pubblicate nell'intervallo di anni specificato nel costruttore.
 */
public class YearPlaylistGenerator extends AutoPlaylistGenerator {

    /** @brief Anno di inizio del periodo di filtraggio (incluso). */
    private final int fromYear;

    /** @brief Anno di fine del periodo di filtraggio (incluso). */
    private final int toYear;

    /**
     * @brief Costruisce un generatore per il periodo di anni specificato.
     *
     * @param fromYear anno di inizio del periodo (incluso)
     * @param toYear   anno di fine del periodo (incluso)
     */
    public YearPlaylistGenerator(int fromYear, int toYear) {
        this.fromYear = fromYear;
        this.toYear = toYear;
    }

    /**
     * @brief Crea una playlist con tutte le tracce pubblicate nel periodo specificato.
     * @details Itera su tutte le tracce del catalogo e aggiunge alla playlist
     * quelle il cui anno di pubblicazione è compreso nell'intervallo definito.
     *
     * @param catalog il catalogo musicale da cui estrarre le tracce
     * @return la playlist generata contenente le tracce del periodo specificato
     */
    @Override
    public Playlist createPlaylist(MusicCatalog catalog) {
        Playlist playlist = new PlaylistImpl(getCriterionName());
        for (Track track : catalog.getAllTracks()) {
            if (track.getYear() >= fromYear && track.getYear() <= toYear) {
                playlist.addTrack(track);
            }
        }
        return playlist;
    }

    /**
     * @brief Restituisce il nome del criterio di generazione.
     *
     * @return il periodo in formato "fromYear–toYear", o solo l'anno se coincidono
     */
    @Override
    public String getCriterionName() {
        if (fromYear == toYear) {
            return String.valueOf(fromYear);
        }
        return fromYear + "–" + toYear;
    }
}
