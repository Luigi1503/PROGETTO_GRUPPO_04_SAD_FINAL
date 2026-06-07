package com.example.gruppo04.model;

import com.example.gruppo04.model.strategy.LoopStrategy;
import com.example.gruppo04.model.strategy.PlaybackStrategy;
import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.strategy.SequentialStrategy;
import com.example.gruppo04.model.strategy.ShuffleStrategy;
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
        // Prepara lista di PlayableSource a partire dalle tracce
        List<PlayableSource> sources = new ArrayList<>();
        sources.addAll(tracks);

        // Da inizio (-1) alla prima traccia
        assertEquals(track1, strategy.nextSource(sources, -1));

        // Dalla prima alla seconda traccia
        assertEquals(track2, strategy.nextSource(sources, 0));

        // Dalla seconda alla terza traccia
        assertEquals(track3, strategy.nextSource(sources, 1));

        // Dall'ultima traccia
        assertNull(strategy.nextSource(sources, 2));

        // Con lista vuota
        assertNull(strategy.nextSource(new ArrayList<>(), 0));

        // Con lista null
        assertNull(strategy.nextSource(null, 0));
    }

    @Test
    void loopStrategy_loopsCorrectly() {
        PlaybackStrategy strategy = new LoopStrategy();
        List<PlayableSource> sources = new ArrayList<>();
        sources.addAll(tracks);

        // Da inizio (-1) alla prima traccia
        assertEquals(track1, strategy.nextSource(sources, -1));

        // Dalla prima alla seconda traccia
        assertEquals(track2, strategy.nextSource(sources, 0));

        // Dalla seconda alla terza traccia
        assertEquals(track3, strategy.nextSource(sources, 1));

        // Dall'ultima traccia, ricomincia da capo
        assertEquals(track1, strategy.nextSource(sources, 2));

        // Con lista vuota
        assertNull(strategy.nextSource(new ArrayList<>(), 0));

        // Con lista null
        assertNull(strategy.nextSource(null, 0));
    }

    @Test
    void shuffleStrategy_returnsRandomTrack() {
        PlaybackStrategy strategy = new ShuffleStrategy();
        List<PlayableSource> sources = new ArrayList<>();
        sources.addAll(tracks);

        // Dovrebbe ritornare una sorgente non nulla e presente nella lista
        PlayableSource next = strategy.nextSource(sources, 0);
        assertNotNull(next);
        assertTrue(sources.contains(next));

        // Con lista vuota
        assertNull(strategy.nextSource(new ArrayList<>(), 0));

        // Con lista null
        assertNull(strategy.nextSource(null, 0));
    }
}
