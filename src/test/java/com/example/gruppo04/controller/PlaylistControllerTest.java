package com.example.gruppo04.controller;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.TrackImpl;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaylistControllerTest {

    private PlaylistController controller;
    private MusicCatalog catalog;
    private Track track1;
    private Track track2;

    @BeforeEach
    void setUp() {
        catalog = new ConcreteMusicCatalog();
        controller = new PlaylistController(catalog);

        // Tracce di esempio
        track1 = new TrackImpl("Bohemian Rhapsody", "Queen", "Rock", 1975, 354);
        track2 = new TrackImpl("Hotel California", "Eagles", "Rock", 1977, 391);
        catalog.addTrack(track1);
        catalog.addTrack(track2);
    }

    // ── createPlaylist ────────────────────────

    @Test
    void createPlaylist_success() {
        boolean result = controller.createPlaylist("Rock Classics");
        assertTrue(result);
        assertEquals(1, catalog.getPlaylists().size());
        assertEquals("Rock Classics", catalog.getPlaylists().get(0).getName());
    }

    @Test
    void createPlaylist_duplicateName_returnsFalse() {
        controller.createPlaylist("Rock Classics");
        boolean result = controller.createPlaylist("Rock Classics");
        assertFalse(result);
        assertEquals(1, catalog.getPlaylists().size());
    }

    @Test
    void createPlaylist_emptyName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.createPlaylist(""));
    }

    @Test
    void createPlaylist_nullName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.createPlaylist(null));
    }

    // ── renamePlaylist ────────────────────────

    @Test
    void renamePlaylist_success() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        boolean result = controller.renamePlaylist(playlist, "Best Of Rock");
        assertTrue(result);
        assertEquals("Best Of Rock", playlist.getName());
    }

    @Test
    void renamePlaylist_duplicateName_returnsFalse() {
        controller.createPlaylist("Rock Classics");
        controller.createPlaylist("Pop Hits");
        Playlist playlist = catalog.getPlaylists().get(0);
        boolean result = controller.renamePlaylist(playlist, "Pop Hits");
        assertFalse(result);
        assertEquals("Rock Classics", playlist.getName());
    }

    @Test
    void renamePlaylist_emptyName_throwsException() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        assertThrows(IllegalArgumentException.class,
                () -> controller.renamePlaylist(playlist, ""));
    }

    // ── deletePlaylist ────────────────────────

    @Test
    void deletePlaylist_success() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        boolean result = controller.deletePlaylist(playlist);
        assertTrue(result);
        assertEquals(0, catalog.getPlaylists().size());
    }

    @Test
    void deletePlaylist_notInCatalog_returnsFalse() {
        // Playlist non aggiunta al catalogo
        ConcreteMusicCatalog otherCatalog = new ConcreteMusicCatalog();
        otherCatalog.createPlaylist("Other Playlist");
        Playlist otherPlaylist = otherCatalog.getPlaylists().get(0);
        boolean result = controller.deletePlaylist(otherPlaylist);
        assertFalse(result);
    }

    // ── addTrackToPlaylist ────────────────────

    @Test
    void addTrackToPlaylist_success() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        boolean result = controller.addTrackToPlaylist(playlist, track1);
        assertTrue(result);
        assertEquals(1, playlist.getTracks().size());
        assertTrue(playlist.getTracks().contains(track1));
    }

    @Test
    void addTrackToPlaylist_duplicateTrack_returnsFalse() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        controller.addTrackToPlaylist(playlist, track1);
        boolean result = controller.addTrackToPlaylist(playlist, track1);
        assertFalse(result);
        assertEquals(1, playlist.getTracks().size());
    }

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

    // ── removeTrackFromPlaylist ───────────────

    @Test
    void removeTrackFromPlaylist_success() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        controller.addTrackToPlaylist(playlist, track1);
        boolean result = controller.removeTrackFromPlaylist(playlist, track1);
        assertTrue(result);
        assertEquals(0, playlist.getTracks().size());
    }

    @Test
    void removeTrackFromPlaylist_trackNotInPlaylist_returnsFalse() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        boolean result = controller.removeTrackFromPlaylist(playlist, track1);
        assertFalse(result);
    }

    @Test
    void removeTrackFromPlaylist_trackRemainsInCatalog() {
        controller.createPlaylist("Rock Classics");
        Playlist playlist = catalog.getPlaylists().get(0);
        controller.addTrackToPlaylist(playlist, track1);
        controller.removeTrackFromPlaylist(playlist, track1);
        assertTrue(catalog.getAllTracks().contains(track1));
    }
}