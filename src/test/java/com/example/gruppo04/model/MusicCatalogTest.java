package com.example.gruppo04.model;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
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
        track1 = new TrackImpl("Bohemian Rhapsody", "Queen", "Rock", 1975, 354, "bohemian.mp3");
        track2 = new TrackImpl("Stairway to Heaven", "Led Zeppelin", "Rock", 1971,  482, "stairway.mp3");
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

        Track duplicateTrack = new TrackImpl("Bohemian Rhapsody", "Queen", "Rock", 1975, 354, "bohemian.mp3");

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
        // Crea e registra la playlist internamente
        catalog.createPlaylist("My Favorites");

        // Recupera la playlist dal catalogo per lavorare sulla stessa istanza
        Playlist playlist = catalog.getPlaylists().stream()
                .filter(p -> p.getName().equals("My Favorites"))
                .findFirst()
                .orElseThrow();

        // Aggiunge le tracce al catalogo e poi alla playlist tramite API pubbliche
        catalog.addTrack(track1);
        catalog.addTrack(track2);

        catalog.addTrackToPlaylist(playlist, track1);
        catalog.addTrackToPlaylist(playlist, track2);

        assertEquals(2, playlist.getTracks().size());

        // Esecuzione della rimozione dal catalogo (effetto a cascata atteso)
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

    /**
     * Verifica la creazione e la rimozione di una playlist nel catalogo.
     * Controlla il valore di ritorno di `createPlaylist` e `deletePlaylist`
     * e la consistenza della lista restituita da `getPlaylists()`.
     */
    @Test
    public void testCreateAndDeletePlaylist() {
        assertTrue(catalog.getPlaylists().isEmpty(), "Catalogo inizialmente vuoto di playlist");

        boolean created = catalog.createPlaylist("Chill Vibes");
        assertTrue(created, "createPlaylist dovrebbe restituire true alla prima creazione");
        assertEquals(1, catalog.getPlaylists().size());

        Playlist p = catalog.getPlaylists().get(0);
        assertEquals("Chill Vibes", p.getName());

        boolean deleted = catalog.deletePlaylist(p);
        assertTrue(deleted, "deletePlaylist dovrebbe restituire true se la playlist era presente");
        assertTrue(catalog.getPlaylists().isEmpty(), "La lista delle playlist deve tornare vuota dopo la cancellazione");
    }

    /**
     * Verifica il caso limite di creazione di due playlist con lo stesso nome.
     * Il secondo tentativo dovrebbe restituire {@code false} senza lanciare eccezioni.
     */
    @Test
    public void testCreatePlaylistDuplicateName() {
        boolean first = catalog.createPlaylist("Road Trip");
        assertTrue(first);

        boolean second = catalog.createPlaylist("Road Trip");
        assertFalse(second, "createPlaylist deve restituire false se il nome esiste già");

        assertEquals(1, catalog.getPlaylists().size(), "Deve rimanere una sola playlist con quel nome");
    }

    /**
     * Verifica la funzionalità di rinomina di una playlist e i vincoli associati.
     * Controlla che la rinomina verso un nome libero abbia successo e che la
     * rinomina verso un nome già esistente fallisca restituendo {@code false}.
     */
    @Test
    public void testRenamePlaylist() {
        catalog.createPlaylist("Morning");
        catalog.createPlaylist("Evening");

        Playlist morning = catalog.getPlaylists().stream()
                .filter(pl -> pl.getName().equals("Morning"))
                .findFirst()
                .orElseThrow();

        // Rinomina verso un nome nuovo
        boolean renamed = catalog.renamePlaylist(morning, "Wake Up");
        assertTrue(renamed, "renamePlaylist dovrebbe restituire true per un nuovo nome disponibile");
        assertTrue(catalog.getPlaylists().stream().anyMatch(pl -> pl.getName().equals("Wake Up")));

        // Rinomina verso un nome già presente (Evening) deve fallire
        boolean renamedToExisting = catalog.renamePlaylist(morning, "Evening");
        assertFalse(renamedToExisting, "renamePlaylist deve restituire false se il nuovo nome è già usato");
    }

    /**
     * Verifica le condizioni di validazione per la creazione di playlist.
     * Il name nullo o vuoto deve sollevare IllegalArgumentException.
     */
    @Test
    public void testCreatePlaylistInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> catalog.createPlaylist(null));
        assertThrows(IllegalArgumentException.class, () -> catalog.createPlaylist(""));
        assertThrows(IllegalArgumentException.class, () -> catalog.createPlaylist("   "));
    }

    /**
     * Verifica che la rinomina fallisca se la playlist non appartiene al catalogo
     * e che newName nullo o vuoto generi IllegalArgumentException.
     */
    @Test
    public void testRenamePlaylistInvalidInput() {
        Playlist external = new PlaylistImpl("External");
        boolean renamed = catalog.renamePlaylist(external, "New Name");
        assertFalse(renamed, "renamePlaylist deve restituire false per playlist non presente nel catalogo");

        catalog.createPlaylist("Local");
        Playlist local = catalog.getPlaylists().get(0);
        assertThrows(IllegalArgumentException.class, () -> catalog.renamePlaylist(local, null));
        assertThrows(IllegalArgumentException.class, () -> catalog.renamePlaylist(local, ""));
        assertThrows(IllegalArgumentException.class, () -> catalog.renamePlaylist(local, "   "));
    }

    /**
     * Verifica che la cancellazione di una playlist non presente restituisca false.
     */
    @Test
    public void testDeletePlaylistNotPresent() {
        Playlist external = new PlaylistImpl("External");
        boolean deleted = catalog.deletePlaylist(external);
        assertFalse(deleted, "deletePlaylist deve restituire false se la playlist non appartiene al catalogo");
    }

    /**
     * Verifica le condizioni di validazione per l'aggiunta delle tracce.
     * Track null deve sollevare IllegalArgumentException.
     */
    @Test
    public void testAddTrackInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> catalog.addTrack(null));
    }

    /**
     * Verifica che l'aggiunta di una traccia ad una playlist rispetti le precondizioni
     * e che l'inserimento duplicato nella playlist restituisca false.
     */
    @Test
    public void testAddTrackToPlaylistValidationAndDuplicates() {
        catalog.createPlaylist("Mix");
        Playlist mix = catalog.getPlaylists().get(0);

        assertThrows(IllegalArgumentException.class, () -> catalog.addTrackToPlaylist(mix, track1));

        catalog.addTrack(track1);
        Playlist external = new PlaylistImpl("External");
        assertThrows(IllegalArgumentException.class, () -> catalog.addTrackToPlaylist(external, track1));

        boolean addedFirst = catalog.addTrackToPlaylist(mix, track1);
        assertTrue(addedFirst, "Prima aggiunta alla playlist deve avere successo");

        boolean addedSecond = catalog.addTrackToPlaylist(mix, track1);
        assertFalse(addedSecond, "Seconda aggiunta della stessa traccia deve fallire");
    }

    /**
     * Verifica che la rimozione da playlist ritorni false se la traccia non è presente.
     */
    @Test
    public void testRemoveTrackFromPlaylistNotPresent() {
        catalog.createPlaylist("Mix");
        Playlist mix = catalog.getPlaylists().get(0);

        catalog.addTrack(track1);
        boolean removed = catalog.removeTrackFromPlaylist(mix, track1);
        assertFalse(removed, "removeTrackFromPlaylist deve restituire false se la traccia non è presente");
    }

    /**
     * Verifica la notifica degli observer per eventi di playlist (creazione, rinomina e cancellazione).
     */
    @Test
    public void testObserverNotificationsOnPlaylistLifecycle() {
        List<CatalogEvent> receivedEvents = new ArrayList<>();
        catalog.registerObserver(receivedEvents::add);

        catalog.createPlaylist("Daily");
        Playlist daily = catalog.getPlaylists().get(0);

        boolean renamed = catalog.renamePlaylist(daily, "Daily Updated");
        assertTrue(renamed);

        boolean deleted = catalog.deletePlaylist(daily);
        assertTrue(deleted);

        assertEquals(3, receivedEvents.size(), "Devono essere stati emessi tre eventi di playlist");
        assertEquals(CatalogEventType.PLAYLIST_ADDED, receivedEvents.get(0).getType());
        assertEquals(CatalogEventType.PLAYLIST_RENAMED, receivedEvents.get(1).getType());
        assertEquals(CatalogEventType.PLAYLIST_REMOVED, receivedEvents.get(2).getType());
    }

    /**
     * Verifica la notifica degli observer per eventi legati alle tracce.
     */
    @Test
    public void testObserverNotificationsOnTrackLifecycle() {
        List<CatalogEvent> receivedEvents = new ArrayList<>();
        catalog.registerObserver(receivedEvents::add);

        catalog.addTrack(track1);

        track1.setTitle("Bohemian Rhapsody (Remastered)");
        catalog.updateTrack(track1);

        catalog.removeTrack(track1.getId());

        assertEquals(3, receivedEvents.size(), "Devono essere stati emessi tre eventi di traccia");
        assertEquals(CatalogEventType.TRACK_ADDED, receivedEvents.get(0).getType());
        assertEquals(CatalogEventType.TRACK_UPDATED, receivedEvents.get(1).getType());
        assertEquals(CatalogEventType.TRACK_REMOVED, receivedEvents.get(2).getType());
        assertEquals(track1, receivedEvents.get(1).getTarget());
    }

    /**
     * Verifica la notifica degli observer quando si aggiungono o rimuovono tracce in playlist.
     */
    @Test
    public void testObserverNotificationsOnPlaylistTrackChanges() {
        List<CatalogEvent> receivedEvents = new ArrayList<>();
        catalog.registerObserver(receivedEvents::add);

        catalog.createPlaylist("Mix");
        Playlist mix = catalog.getPlaylists().get(0);

        catalog.addTrack(track1);
        catalog.addTrackToPlaylist(mix, track1);
        catalog.removeTrackFromPlaylist(mix, track1);

        List<CatalogEvent> playlistTrackEvents = receivedEvents.stream()
            .filter(event -> event.getType() == CatalogEventType.PLAYLIST_TRACK_ADDED
                || event.getType() == CatalogEventType.PLAYLIST_TRACK_REMOVED)
            .toList();

        assertEquals(2, playlistTrackEvents.size(), "Devono essere stati emessi due eventi di playlist track");
        assertEquals(CatalogEventType.PLAYLIST_TRACK_ADDED, playlistTrackEvents.get(0).getType());
        assertEquals(CatalogEventType.PLAYLIST_TRACK_REMOVED, playlistTrackEvents.get(1).getType());
    }

    /**
     * Verifica le condizioni di errore di updateTrack per input null o traccia assente.
     */
    @Test
    public void testUpdateTrackInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> catalog.updateTrack(null));

        Track missing = new TrackImpl("Missing", "Unknown", "Rock", 1999, 200, "missing.mp3");
        assertThrows(IllegalArgumentException.class, () -> catalog.updateTrack(missing));
    }

    /**
     * Verifica che il sistema sollevi un'eccezione se si tenta di inserire una traccia
     * con la stessa combinazione di Titolo e Autore (case-insensitive) di una già esistente.
     */
    @Test
    public void testAddDuplicateTitleAndAuthorThrowsException() {
        catalog.addTrack(track1); // "Bohemian Rhapsody" di "Queen"

        // Filepath diverso, ma stessa coppia Titolo-Autore
        Track duplicateComboTrack = new TrackImpl("bohemian rhapsody", "QUEEN", "Rock", 1975, 354, "bohemian_remix.mp3");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            catalog.addTrack(duplicateComboTrack);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("esiste già"));
    }

    /**
     * Verifica di controparte: due autori diversi possono avere canzoni con lo stesso titolo.
     */
    @Test
    public void testAddSameTitleDifferentAuthorIsAllowed() {
        catalog.addTrack(track1); // "Bohemian Rhapsody" di "Queen"

        Track sameTitleDifferentAuthor = new TrackImpl("Bohemian Rhapsody", "The Muppets", "Comedy", 2009, 295, "muppets_bohemian.mp3");

        assertDoesNotThrow(() -> catalog.addTrack(sameTitleDifferentAuthor));
        assertEquals(2, catalog.getAllTracks().size());
    }

    /**
     * Verifica che il sistema sollevi un'eccezione se si tenta di inserire una traccia
     * con un filePath identico a una già presente nel catalogo, in modo da evitare
     * che lo stesso file MP3 venga associato a più brani.
     */
    @Test
    public void testAddTrackWithDuplicateFilePathThrowsException() {
        // Inseriamo la prima traccia (track1 ha "bohemian.mp3" come filepath)
        catalog.addTrack(track1);

        // Creiamo una seconda traccia con titolo e autore diversi, ma stesso identico filepath
        Track duplicateFileTrack = new TrackImpl("Bohemian Rhapsody (Cover)", "Fake Queen", "Rock", 2026, 354, "bohemian.mp3");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            catalog.addTrack(duplicateFileTrack);
        });

        assertTrue(exception.getMessage().contains("Questo file MP3 è già stato associato al brano"));
    }


    /**
     * @brief Verifica i controlli di integrità del catalogo in fase di aggiornamento.
     * @details Assicura che la modifica di una traccia in modo che collida con i
     * metadati di un'altra venga respinta.
     */
    @Test
    public void testUpdateTrackDuplicateTitleAndAuthorThrowsException() {
        catalog.addTrack(track1);
        catalog.addTrack(track2);

        track2.setTitle("Bohemian Rhapsody");
        track2.setAuthor("Queen");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            catalog.updateTrack(track2);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("già presente"));
    }

    /**
     * @brief Verifica il vincolo di univocità del file in fase di aggiornamento.
     * @details Assicura che non sia possibile riassegnare un MP3 già in uso ad un'altra traccia.
     */
    @Test
    public void testUpdateTrackDuplicateFilePathThrowsException() {
        catalog.addTrack(track1);
        catalog.addTrack(track2);

        track2.setFilePath("bohemian.mp3");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            catalog.updateTrack(track2);
        });

        assertTrue(exception.getMessage().contains("Questo file MP3 è già stato associato"));
    }

    /**
     * @brief Verifica il corretto aggiornamento di un campo legittimo.
     * @details Assicura che una modifica non conflittuale (es. l'anno) passi i controlli
     * senza identificare la traccia come un duplicato di sé stessa.
     */
    @Test
    public void testUpdateTrackLegitimateUpdateDoesNotThrow() {
        catalog.addTrack(track1);
        track1.setYear(2024);

        assertDoesNotThrow(() -> catalog.updateTrack(track1));

        Track updated = catalog.getAllTracks().iterator().next();
        assertEquals(2024, updated.getYear());
    }
}
