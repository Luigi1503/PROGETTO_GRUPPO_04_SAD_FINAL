package com.example.gruppo04.persistence;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.TrackImpl;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Suite di test unitari automatizzati focalizzati sulla classe {@link PersistenceManager}.
 * Verifica la conformità al pattern Singleton, la persistenza dello stato (salvataggio e caricamento)
 * sia in scenari di catalogo vuoto che popolato, e la corretta gestione dei parametri di input.
 */
public class PersistenceManagerTest {

    /** @brief Il catalogo musicale utilizzato come sorgente dati. */
    private MusicCatalog catalog;

    /** @brief Riferimento al file temporaneo su cui effettuare i test di scrittura/lettura. */
    private File tempFile;

    /** @brief Istanza del gestore della persistenza da testare. */
    private PersistenceManager persistenceManager;

    /**
     * @brief Configura l'ambiente prima di ogni singolo test.
     * * Pulisce lo stato del catalogo, acquisisce le istanze Singleton e crea un file temporaneo isolato.
     * @throws IOException Se si verifica un errore durante la creazione del file temporaneo.
     */
    @BeforeEach
    public void setUp() throws IOException {
        ConcreteMusicCatalog.getInstance().reset();
        catalog = ConcreteMusicCatalog.getInstance();
        persistenceManager = PersistenceManager.getInstance();
        tempFile = File.createTempFile("catalog_test", ".ser");
    }

    /**
     * @brief Ripristina lo stato ed elimina il file temporaneo dopo ogni test.
     */
    @AfterEach
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    // ── Singleton ───────────────────────────

    /**
     * @brief Verifica che {@link PersistenceManager} rispetti il pattern Singleton restituendo sempre la stessa istanza.
     */
    @Test
    public void testSingletonInstance() {
        PersistenceManager instance1 = PersistenceManager.getInstance();
        PersistenceManager instance2 = PersistenceManager.getInstance();
        assertSame(instance1, instance2, "PersistenceManager deve essere un Singleton");
    }

    // ── Salvataggio e caricamento ──────────

    /**
     * @brief Verifica il salvataggio e il caricamento di un catalogo privo di elementi.
     * @throws Exception Se si verificano errori inattesi durante la persistenza.
     */
    @Test
    public void testSaveAndLoadEmptyCatalog() throws Exception {
        persistenceManager.save(catalog, tempFile.getAbsolutePath());
        MusicCatalog loadedCatalog = persistenceManager.load(tempFile.getAbsolutePath());

        assertNotNull(loadedCatalog);
        assertTrue(loadedCatalog.getAllTracks().isEmpty());
        assertTrue(loadedCatalog.getPlaylists().isEmpty());
    }

    /**
     * @brief Verifica il ciclo completo di salvataggio e caricamento con un catalogo popolato.
     * Controlla il ripristino di tracce, metadati, playlist e associazioni.
     * @throws Exception Se si verificano errori durante la persistenza o la validazione.
     */
    @Test
    public void testSaveAndLoadWithData() throws Exception {
        Track track1 = new TrackImpl("Bohemian Rhapsody", "Queen", "Rock", 1975, 354, "bohemian.mp3");
        Track track2 = new TrackImpl("It's My Life", "Bon Jovi", "Rock", 2000, 224, "its_my_life.mp3");
        catalog.addTrack(track1);
        catalog.addTrack(track2);

        catalog.createPlaylist("Favorites");
        Playlist playlist = catalog.getPlaylists().get(0);
        catalog.addTrackToPlaylist(playlist, track2);

        persistenceManager.save(catalog, tempFile.getAbsolutePath());

        // Resetta lo stato in memoria per verificare il caricamento reale
        ConcreteMusicCatalog.getInstance().reset();
        assertTrue(catalog.getAllTracks().isEmpty());
        assertTrue(catalog.getPlaylists().isEmpty());

        // Carica lo stato
        MusicCatalog loadedCatalog = persistenceManager.load(tempFile.getAbsolutePath());
        assertNotNull(loadedCatalog);

        Collection<Track> loadedTracks = loadedCatalog.getAllTracks();
        assertEquals(2, loadedTracks.size());

        Track loadedTrack2 = loadedTracks.stream()
                .filter(t -> t.getTitle().equals("It's My Life"))
                .findFirst()
                .orElse(null);

        assertNotNull(loadedTrack2);
        assertEquals("Bon Jovi", loadedTrack2.getAuthor());
        assertEquals("Rock", loadedTrack2.getGenre());
        assertEquals(2000, loadedTrack2.getYear());
        assertEquals(224, loadedTrack2.getDuration());
        assertEquals("its_my_life.mp3", loadedTrack2.getFilePath());

        // Verifica playlist caricate
        List<Playlist> loadedPlaylists = loadedCatalog.getPlaylists();
        assertEquals(1, loadedPlaylists.size());
        Playlist loadedPlaylist = loadedPlaylists.get(0);
        assertEquals("Favorites", loadedPlaylist.getName());
        assertEquals(1, loadedPlaylist.getTracks().size());
        assertEquals(loadedTrack2, loadedPlaylist.getTracks().get(0));
    }

    /**
     * @brief Verifica che il metodo load ripristini effettivamente lo stato precedentemente salvato.
     * @throws Exception Se si verificano errori durante il test.
     */
    @Test
    public void testLoadRestoresPreviousState() throws Exception {
        Track track = new TrackImpl("Thunderstruck", "AC/DC", "Rock", 1990, 292, "thunderstruck.mp3");
        catalog.addTrack(track);
        persistenceManager.save(catalog, tempFile.getAbsolutePath());

        ConcreteMusicCatalog.getInstance().reset();
        persistenceManager.load(tempFile.getAbsolutePath());

        assertEquals(1, catalog.getAllTracks().size());
        Track loadedTrack = catalog.getAllTracks().iterator().next();
        assertNotNull(loadedTrack);
        assertEquals("Thunderstruck", loadedTrack.getTitle());
    }

    /**
     * @brief Verifica che il caricamento restituisca sempre il singleton del catalogo.
     * @throws Exception Se si verificano errori durante il test.
     */
    @Test
    public void testLoadReturnsSingletonCatalogInstance() throws Exception {
        persistenceManager.save(catalog, tempFile.getAbsolutePath());
        MusicCatalog loadedCatalog = persistenceManager.load(tempFile.getAbsolutePath());
        assertSame(ConcreteMusicCatalog.getInstance(), loadedCatalog);
    }

    /**
     * @brief Verifica il corretto funzionamento dei metodi save() e load() che utilizzano il percorso di default configurato nel PersistenceManager.
     * @throws Exception Se si verifica un errore durante il salvataggio o il caricamento.
     */
    @Test
    public void testSaveAndLoadUsingDefaultPath() throws Exception {
        persistenceManager.setDefaultFilePath(tempFile.getAbsolutePath());

        Track track = new TrackImpl("Numb", "Linkin Park", "Rock", 2003, 185, "numb.mp3");
        catalog.addTrack(track);

        persistenceManager.save(catalog);

        ConcreteMusicCatalog.getInstance().reset();
        MusicCatalog loadedCatalog = persistenceManager.load();

        assertEquals(1, loadedCatalog.getAllTracks().size());
    }

    // ── Errori / Validazioni ───────────────

    /**
     * @brief Verifica che il tentativo di salvataggio di un catalogo nullo sollevi un'eccezione.
     */
    @Test
    public void testSaveWithNullCatalogThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> persistenceManager.save(null, tempFile.getAbsolutePath()));
    }

    /**
     * @brief Verifica che percorsi file nulli o vuoti sollevino eccezioni in fase di salvataggio.
     */
    @Test
    public void testSaveWithInvalidFileThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> persistenceManager.save(catalog, null));
        assertThrows(IllegalArgumentException.class,
                () -> persistenceManager.save(catalog, "   "));
    }

    /**
     * @brief Verifica che percorsi file nulli o vuoti sollevino eccezioni in fase di caricamento.
     */
    @Test
    public void testLoadWithInvalidFileThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> persistenceManager.load(null));
        assertThrows(IllegalArgumentException.class,
                () -> persistenceManager.load("   "));
    }

    /**
     * @brief Verifica che il caricamento da un file inesistente sollevi una IOException.
     */
    @Test
    public void testLoadNonExistingFileThrowsException() {
        assertThrows(IOException.class, () -> persistenceManager.load("file_che_non_esiste.ser"));
    }

    // ── Default File Path ───────────────────

    /**
     * @brief Verifica che il percorso di default possa essere impostato e successivamente recuperato correttamente.
     */
    @Test
    public void testSetAndGetDefaultFilePath() {
        String path = "abc.ser";
        persistenceManager.setDefaultFilePath(path);
        assertEquals(path, persistenceManager.getDefaultFilePath());
    }
}