package com.example.gruppo04.controller;

import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.TagType;
import com.example.gruppo04.model.state.PlaybackState;
import com.example.gruppo04.model.strategy.LoopStrategy;
import com.example.gruppo04.model.strategy.SequentialStrategy;
import com.example.gruppo04.model.strategy.ShuffleStrategy;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica la navigazione "indietro" (previousTrack/previousSource) per le tre
 * strategie, allineata al pannello Help.
 *
 * <p>A differenza di {@link PlaybackControllerTest} gli stub restituiscono un
 * {@code filePath} NON nullo (file inesistente): così {@code avviaAudioFisico}
 * tenta una riproduzione che fallisce in modo asincrono senza ricorsione,
 * lasciando lo stato del controller deterministico e ispezionabile.</p>
 */
class PreviousNavigationTest {

    private static Track stubTrack(String title) {
        return new Track() {
            private final UUID id = UUID.randomUUID();
            private int playCount;
            private final Set<TagType> tags = new java.util.HashSet<>();
            @Override public UUID getId()           { return id; }
            @Override public String getTitle()      { return title; }
            @Override public String getAuthor()     { return "Autore"; }
            @Override public String getGenre()      { return "Pop"; }
            @Override public int getYear()          { return 2020; }
            @Override public int getDuration()      { return 180; }
            // NON nullo (ma file inesistente): evita la ricorsione di skipTrack
            @Override public String getFilePath()   { return "file-inesistente.mp3"; }
            @Override public void setTitle(String t)   {}
            @Override public void setAuthor(String a)  {}
            @Override public void setGenre(String g)   {}
            @Override public void setYear(int y)       {}
            @Override public void setDuration(int d)   {}
            @Override public void setFilePath(String p){}
            @Override public void incrementPlayCount() { playCount++; }
            @Override public int getPlayCount() { return playCount; }
            @Override public void addTag(TagType tag) { tags.add(tag); }
            @Override public void removeTag(TagType tag) { tags.remove(tag); }
            @Override public Set<TagType> getTags() { return Set.copyOf(tags); }
            @Override public boolean hasTag(TagType tag) { return tags.contains(tag); }
            @Override public List<Track> getTracks() { return List.of(this); }
            @Override public Map<String, String> getDisplayName() {
                return Map.of("Titolo", title);
            }
        };
    }

    private static PlayableSource stubSource(String name, Track... tracks) {
        List<Track> trackList = List.of(tracks);
        return new PlayableSource() {
            @Override public List<Track> getTracks() { return trackList; }
            @Override public Map<String, String> getDisplayName() { return Map.of("Nome", name); }
        };
    }

    private PlaybackController controller;
    private PlaybackState state;
    private Track t1, t2, t3, t4;
    private PlayableSource s1, s2, s3;

    @BeforeEach
    void setUp() {
        ConcreteMusicCatalog.getInstance().reset();
        state = new PlaybackState();
        controller = new PlaybackController(state);

        t1 = stubTrack("T1");
        t2 = stubTrack("T2");
        t3 = stubTrack("T3");
        t4 = stubTrack("T4");

        s1 = stubSource("S1", t1, t2);
        s2 = stubSource("S2", t3);
        s3 = stubSource("S3", t4);
    }

    // ── Issue 1: in Sequential, la fine di una sorgente avanza alla successiva ──
    @Test
    void sequential_endOfSource_advancesToNextSourceWhenQueueHasMore() {
        controller.changeStrategy(new SequentialStrategy());
        controller.play(List.of(s1, s2, s3), s1, t2); // ultima traccia di s1

        controller.skipTrack(); // fine s1 → s2

        assertEquals(s2, controller.getCurrentSource(),
                "Con più sorgenti in coda, Sequential deve passare alla successiva");
        assertEquals(t3, controller.getCurrentTrack());
    }

    // ── Issue 2: in Shuffle, previous senza storico riparte la traccia corrente ──
    @Test
    void shuffle_previousWithoutHistory_restartsCurrentTrack() {
        controller.changeStrategy(new ShuffleStrategy());
        PlayableSource playlist = stubSource("PL", t1, t2, t3, t4);
        controller.play(List.of(playlist), playlist, t2);

        Track before = controller.getCurrentTrack();
        controller.previousTrack(); // nessuno storico → riavvia la corrente

        assertEquals(before, controller.getCurrentTrack(),
                "Senza storico, Shuffle ⏮ deve riavviare la traccia corrente, non sceglierne una a caso");
    }

    @Test
    void shuffle_previousReturnsToPreviouslyPlayedTrack() {
        controller.changeStrategy(new ShuffleStrategy());
        PlayableSource playlist = stubSource("PL", t1, t2, t3, t4);
        controller.play(List.of(playlist), playlist, t1);

        controller.skipTrack();              // t1 → traccia casuale (t1 va nello storico)
        Track afterSkip = controller.getCurrentTrack();
        controller.previousTrack();          // torna alla precedentemente riprodotta = t1

        assertNotEquals(afterSkip, controller.getCurrentTrack());
        assertEquals(t1, controller.getCurrentTrack(),
                "Shuffle ⏮ deve tornare alla traccia precedentemente riprodotta");
    }

    @Test
    void shuffle_catalog_previousReturnsTheActuallyPreviousTrack() {
        controller.changeStrategy(new ShuffleStrategy());
        // Nel catalogo ogni traccia è una sorgente a sé.
        List<PlayableSource> catalogQueue = List.of(t1, t2, t3, t4);
        controller.play(catalogQueue, t1, null);

        controller.skipTrack();                       // → traccia casuale X
        Track playedBefore = controller.getCurrentTrack();
        controller.skipTrack();                       // → traccia casuale Y (≠ X di norma)

        controller.previousTrack();                   // ⏮ → deve tornare a X (reale precedente)

        assertEquals(playedBefore, controller.getCurrentTrack(),
                "Shuffle nel catalogo: ⏮ deve tornare alla traccia realmente precedente, non sempre la stessa");
        assertEquals(playedBefore, controller.getCurrentSource(),
                "Nel catalogo la sorgente coincide con la traccia: deve seguire lo storico");
    }

    // ── Issue 3: in Loop, indietro non lascia la playlist corrente ──
    @Test
    void loop_previousSource_staysOnCurrentSource() {
        controller.changeStrategy(new LoopStrategy());
        controller.play(List.of(s1, s2, s3), s2, t3);

        controller.previousSource(); // ⏮⏮ in Loop → resta su s2

        assertEquals(s2, controller.getCurrentSource(),
                "In Loop, ⏮⏮ deve restare sulla sorgente corrente, non andare a un'altra playlist");
    }

    @Test
    void loop_previousTrack_atFirstTrack_wrapsWithinSameSource() {
        controller.changeStrategy(new LoopStrategy());
        PlayableSource playlist = stubSource("PL", t1, t2, t3);
        controller.play(List.of(playlist, s2), playlist, t1); // prima traccia

        controller.previousTrack(); // ciclico → ultima traccia della STESSA playlist

        assertEquals(playlist, controller.getCurrentSource(),
                "In Loop, ⏮ non deve cambiare playlist");
        assertEquals(t3, controller.getCurrentTrack(),
                "In Loop, ⏮ dalla prima traccia torna all'ultima della stessa playlist");
    }
}