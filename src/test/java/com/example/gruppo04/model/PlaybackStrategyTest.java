package com.example.gruppo04.model;

import com.example.gruppo04.interfaces.PlaybackStrategy;
import com.example.gruppo04.interfaces.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlaybackStrategyTest {

    private List<Track> tracks;
    private Track track1;
    private Track track2;
    private Track track3;

    @BeforeEach
    void setUp() {
        tracks = new ArrayList<>();
        track1 = new TrackImpl("Starlight", "Muse", "Rock", 2006, 240, "starlight.mp3");
        track2 = new TrackImpl("Hysteria", "Muse", "Rock", 2003, 227, "hysteria.mp3");
        track3 = new TrackImpl("Supermassive Black Hole", "Muse", "Rock", 2006, 209, "sbh.mp3");
        tracks.add(track1);
        tracks.add(track2);
        tracks.add(track3);
    }

    @Test
    void sequentialStrategy_returnsNextTrack() {
        PlaybackStrategy strategy = new SequentialStrategy();

        // Da inizio (-1) alla prima traccia
        assertEquals(track1, strategy.nextTrack(tracks, -1));

        // Dalla prima alla seconda traccia
        assertEquals(track2, strategy.nextTrack(tracks, 0));

        // Dalla seconda alla terza traccia
        assertEquals(track3, strategy.nextTrack(tracks, 1));

        // Dall'ultima traccia
        assertNull(strategy.nextTrack(tracks, 2));

        // Con lista vuota
        assertNull(strategy.nextTrack(new ArrayList<>(), 0));

        // Con lista null
        assertNull(strategy.nextTrack(null, 0));
    }

    @Test
    void loopStrategy_loopsCorrectly() {
        PlaybackStrategy strategy = new LoopStrategy();

        // Da inizio (-1) alla prima traccia
        assertEquals(track1, strategy.nextTrack(tracks, -1));

        // Dalla prima alla seconda traccia
        assertEquals(track2, strategy.nextTrack(tracks, 0));

        // Dalla seconda alla terza traccia
        assertEquals(track3, strategy.nextTrack(tracks, 1));

        // Dall'ultima traccia, ricomincia da capo
        assertEquals(track1, strategy.nextTrack(tracks, 2));

        // Con lista vuota
        assertNull(strategy.nextTrack(new ArrayList<>(), 0));

        // Con lista null
        assertNull(strategy.nextTrack(null, 0));
    }

    @Test
    void shuffleStrategy_returnsRandomTrack() {
        PlaybackStrategy strategy = new ShuffleStrategy();

        // Dovrebbe ritornare una traccia non nulla e presente nella lista
        Track next = strategy.nextTrack(tracks, 0);
        assertNotNull(next);
        assertTrue(tracks.contains(next));

        // Con lista vuota
        assertNull(strategy.nextTrack(new ArrayList<>(), 0));

        // Con lista null
        assertNull(strategy.nextTrack(null, 0));
    }
}
