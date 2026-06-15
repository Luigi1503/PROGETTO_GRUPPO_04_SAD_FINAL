package com.example.gruppo04.model.factory_method;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.TagType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servizio singleton responsabile della gestione delle playlist automatiche.
 *
 * <p>Le playlist vengono generate tramite diversi {@link AutoPlaylistGenerator}
 * (tag, genere, anno, ecc.) e mantenute in modo stabile nel tempo.</p>
 *
 * <p>Il servizio garantisce che:
 * <ul>
 *     <li>le playlist non vengano ricreate da zero ad ogni aggiornamento</li>
 *     <li>i metadati runtime (es. numero di riproduzioni) non vengano persi</li>
 *     <li>i brani siano sincronizzati con il catalogo corrente</li>
 * </ul>
 * </p>
 *
 * <p>Pattern utilizzato: Singleton + Factory Method.</p>
 */
public final class AutomaticPlaylistService {

    /** Istanza singleton del servizio. */
    private static final AutomaticPlaylistService INSTANCE =
            new AutomaticPlaylistService();

    /**
     * Insieme di possibili generatori di playlist automatiche.
     * Ogni generatore produce una playlist basata su una regola diversa.
     */
    private static final List<AutoPlaylistGenerator> ALL_AVAILABLE_GENERATORS = List.of(
            new TagPlaylistGenerator(TagType.FAVOURITE),
            new TagPlaylistGenerator(TagType.NEW_RELEASE),
            new TagPlaylistGenerator(TagType.EXPLICIT),
            new GenrePlaylistGenerator("Pop"),
            new GenrePlaylistGenerator("Rock"),
            new GenrePlaylistGenerator("Jazz"),
            new GenrePlaylistGenerator("Classica"),
            new GenrePlaylistGenerator("Hip-Hop"),
            new GenrePlaylistGenerator("Electronic"),
            new GenrePlaylistGenerator("Reggae"),
            new YearPlaylistGenerator(1960, 1970),
            new YearPlaylistGenerator(1971, 1980),
            new YearPlaylistGenerator(1981, 1990),
            new YearPlaylistGenerator(1991, 2000),
            new YearPlaylistGenerator(2001, 2010),
            new YearPlaylistGenerator(2011, 2020),
            new YearPlaylistGenerator(2021, 2026)
    );

    /**
     * Generatori attivi di playlist automatiche.
     * Ogni generatore produce una playlist basata su una regola diversa.
     */
    private final List<AutoPlaylistGenerator> generators = new ArrayList<>(List.of(
            new TagPlaylistGenerator(TagType.FAVOURITE),
            new TagPlaylistGenerator(TagType.NEW_RELEASE),
            new GenrePlaylistGenerator("Pop"),
            new GenrePlaylistGenerator("Rock"),
            new YearPlaylistGenerator(1991, 2000)
    ));


    /**
     * Mappa delle playlist persistenti.
     *
     * <p>La chiave è il nome della playlist, mentre il valore è l’istanza
     * stabile che mantiene i metadati runtime.</p>
     */
    private final Map<String, Playlist> playlistsByName =
            new LinkedHashMap<>();

    /**
     * Costruttore privato per garantire il pattern Singleton.
     */
    private AutomaticPlaylistService() {}

    /**
     * Restituisce l’istanza singleton del servizio.
     *
     * @return istanza unica di {@code AutomaticPlaylistService}
     */
    public static AutomaticPlaylistService getInstance() {
        return INSTANCE;
    }

    /**
     * Aggiorna o sincronizza tutte le playlist automatiche
     * a partire dallo stato corrente del catalogo.
     *
     * <p>Se una playlist non esiste ancora viene creata,
     * altrimenti viene aggiornata preservando l’istanza esistente.</p>
     *
     * @param catalog catalogo musicale di riferimento
     * @return lista non modificabile delle playlist automatiche aggiornate
     */
    public synchronized List<Playlist> refresh(MusicCatalog catalog) {
        for (AutoPlaylistGenerator generator : generators) {
            Playlist generated = generator.createPlaylist(catalog);

            Playlist stable = playlistsByName.computeIfAbsent(
                    generated.getName(),
                    name -> generated
            );

            syncTracks(stable, generated.getTracks());
        }
        return getPlaylists();
    }

    /**
     * Restituisce tutte le playlist automatiche attualmente gestite.
     *
     * <p>La lista è immutabile per evitare modifiche esterne.</p>
     *
     * @return lista delle playlist automatiche
     */
    public synchronized List<Playlist> getPlaylists() {
        return Collections.unmodifiableList(
                new ArrayList<>(playlistsByName.values())
        );
    }

    /**
     * Sincronizza i brani di una playlist mantenendo l’istanza stabile.
     *
     * <p>Rimuove i brani non più presenti e aggiunge quelli nuovi
     * senza ricreare la playlist, preservando i metadati runtime.</p>
     *
     * @param target playlist da aggiornare
     * @param desiredTracks lista aggiornata di brani desiderati
     */
    private void syncTracks(Playlist target, List<Track> desiredTracks) {

        // Rimuove i brani non più presenti
        for (Track track : new ArrayList<>(target.getTracks())) {
            if (!desiredTracks.contains(track)) {
                target.removeTrack(track);
            }
        }

        // Aggiunge i nuovi brani
        for (Track track : desiredTracks) {
            if (!target.getTracks().contains(track)) {
                target.addTrack(track);
            }
        }
    }

    /**
     * @brief Restituisce i generatori disponibili non ancora attivi.
     * @details Filtra {@link #generators} escludendo quelli
     * il cui criterio corrisponde ad una playlist già presente.
     *
     * @return lista dei generatori disponibili per l'aggiunta
     */
    public synchronized List<AutoPlaylistGenerator> getAvailableGenerators() {
        List<String> existingNames = playlistsByName.keySet().stream().toList();
        return ALL_AVAILABLE_GENERATORS.stream()
                .filter(g -> !existingNames.contains(g.getCriterionName()))
                .toList();
    }

    /**
     * @brief Aggiunge un generatore personalizzato alla lista dei generatori attivi.
     * @details Il generatore viene aggiunto solo se non esiste già un criterio
     * con lo stesso nome. Dopo l'aggiunta il generatore sarà incluso nelle
     * chiamate successive a {@link #refresh(MusicCatalog)}.
     *
     * @param generator il generatore da aggiungere; non deve essere {@code null}
     */
    public synchronized void addCustomGenerator(AutoPlaylistGenerator generator) {
        if (generator == null) return;
        boolean exists = generators.stream()
                .anyMatch(g -> g.getCriterionName()
                        .equalsIgnoreCase(generator.getCriterionName()));
        if (!exists)
            generators.add(generator);
    }


    /**
     * @brief Rimuove il generatore con il criterio specificato.
     *
     * @param criterionName il nome del criterio da rimuovere
     */
    public synchronized void removeGenerator(String criterionName) {
        generators.removeIf(g ->
                g.getCriterionName().equalsIgnoreCase(criterionName));
        playlistsByName.remove(criterionName);
    }


}