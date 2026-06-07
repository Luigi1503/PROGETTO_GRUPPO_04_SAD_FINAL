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

    /**
     * @brief Configura l'ambiente prima di ogni singolo test.
     * @details Pulisce lo stato del catalogo, acquisisce le istanze Singleton
     * e imposta il percorso del file di test.
     */
    @BeforeEach
    void setUp() {
        ConcreteMusicCatalog.getInstance().reset();
        catalog = ConcreteMusicCatalog.getInstance();
        pm = PersistenceManager.getInstance();
        pm.setDefaultFilePath(TEST_FILE);
        trackController = new TrackController(catalog);
        playlistController = new PlaylistController(catalog);
    }

    /**
     * @brief Ripristina lo stato ed elimina il file temporaneo dopo ogni test.
     */
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
        trackController.addTrack("Bohemian Rhapsody", "Queen", "Rock", 1975, 354, "queen.mp3");
        Track original = catalog.getAllTracks().iterator().next();

        pm.save(catalog);
        ConcreteMusicCatalog.getInstance().reset();
        pm.load();

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

    /**
     * @brief Verifica che {@link PersistenceManager} rispetti il pattern Singleton restituendo sempre la stessa istanza.
     */
    @Test
    public void testSingletonInstance() {
        PersistenceManager instance1 = PersistenceManager.getInstance();
        PersistenceManager instance2 = PersistenceManager.getInstance();
        assertSame(instance1, instance2, "PersistenceManager deve essere un Singleton");
    }

    // ── METODI AGGIUNTI DAL DOCUMENTO 3 ───────────────────────────

    /**
     * @brief Verifica il salvataggio e il caricamento di un catalogo privo di elementi con parametri espliciti.
     * @throws Exception Se si verificano errori inattesi durante la persistenza.
     */
    @Test
    void testSaveAndLoadEmptyCatalogWithExplicitPath() throws Exception {
        File tempFile = File.createTempFile("catalog_test_empty", ".ser");
        try {
            pm.save(catalog, tempFile.getAbsolutePath());
            MusicCatalog loadedCatalog = pm.load(tempFile.getAbsolutePath());

            assertNotNull(loadedCatalog);
            assertTrue(loadedCatalog.getAllTracks().isEmpty());
            assertTrue(loadedCatalog.getPlaylists().isEmpty());
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * @brief Verifica che il caricamento restituisca sempre il singleton del catalogo.
     * @throws Exception Se si verificano errori durante il test.
     */
    @Test
    void testLoadReturnsSingletonCatalogInstance() throws Exception {
        trackController.addTrack("Test Track", "Test Artist", "Rock", 2020, 180, "test.mp3");
        pm.save(catalog);
        ConcreteMusicCatalog.getInstance().reset();

        MusicCatalog loadedCatalog = pm.load();
        assertSame(ConcreteMusicCatalog.getInstance(), loadedCatalog,
                "load() deve restituire l'istanza singleton di ConcreteMusicCatalog");
    }

    /**
     * @brief Verifica che percorsi file con soli spazi sollevino eccezioni in fase di salvataggio.
     */
    @Test
    void testSaveWithSpacesOnlyPathThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> pm.save(catalog, "   "),
                "save() deve lanciare IllegalArgumentException per path con soli spazi");
    }

    /**
     * @brief Verifica che il percorso di default possa essere impostato e successivamente recuperato correttamente.
     */
    @Test
    void testSetAndGetDefaultFilePath() {
        String path = "abc.ser";
        pm.setDefaultFilePath(path);
        assertEquals(path, pm.getDefaultFilePath(),
                "getDefaultFilePath() deve restituire il valore impostato");
    }
}