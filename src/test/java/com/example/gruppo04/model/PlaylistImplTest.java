package com.example.gruppo04.model;

import com.example.gruppo04.interfaces.Track;
import org.junit.jupiter.api.Test;
import com.example.gruppo04.interfaces.Playlist;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Suite di test unitari per {@link PlaylistImpl}.
 *
 * Verifica il contratto della playlist: gestione del nome, aggiunta e
 * rimozione di tracce, immutabilità della lista esposta e assenza di duplicati.
 */
class PlaylistImplTest {

    /** Istanza sotto test, ricreata pulita prima di ogni caso. */
    private Playlist playlist;

    /** Prima traccia di esempio usata nei test. */
    private Track t;

    /** Seconda traccia di esempio usata nei test. */
    private Track t2;

    /**
     * @brief Inizializza una playlist vuota prima di ogni test.
     *
     * Garantisce l'isolamento tra i casi: ognuno parte da uno stato noto.
     */
    @BeforeEach
    void setUp() {
        playlist = new PlaylistImpl("Late Night Vibes");
        t = new TrackImpl("Canzone Titolata", "Giovanni", "Canzoni d'amore", 2017, 216, "canzTit.mp3");
        t2 = new TrackImpl("Canzone Titolata2", "Giovanni", "Canzoni d'amore", 2017, 216, "canzTit2.mp3");
    }

    /**
     * @brief Il costruttore imposta il nome, restituito poi da getName().
     */
    @Test
    void setName(){
        assertEquals("Late Night Vibes", playlist.getName());
    }

    /**
     * @brief Costruire una playlist con nome vuoto solleva IllegalArgumentException.
     */
    @Test
    void nameEmpty(){
        assertThrows(IllegalArgumentException.class, () -> new PlaylistImpl(""));
    }

    /**
     * @brief Aggiungere tracce distinte incrementa la dimensione della lista.
     */
    @Test
    void addTrack() {
        playlist.addTrack(t);
        assertEquals(1, playlist.getTracks().size());
        playlist.addTrack(t2);
        assertEquals(2, playlist.getTracks().size());
    }

    /**
     * @brief Rimuovere tracce presenti riduce la dimensione fino a svuotare la playlist.
     */
    @Test
    void removeTrack() {
        playlist.addTrack(t);
        playlist.addTrack(t2);
        playlist.removeTrack(t2);
        assertEquals(1, playlist.getTracks().size());
        playlist.removeTrack(t);
        assertEquals(0, playlist.getTracks().size());
    }

    /**
     * @brief La lista esposta è vuota all'inizio e immodificabile dall'esterno.
     *
     * Una modifica diretta (clear) deve sollevare UnsupportedOperationException,
     * a garanzia dell'incapsulamento.
     */
    @Test
    void getTracks() {
        assertTrue(playlist.getTracks().isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> playlist.getTracks().clear());
    }

    /**
     * @brief Rimuovere una traccia assente da una playlist vuota restituisce false
     *        e non altera lo stato.
     */
    @Test
    void removeTrackEmptyPlaylist() {
        Track assente = new TrackImpl("Canzone Titolata", "Giovanni", "Canzoni d'amore", 2017, 216, "assente.mp3");
        assertFalse(playlist.removeTrack(assente));
        assertTrue(playlist.getTracks().isEmpty());
    }

    /**
     * @brief Rimuovere una traccia assente non solleva eccezioni e lascia la playlist vuota.
     */
    @Test
    void removeTrackEmptyListException() {
        Track assente = new TrackImpl("Canzone Titolata", "Giovanni", "Canzoni d'amore", 2017, 216, "assente.mp3");
        assertDoesNotThrow(() -> playlist.removeTrack(assente));
        assertTrue(playlist.getTracks().isEmpty());
    }

    /**
     * @brief Aggiungere due volte la stessa traccia non crea duplicati.
     */
    @Test
    void addDuplicateNotAdd() {
        playlist.addTrack(t);
        playlist.addTrack(t);                       // stessa identica traccia
        assertEquals(1, playlist.getTracks().size());
    }

    /**
     * @brief setName aggiorna il nome della playlist, riflesso da getName().
     */
    @Test
    void renamePlaylist(){
        playlist.setName("Rename Titolata");
        assertEquals("Rename Titolata", playlist.getName());
    }


    /**
     * @brief Verifica l'estrazione delle tracce da una playlist popolata.
     * @details Controlla che l'implementazione del contratto PlayableSource
     * restituisca fedelmente tutte e sole le tracce inserite in precedenza.
     */
    @Test
    void getTracks_populatedPlaylist_returnsAllTracks() {
        playlist.addTrack(t);
        playlist.addTrack(t2);

        List<Track> extractedTracks = playlist.getTracks();

        assertNotNull(extractedTracks, "La lista restituita non deve essere nulla");
        assertEquals(2, extractedTracks.size(), "La lista deve contenere esattamente i 2 brani inseriti");

        // Verifica che gli elementi siano esattamente quelli attesi (l'ordine di inserimento è mantenuto dall'ArrayList interna)
        assertEquals(t, extractedTracks.get(0));
        assertEquals(t2, extractedTracks.get(1));
    }

    /**
     * @brief Verifica la corretta generazione dei metadati generali della playlist.
     * @details Controlla che il dizionario generato contenga le chiavi previste
     * ("Nome", "Tipo", "Brani Totali") con i valori corretti e dinamicamente aggiornati.
     * Verifica inoltre l'ordine rigoroso di iterazione della mappa garantito
     * dalla LinkedHashMap, fondamentale per una corretta visualizzazione nella UI.
     */
    @Test
    void getDisplayName_returnsOrderedMetadataMap() {
        // Aggiungiamo un paio di tracce per testare che il contatore "Brani Totali" si aggiorni correttamente
        playlist.addTrack(t);
        playlist.addTrack(t2);

        Map<String, String> meta = playlist.getDisplayName();

        assertNotNull(meta, "La mappa dei metadati non deve essere nulla");
        assertEquals(3, meta.size(), "La mappa deve contenere esattamente 3 elementi");

        // Verifica la correttezza dei valori (incluso il conteggio dinamico)
        assertEquals("Late Night Vibes", meta.get("Nome"));
        assertEquals("Playlist", meta.get("Tipo"));
        assertEquals("2", meta.get("Brani Totali")); // Deve essere "2" perché abbiamo aggiunto t e t2

        // Verifica l'ordine di inserimento (Cruciale per la LinkedHashMap e per chi scriverà la UI)
        java.util.Iterator<String> keyIterator = meta.keySet().iterator();
        assertEquals("Nome", keyIterator.next(), "La prima chiave deve essere 'Nome'");
        assertEquals("Tipo", keyIterator.next(), "La seconda chiave deve essere 'Tipo'");
        assertEquals("Brani Totali", keyIterator.next(), "La terza chiave deve essere 'Brani Totali'");
    }

    @Test
    void playCount_incrementsFromZero() {
        assertEquals(0, playlist.getPlayCount());

        playlist.incrementPlayCount();
        playlist.incrementPlayCount();

        assertEquals(2, playlist.getPlayCount());
    }
}
