package com.example.gruppo04.test;

import com.example.gruppo04.model.*;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogEventType;
import com.example.gruppo04.observer.CatalogObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test unitari automatizzati focalizzati sulla classe {@link ConcreteMusicCatalog}.
 * <p>
 * Verifica i contratti di business, la consistenza dello stato durante le operazioni a cascata,
 * i vincoli di unicità (casi limite) e il corretto funzionamento dell'infrastruttura Observer.
 * </p>
 */
public class MusicCatalogTest {
    private MusicCatalog catalog;
    private Track track1;
    private Track track2;

    /**
     * Configura l'ambiente di test prima di ogni singola esecuzione,
     * istanziando un nuovo catalogo pulito e tracce di test stub.
     */
    @BeforeEach
    public void setUp() {
        catalog = new ConcreteMusicCatalog();
        track1 = new TrackImpl("Bohemian Rhapsody", "Queen", "Rock", 1975, 354);
        track2 = new TrackImpl("Stairway to Heaven", "Led Zeppelin", "Rock", 1971,  482);
    }

    /**
     * Verifica il corretto inserimento e la successiva rimozione isolata di una traccia dal catalogo.
     */
    @Test
    public void testAddAndRemoveTrack() {
        catalog.addTrack(track1);
        assertEquals(1, catalog.getAllTracks().size());
        assertTrue(catalog.getAllTracks().contains(track1));

        catalog.removeTrack(track1.getId());
        assertEquals(0, catalog.getAllTracks().size());
        assertFalse(catalog.getAllTracks().contains(track1));
    }

    /**
     * Verifica il caso limite in cui si tenti di inserire una traccia con un titolo identico
     * (case-insensitive) a una già presente, accertandosi che venga sollevata l'eccezione corretta.
     */
    @Test
    public void testAddDuplicateTrackTitleThrowsException() {
        catalog.addTrack(track1);

        Track duplicateTrack = new TrackImpl("Bohemian Rhapsody", "Queen", "Rock", 1975, 354);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            catalog.addTrack(duplicateTrack);
        });

        assertTrue(exception.getMessage().contains("titolo esiste già"));
    }

    /**
     * Verifica il vincolo architetturale critico del Task T02: la rimozione a cascata.
     * Controlla che se una traccia viene eliminata dal catalogo generale, essa svanisca
     * automaticamente da tutte le playlist associate senza intaccare le restanti tracce.
     */
    @Test
    public void testCascadeRemovalFromPlaylists() {
        Playlist playlist = new PlaylistImpl("My Favorites");
        catalog.createPlaylist("My Favorites"); // Crea e registra la playlist internamente

        catalog.addTrack(track1);
        catalog.addTrack(track2);

        // Simulazione associazione tracce a playlist
        playlist.getTracks().add(track1);
        playlist.getTracks().add(track2);

        assertEquals(2, playlist.getTracks().size());

        // Esecuzione della rimozione
        catalog.removeTrack(track1.getId());

        // Asserzioni di consistenza dello stato
        assertFalse(catalog.getAllTracks().contains(track1), "La traccia deve sparire dal catalogo globale.");
        assertEquals(1, playlist.getTracks().size(), "La playlist deve ridursi di un elemento.");
        assertFalse(playlist.getTracks().contains(track1), "La traccia 1 deve essere stata rimossa dalla playlist.");
        assertTrue(playlist.getTracks().contains(track2), "La traccia 2 non coinvolta deve rimanere inalterata.");
    }

    /**
     * Verifica che il meccanismo di notifica del pattern Observer si attivi correttamente
     * e con i parametri attesi a seguito di un evento di inserimento traccia.
     */
    @Test
    public void testObserverNotificationOnAdd() {
        List<CatalogEvent> receivedEvents = new ArrayList<>();
        CatalogObserver mockObserver = receivedEvents::add;

        catalog.registerObserver(mockObserver);
        catalog.addTrack(track1);

        assertEquals(1, receivedEvents.size(), "L'observer avrebbe dovuto intercettare esattamente un evento.");
        assertEquals(CatalogEventType.TRACK_ADDED, receivedEvents.get(0).getType());
        assertEquals(track1, receivedEvents.get(0).getTarget());
    }
}
