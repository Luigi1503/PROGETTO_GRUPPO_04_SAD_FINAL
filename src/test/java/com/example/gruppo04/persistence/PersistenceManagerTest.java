package com.example.gruppo04.persistence;

import com.example.gruppo04.controller.PlaylistController;
import com.example.gruppo04.controller.TrackController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test per la persistenza del catalogo musicale.
 * Verifica il salvataggio ed il caricamento con verifica
 * dell'integrità di catalogo e playlist.
 */
class PersistenceManagerTest {

    private MusicCatalog catalog;
    private PersistenceManager pm;
    private TrackController trackController;
    private PlaylistController playlistController;
    private static final String TEST_FILE = "test_catalog.ser";

    @BeforeEach
    void setUp() {
        ConcreteMusicCatalog.getInstance().reset();
        catalog = ConcreteMusicCatalog.getInstance();
        pm = PersistenceManager.getInstance();
        pm.setDefaultFilePath(TEST_FILE);
        trackController = new TrackController(catalog);
        playlistController = new PlaylistController(catalog);
    }

    @AfterEach
    void tearDown() {
        // elimina il file di test dopo ogni test
        new File(TEST_FILE).delete();
        ConcreteMusicCatalog.getInstance().reset();
    }

    // ── save / load ───────────────────────────

    /**
     * Verifica che il catalogo venga salvato e ricaricato correttamente
     * con le stesse tracce — round-trip completo.
     */
    @Test
    void saveAndLoad_tracksRoundTrip() throws IOException, ClassNotFoundException {
        trackController.addTrack("Bohemian Rhapsody", "Queen", "Rock", 1975, 354, "queen.mp3");
        trackController.addTrack("Hotel California", "Eagles", "Rock", 1977, 391, "hotel.mp3");

        pm.save(catalog);
        ConcreteMusicCatalog.getInstance().reset();
        pm.load();

        assertEquals(2, catalog.getAllTracks().size());
        assertTrue(catalog.getAllTracks().stream()
                .anyMatch(t -> t.getTitle().equals("Bohemian Rhapsody")));
        assertTrue(catalog.getAllTracks().stream()
                .anyMatch(t -> t.getTitle().equals("Hotel California")));
    }

    /**
     * Verifica che le playlist vengano salvate e ricaricate correttamente
     * con le stesse tracce al loro interno — round-trip completo.
     */
    @Test
    void saveAndLoad_playlists() throws IOException, ClassNotFoundException {
        trackController.addTrack("Bohemian Rhapsody", "Queen", "Rock", 1975, 354, "queen.mp3");
        Track track = catalog.getAllTracks().iterator().next();
        playlistController.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        playlistController.addTrackToPlaylist(playlist, track);

        pm.save(catalog);
        ConcreteMusicCatalog.getInstance().reset();
        pm.load();

        assertEquals(1, catalog.getPlaylists().size());
        assertEquals("Rock Classics", catalog.getPlaylists().get(0).getName());
        assertEquals(1, catalog.getPlaylists().get(0).getTracks().size());
        assertEquals("Bohemian Rhapsody",
                catalog.getPlaylists().get(0).getTracks().get(0).getTitle());
    }

    /**
     * Verifica che il salvataggio di un catalogo vuoto non generi eccezioni
     * e che il caricamento restituisca un catalogo vuoto.
     */
    @Test
    void saveAndLoad_emptyCatalog() throws IOException, ClassNotFoundException {
        pm.save(catalog);
        ConcreteMusicCatalog.getInstance().reset();
        pm.load();

        assertTrue(catalog.getAllTracks().isEmpty());
        assertTrue(catalog.getPlaylists().isEmpty());
    }

    /**
     * Verifica che il caricamento da un file inesistente
     * sollevi IOException.
     */
    @Test
    void load_fileNotFound_throwsIOException() {
        pm.setDefaultFilePath("non_esistente.ser");
        assertThrows(IOException.class, () -> pm.load());
    }

    /**
     * Verifica che save() con catalog null sollevi IllegalArgumentException.
     */
    @Test
    void save_nullCatalog_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> pm.save(null));
    }

    /**
     * Verifica che save() con filePath null sollevi IllegalArgumentException.
     */
    @Test
    void save_nullFilePath_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> pm.save(catalog, null));
    }

    /**
     * Verifica che save() con filePath vuoto sollevi IllegalArgumentException.
     */
    @Test
    void save_emptyFilePath_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> pm.save(catalog, ""));
    }

    /**
     * Verifica che load() con filePath null sollevi IllegalArgumentException.
     */
    @Test
    void load_nullFilePath_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> pm.load(null));
    }

    /**
     * Verifica che dopo il salvataggio e il caricamento i campi
     * delle tracce siano identici a quelli originali.
     */
    @Test
    void saveAndLoad_trackFieldsIntegrity() throws IOException, ClassNotFoundException {
        // arrange
        trackController.addTrack("Bohemian Rhapsody", "Queen", "Rock", 1975, 354, "queen.mp3");
        Track original = catalog.getAllTracks().iterator().next();

        // act
        pm.save(catalog);
        ConcreteMusicCatalog.getInstance().reset();
        pm.load();

        // assert
        Track loaded = catalog.getAllTracks().iterator().next();
        assertEquals(original.getTitle(),    loaded.getTitle());
        assertEquals(original.getAuthor(),   loaded.getAuthor());
        assertEquals(original.getGenre(),    loaded.getGenre());
        assertEquals(original.getYear(),     loaded.getYear());
        assertEquals(original.getDuration(), loaded.getDuration());
        assertEquals(original.getId(),       loaded.getId());
    }

    /**
     * Verifica che AutoSaveObserver salvi automaticamente il catalogo
     * ad ogni modifica senza errori.
     */
    @Test
    void autoSaveObserver_savesOnChange() throws IOException, ClassNotFoundException {
        AutoSaveObserver autoSave = new AutoSaveObserver(catalog);
        catalog.registerObserver(autoSave);

        trackController.addTrack("Bohemian Rhapsody", "Queen", "Rock", 1975, 354, "queen.mp3");

        assertTrue(new File(TEST_FILE).exists());

        ConcreteMusicCatalog.getInstance().reset();
        pm.load();
        assertEquals(1, catalog.getAllTracks().size());
    }
}