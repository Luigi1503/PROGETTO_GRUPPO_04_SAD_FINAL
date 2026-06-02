package com.example.gruppo04.controller;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.TrackImpl;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test per {@link PlaylistController}.
 * Verifica che i metodi del controller deleghino correttamente le operazioni
 * a {@link ConcreteMusicCatalog} e restituiscano i valori attesi.
 */
class PlaylistControllerTest {

    /** Controller sotto test. */
    private PlaylistController controller;

    /** Catalogo musicale reale. */
    private MusicCatalog catalog;

    /** Prima traccia di esempio usata nei test. */
    private Track track1;

    /** Seconda traccia di esempio usata nei test. */
    private Track track2;

    /**
     * Inizializza il catalogo, il controller e le tracce di esempio
     * prima di ogni metodo di test, garantendo uno stato pulito e indipendente.
     */
    @BeforeEach
    void setUp() {
        catalog = new ConcreteMusicCatalog();
        controller = new PlaylistController(catalog);

        track1 = new TrackImpl("Hold back the river", "James Bay", "Rock", 2014, 354, "holdBack.mp3");
        track2 = new TrackImpl("Someday", "OneRepublic", "Pop", 2021, 391, "someDay.mp3");
        catalog.addTrack(track1);
        catalog.addTrack(track2);
    }

    // ── createPlaylist ────────────────────────

    /**
     * Verifica che una playlist con nome valido venga creata correttamente
     * e risulti presente nel catalogo con il nome specificato.
     */
    @Test
    void createPlaylist_success() {
        boolean result = controller.createPlaylist("Rock Classics");
        assertTrue(result);
        assertEquals(1, catalog.getPlaylists().size());
        assertEquals("Rock Classics", catalog.getPlaylists().get(0).getName());
    }

    /**
     * Verifica che la creazione di una playlist con nome già esistente
     * restituisca false e non aggiunga duplicati al catalogo.
     */
    @Test
    void createPlaylist_duplicateName_returnsFalse() {
        controller.createPlaylist("Rock Classics");
        boolean result = controller.createPlaylist("Rock Classics");
        assertFalse(result);
        assertEquals(1, catalog.getPlaylists().size());
    }

    /**
     * Verifica che la creazione di una playlist con nome vuoto
     * sollevi un'eccezione di tipo {@link IllegalArgumentException}.
     */
    @Test
    void createPlaylist_emptyName_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createPlaylist(""));
    }

    /**
     * Verifica che la creazione di una playlist con nome null
     * sollevi un'eccezione di tipo {@link IllegalArgumentException}.
     */
    @Test
    void createPlaylist_nullName_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createPlaylist(null));
    }

    // ── renamePlaylist ────────────────────────

    /**
     * Verifica che una playlist esistente venga rinominata correttamente
     * e che il nuovo nome sia riflesso nell'oggetto playlist.
     */
    @Test
    void renamePlaylist_success() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        boolean result = controller.renamePlaylist(playlist, "Best of Rock");
        assertTrue(result);
        assertEquals("Best of Rock", playlist.getName());
    }

    /**
     * Verifica che la rinomina di una playlist con un nome già usato
     * da un'altra playlist restituisca false e non modifichi il nome originale.
     */
    @Test
    void renamePlaylist_duplicateName_returnsFalse() {
        controller.createPlaylist("Rock Classics");
        controller.createPlaylist("Pop Hits");
        Playlist playlist = catalog.getPlaylists().get(0);
        boolean result = controller.renamePlaylist(playlist, "Pop Hits");
        assertFalse(result);
        assertEquals("Rock Classics", playlist.getName());
    }

    /**
     * Verifica che la rinomina di una playlist con nome vuoto
     * sollevi un'eccezione di tipo {@link IllegalArgumentException}.
     */
    @Test
    void renamePlaylist_emptyName_throwsException() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.renamePlaylist(playlist, ""));
    }

    /**
     * Verifica che la rinomina di una playlist non appartenente al catalogo
     * restituisce false.
     */
    @Test
    void renamePlaylist_notInCatalog_returnFalse() {
        ConcreteMusicCatalog otherCatalog = new ConcreteMusicCatalog();
        otherCatalog.createPlaylist("Other Playlist");
        Playlist otherPlaylist = otherCatalog.getPlaylists().get(0);
        boolean result = controller.renamePlaylist(otherPlaylist, "Pop Hits");
        assertFalse(result);
    }

    // ── deletePlaylist ────────────────────────

    /**
     * Verifica che una playlist esistente venga rimossa correttamente
     * dal catalogo.
     */
    @Test
    void deletePlaylist_success() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        boolean result = controller.deletePlaylist(playlist);
        assertTrue(result);
        assertEquals(0, catalog.getPlaylists().size());
    }

    /**
     * Verifica che la rimozione di una playlist non appartenente
     * al catalogo restituisca false senza modificare lo stato del catalogo.
     */
    @Test
    void deletePlaylist_notInCatalog_returnsFalse() {
        ConcreteMusicCatalog otherCatalog = new ConcreteMusicCatalog();
        otherCatalog.createPlaylist("Other Playlist");
        Playlist otherPlaylist = otherCatalog.getPlaylists().get(0);
        boolean result = controller.deletePlaylist(otherPlaylist);
        assertFalse(result);
    }

    // ── addTrackToPlaylist ────────────────────

    /**
     * Verifica che una traccia valida venga aggiunta correttamente
     * alla playlist e risulti presente nella lista delle tracce.
     */
    @Test
    void addTrackToPlaylist_success() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        boolean result = controller.addTrackToPlaylist(playlist, track1);
        assertTrue(result);
        assertEquals(1, playlist.getTracks().size());
        assertTrue(playlist.getTracks().contains(track1));
    }

    /**
     * Verifica che l'aggiunta di una traccia già presente nella playlist
     * restituisca false e non crei duplicati.
     */
    @Test
    void addTrackToPlaylist_duplicateTrack_returnsFalse() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        controller.addTrackToPlaylist(playlist, track1);
        boolean result = controller.addTrackToPlaylist(playlist, track1);
        assertFalse(result);
        assertEquals(1, playlist.getTracks().size());
    }

    /**
     * Verifica che più tracce aggiunte in sequenza mantengano
     * l'ordine di inserimento nella playlist.
     */
    @Test
    void addTrackToPlaylist_multipleTracksCorrectOrder() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        controller.addTrackToPlaylist(playlist, track1);
        controller.addTrackToPlaylist(playlist, track2);
        assertEquals(2, playlist.getTracks().size());
        assertEquals(track1, playlist.getTracks().get(0));
        assertEquals(track2, playlist.getTracks().get(1));
    }


    /**
     * Verifica che l'aggiunta di una traccia ad una playlist non appartenente
     * al catalogo sollevi un'eccezione.
     */
    @Test
    void addTrackToPlaylist_playlistNotInCatalog_throwsException() {
        ConcreteMusicCatalog otherCatalog = new ConcreteMusicCatalog();
        otherCatalog.createPlaylist("Other Playlist");
        Playlist otherPlaylist = otherCatalog.getPlaylists().get(0);
        assertThrows(IllegalArgumentException.class,
                () -> controller.addTrackToPlaylist(otherPlaylist, track1));
    }

    // ── removeTrackFromPlaylist ───────────────

    /**
     * Verifica che una traccia presente nella playlist venga rimossa
     * correttamente e che la playlist risulti vuota dopo la rimozione.
     */
    @Test
    void removeTrackFromPlaylist_success() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        controller.addTrackToPlaylist(playlist, track1);
        boolean result = controller.removeTrackFromPlaylist(playlist, track1);
        assertTrue(result);
        assertEquals(0, playlist.getTracks().size());
    }

    /**
     * Verifica che la rimozione di una traccia non presente nella playlist
     * restituisca false senza modificare lo stato della playlist.
     */
    @Test
    void removeTrackFromPlaylist_trackNotInPlaylist_returnsFalse() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        boolean result = controller.removeTrackFromPlaylist(playlist, track1);
        assertFalse(result);
    }

    /**
     * Verifica che la rimozione di una traccia dalla playlist non la elimini
     * dal catalogo generale — la traccia deve rimanere disponibile nel catalogo.
     */
    @Test
    void removeTrackFromPlaylist_trackRemainsInCatalog() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        controller.addTrackToPlaylist(playlist, track1);
        controller.removeTrackFromPlaylist(playlist, track1);
        assertTrue(catalog.getAllTracks().contains(track1));
    }
}