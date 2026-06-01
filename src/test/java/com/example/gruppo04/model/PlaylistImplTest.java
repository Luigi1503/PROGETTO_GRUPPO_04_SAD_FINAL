package com.example.gruppo04.model;

import com.example.gruppo04.interfaces.Track;
import org.junit.jupiter.api.Test;
import com.example.gruppo04.interfaces.Playlist;
import org.junit.jupiter.api.BeforeEach;
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

    /**
     * @brief Inizializza una playlist vuota prima di ogni test.
     *
     * Garantisce l'isolamento tra i casi: ognuno parte da uno stato noto.
     */
    @BeforeEach
    void setUp() {
        playlist = new PlaylistImpl("Late Night Vibes");
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
        Track t = new TrackImpl("Canzone Titolata", "Giovanni", "Canzoni d'amore", 2017, 216);
        playlist.addTrack(t);
        assertEquals(playlist.getTracks().size(), 1);
        Track t2 = new TrackImpl("Canzone Titolata2", "Giovanni", "Canzoni d'amore", 2017, 216);
        playlist.addTrack(t2);
        assertEquals(playlist.getTracks().size(), 2);
    }

    /**
     * @brief Rimuovere tracce presenti riduce la dimensione fino a svuotare la playlist.
     */
    @Test
    void removeTrack() {
        Track t = new TrackImpl("Canzone Titolata", "Giovanni", "Canzoni d'amore", 2017, 216);
        playlist.addTrack(t);
        Track t2 = new TrackImpl("Canzone Titolata2", "Giovanni", "Canzoni d'amore", 2017, 216);
        playlist.addTrack(t2);
        playlist.removeTrack(t2);
        assertEquals(playlist.getTracks().size(), 1);
        playlist.removeTrack(t);
        assertEquals(playlist.getTracks().size(), 0);
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
        Track assente = new TrackImpl("Canzone Titolata", "Giovanni", "Canzoni d'amore", 2017, 216);
        assertFalse(playlist.removeTrack(assente));
        assertTrue(playlist.getTracks().isEmpty());
    }

    /**
     * @brief Rimuovere una traccia assente non solleva eccezioni e lascia la playlist vuota.
     */
    @Test
    void removeTrackEmptyListException() {
        Track assente = new TrackImpl("Canzone Titolata", "Giovanni", "Canzoni d'amore", 2017, 216);
        assertDoesNotThrow(() -> playlist.removeTrack(assente));
        assertTrue(playlist.getTracks().isEmpty());
    }

    /**
     * @brief Aggiungere due volte la stessa traccia non crea duplicati.
     */
    @Test
    void addDuplicateNotAdd() {
        Track t = new TrackImpl("Canzone Titolata", "Giovanni", "Canzoni d'amore", 2017, 216);
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
}