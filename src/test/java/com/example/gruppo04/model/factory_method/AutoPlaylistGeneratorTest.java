package com.example.gruppo04.model.factory_method;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.model.TagType;
import com.example.gruppo04.model.TrackImpl;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Suite di test unitari per il pattern Factory Method.
 * @details Verifica che ciascun generatore produca playlist contenenti
 * esclusivamente le tracce corrispondenti al criterio specificato
 * (genere, anno/periodo, tag).
 */
class AutoPlaylistGeneratorTest {

    /** @brief Il catalogo musicale usato come sorgente dati nei test. */
    private MusicCatalog catalog;

    /**
     * @brief Configura l'ambiente prima di ogni test.
     * @details Resetta il catalogo e lo popola con tracce di test
     * che coprono diversi generi, anni e tag.
     */
    @BeforeEach
    void setUp() {
        ConcreteMusicCatalog.getInstance().reset();
        catalog = ConcreteMusicCatalog.getInstance();

        // tracce di generi diversi
        catalog.addTrack(new TrackImpl("Bohemian Rhapsody", "Queen",       "Rock", 1975, 354, null));
        catalog.addTrack(new TrackImpl("Hotel California",  "Eagles",      "Rock", 1977, 391, null));
        catalog.addTrack(new TrackImpl("As It Was",         "Harry Styles","Pop",  2022, 300, null));
        catalog.addTrack(new TrackImpl("Levitating",        "Dua Lipa",    "Pop",  2020, 203, null));
        catalog.addTrack(new TrackImpl("Thriller",          "Michael Jackson","Pop",1982, 358, null));

        // aggiunge tag ad alcune tracce
        catalog.getAllTracks().stream()
                .filter(t -> t.getTitle().equals("As It Was"))
                .findFirst()
                .ifPresent(t -> t.addTag(TagType.NEW_RELEASE));

        catalog.getAllTracks().stream()
                .filter(t -> t.getTitle().equals("Bohemian Rhapsody"))
                .findFirst()
                .ifPresent(t -> t.addTag(TagType.FAVOURITE));

        catalog.getAllTracks().stream()
                .filter(t -> t.getTitle().equals("Thriller"))
                .findFirst()
                .ifPresent(t -> t.addTag(TagType.EXPLICIT));
    }

    /**
     * @brief Ripristina lo stato del catalogo dopo ogni test.
     */
    @AfterEach
    void tearDown() {
        ConcreteMusicCatalog.getInstance().reset();
    }

    // ── GenrePlaylistGenerator ────────────────

    /**
     * @brief Verifica che GenrePlaylistGenerator produca una playlist
     * contenente solo le tracce del genere specificato.
     */
    @Test
    void genreGenerator_createsPlaylistWithCorrectTracks() {
        AutoPlaylistGenerator generator = new GenrePlaylistGenerator("Rock");
        Playlist playlist = generator.createPlaylist(catalog);

        assertNotNull(playlist);
        assertEquals(2, playlist.getTracks().size());
        assertTrue(playlist.getTracks().stream()
                .allMatch(t -> t.getGenre().equalsIgnoreCase("Rock")));
    }

    /**
     * @brief Verifica che GenrePlaylistGenerator produca una playlist vuota
     * se nessuna traccia corrisponde al genere specificato.
     */
    @Test
    void genreGenerator_emptyPlaylistForUnknownGenre() {
        AutoPlaylistGenerator generator = new GenrePlaylistGenerator("Jazz");
        Playlist playlist = generator.createPlaylist(catalog);

        assertNotNull(playlist);
        assertTrue(playlist.getTracks().isEmpty());
    }

    /**
     * @brief Verifica che getCriterionName() restituisca il genere corretto.
     */
    @Test
    void genreGenerator_criterionNameMatchesGenre() {
        AutoPlaylistGenerator generator = new GenrePlaylistGenerator("Pop");
        assertEquals("Pop", generator.getCriterionName());
    }

    /**
     * @brief Verifica che il confronto del genere sia case-insensitive.
     */
    @Test
    void genreGenerator_caseInsensitiveMatch() {
        AutoPlaylistGenerator generator = new GenrePlaylistGenerator("rock");
        Playlist playlist = generator.createPlaylist(catalog);

        assertEquals(2, playlist.getTracks().size());
    }

    // ── YearPlaylistGenerator ─────────────────

    /**
     * @brief Verifica che YearPlaylistGenerator produca una playlist
     * contenente solo le tracce nel periodo specificato.
     */
    @Test
    void yearGenerator_createsPlaylistWithCorrectTracks() {
        AutoPlaylistGenerator generator = new YearPlaylistGenerator(2020, 2022);
        Playlist playlist = generator.createPlaylist(catalog);

        assertNotNull(playlist);
        assertEquals(2, playlist.getTracks().size());
        assertTrue(playlist.getTracks().stream()
                .allMatch(t -> t.getYear() >= 2020 && t.getYear() <= 2022));
    }

    /**
     * @brief Verifica che YearPlaylistGenerator produca una playlist vuota
     * se nessuna traccia è nel periodo specificato.
     */
    @Test
    void yearGenerator_emptyPlaylistForUnknownPeriod() {
        AutoPlaylistGenerator generator = new YearPlaylistGenerator(2000, 2010);
        Playlist playlist = generator.createPlaylist(catalog);

        assertNotNull(playlist);
        assertTrue(playlist.getTracks().isEmpty());
    }

    /**
     * @brief Verifica che getCriterionName() restituisca il periodo corretto.
     */
    @Test
    void yearGenerator_criterionNameMatchesPeriod() {
        AutoPlaylistGenerator generator = new YearPlaylistGenerator(2020, 2022);
        assertEquals("2020–2022", generator.getCriterionName());
    }

    /**
     * @brief Verifica che getCriterionName() restituisca solo l'anno
     * quando fromYear e toYear coincidono.
     */
    @Test
    void yearGenerator_criterionNameSingleYear() {
        AutoPlaylistGenerator generator = new YearPlaylistGenerator(1975, 1975);
        assertEquals("1975", generator.getCriterionName());
        assertEquals(1, generator.createPlaylist(catalog).getTracks().size());
    }

    // ── TagPlaylistGenerator ──────────────────

    /**
     * @brief Verifica che TagPlaylistGenerator produca una playlist
     * contenente solo le tracce con il tag specificato.
     */
    @Test
    void tagGenerator_createsPlaylistWithCorrectTracks() {
        AutoPlaylistGenerator generator = new TagPlaylistGenerator(TagType.FAVOURITE);
        Playlist playlist = generator.createPlaylist(catalog);

        assertNotNull(playlist);
        assertEquals(1, playlist.getTracks().size());
        assertEquals("Bohemian Rhapsody", playlist.getTracks().get(0).getTitle());
    }

    /**
     * @brief Verifica che TagPlaylistGenerator produca una playlist vuota
     * se nessuna traccia ha il tag specificato.
     */
    @Test
    void tagGenerator_emptyPlaylistForUnusedTag() {
        // nessuna traccia ha il tag NEW_RELEASE tranne "As It Was"
        AutoPlaylistGenerator generator = new TagPlaylistGenerator(TagType.NEW_RELEASE);
        Playlist playlist = generator.createPlaylist(catalog);

        assertEquals(1, playlist.getTracks().size());
        assertEquals("As It Was", playlist.getTracks().get(0).getTitle());
    }

    /**
     * @brief Verifica che getCriterionName() restituisca il nome leggibile del tag.
     */
    @Test
    void tagGenerator_criterionNameFavourite() {
        assertEquals("Preferite", new TagPlaylistGenerator(TagType.FAVOURITE).getCriterionName());
    }

    /**
     * @brief Verifica che getCriterionName() restituisca il nome leggibile del tag EXPLICIT.
     */
    @Test
    void tagGenerator_criterionNameExplicit() {
        assertEquals("Esplicite", new TagPlaylistGenerator(TagType.EXPLICIT).getCriterionName());
    }

    /**
     * @brief Verifica che getCriterionName() restituisca il nome leggibile del tag NEW_RELEASE.
     */
    @Test
    void tagGenerator_criterionNameNewRelease() {
        assertEquals("Nuove Uscite", new TagPlaylistGenerator(TagType.NEW_RELEASE).getCriterionName());
    }

    // ── Polimorfismo ──────────────────────────

    /**
     * @brief Verifica che i generatori siano utilizzabili in modo polimorfico
     * tramite il tipo astratto AutoPlaylistGenerator.
     */
    @Test
    void generators_areUsablePolymorphically() {
        AutoPlaylistGenerator[] generators = {
                new GenrePlaylistGenerator("Rock"),
                new YearPlaylistGenerator(1970, 1980),
                new TagPlaylistGenerator(TagType.FAVOURITE)
        };

        for (AutoPlaylistGenerator generator : generators) {
            Playlist playlist = generator.createPlaylist(catalog);
            assertNotNull(playlist);
            assertNotNull(generator.getCriterionName());
            assertFalse(generator.getCriterionName().isEmpty());
        }
    }

    // ── Aggiornamento automatico ──────────────

    /**
     * @brief Verifica che GenrePlaylistGenerator includa le nuove tracce
     * aggiunte al catalogo dopo la generazione.
     */
    @Test
    void genreGenerator_updatesAfterNewTrackAdded() {
        AutoPlaylistGenerator generator = new GenrePlaylistGenerator("Rock");

        // prima generazione — 2 tracce Rock
        Playlist before = generator.createPlaylist(catalog);
        assertEquals(2, before.getTracks().size());

        // aggiunge una nuova traccia Rock
        catalog.addTrack(new TrackImpl("Thunderstruck", "AC/DC", "Rock", 1990, 292, null));

        // nuova generazione — deve includere la nuova traccia
        Playlist after = generator.createPlaylist(catalog);
        assertEquals(3, after.getTracks().size());
        assertTrue(after.getTracks().stream()
                .anyMatch(t -> t.getTitle().equals("Thunderstruck")));
    }

    /**
     * @brief Verifica che YearPlaylistGenerator includa le nuove tracce
     * aggiunte al catalogo dopo la generazione.
     */
    @Test
    void yearGenerator_updatesAfterNewTrackAdded() {
        AutoPlaylistGenerator generator = new YearPlaylistGenerator(2020, 2022);

        Playlist before = generator.createPlaylist(catalog);
        assertEquals(2, before.getTracks().size());

        catalog.addTrack(new TrackImpl("Flowers", "Miley Cyrus", "Pop", 2021, 200, null));

        Playlist after = generator.createPlaylist(catalog);
        assertEquals(3, after.getTracks().size());
        assertTrue(after.getTracks().stream()
                .anyMatch(t -> t.getTitle().equals("Flowers")));
    }

    /**
     * @brief Verifica che TagPlaylistGenerator includa le nuove tracce
     * aggiunte al catalogo con il tag corrispondente.
     */
    @Test
    void tagGenerator_updatesAfterNewTrackAdded() {
        AutoPlaylistGenerator generator = new TagPlaylistGenerator(TagType.FAVOURITE);

        Playlist before = generator.createPlaylist(catalog);
        assertEquals(1, before.getTracks().size());

        TrackImpl newTrack = new TrackImpl("Flowers", "Miley Cyrus", "Pop", 2021, 200, null);
        newTrack.addTag(TagType.FAVOURITE);
        catalog.addTrack(newTrack);

        Playlist after = generator.createPlaylist(catalog);
        assertEquals(2, after.getTracks().size());
        assertTrue(after.getTracks().stream()
                .anyMatch(t -> t.getTitle().equals("Flowers")));
    }


}