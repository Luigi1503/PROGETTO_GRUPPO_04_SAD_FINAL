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
 * @class PreviousNavigationTest
 * @brief Verifica la navigazione "indietro" (previousTrack/previousSource) per le tre strategie.
 *
 * A differenza di {@link PlaybackControllerTest} gli stub restituiscono un
 * filePath NON nullo (file inesistente): così avviaAudioFisico tenta una
 * riproduzione che fallisce in modo asincrono senza ricorsione, lasciando
 * lo stato del controller deterministico e ispezionabile.
 */
class PreviousNavigationTest {

    /**
     * @brief Crea una traccia stub con titolo specificato per i test.
     * @param title Il titolo da assegnare alla traccia.
     * @return Un'istanza di {@link Track} con dati fittizi e filePath non nullo
     * ma puntante a un file inesistente, per evitare ricorsione in skipTrack.
     */
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

    /**
     * @brief Crea una sorgente riproducibile stub contenente le tracce specificate.
     * @param name Il nome da assegnare alla sorgente.
     * @param tracks Le tracce da includere nella sorgente.
     * @return Un'istanza di {@link PlayableSource} con dati fittizi.
     */
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

    /**
     * @brief Inizializza il fixture di test prima di ogni esecuzione.
     *
     * Resetta il catalogo musicale, crea un nuovo controller con il relativo
     * stato di riproduzione e prepara tracce e sorgenti stub da utilizzare nei test.
     */
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

    /**
     * @brief Verifica che in modalità Sequential la fine di una sorgente avanzi alla successiva.
     *
     * Controlla che, con più sorgenti in coda, al termine dell'ultima traccia
     * di una sorgente il controller passi automaticamente alla sorgente successiva.
     */
    @Test
    void sequential_endOfSource_advancesToNextSourceWhenQueueHasMore() {
        controller.changeStrategy(new SequentialStrategy());
        controller.play(List.of(s1, s2, s3), s1, t2);

        controller.skipTrack();

        assertEquals(s2, controller.getCurrentSource(),
                "Con più sorgenti in coda, Sequential deve passare alla successiva");
        assertEquals(t3, controller.getCurrentTrack());
    }

    /**
     * @brief Verifica che in modalità Shuffle, previousTrack senza storico riavvii la traccia corrente.
     *
     * Controlla che, in assenza di uno storico di riproduzione, l'azione
     * "indietro" non scelga una traccia casuale ma riparta dalla traccia attuale.
     */
    @Test
    void shuffle_previousWithoutHistory_restartsCurrentTrack() {
        controller.changeStrategy(new ShuffleStrategy());
        PlayableSource playlist = stubSource("PL", t1, t2, t3, t4);
        controller.play(List.of(playlist), playlist, t2);

        Track before = controller.getCurrentTrack();
        controller.previousTrack();

        assertEquals(before, controller.getCurrentTrack(),
                "Senza storico, Shuffle ⏮ deve riavviare la traccia corrente, non sceglierne una a caso");
    }

    /**
     * @brief Verifica che in modalità Shuffle, previousTrack torni alla traccia precedentemente riprodotta.
     *
     * Controlla che dopo uno skip (che genera una traccia casuale e accoda
     * quella precedente nello storico) l'azione "indietro" recuperi correttamente
     * la traccia riprodotta in precedenza.
     */
    @Test
    void shuffle_previousReturnsToPreviouslyPlayedTrack() {
        controller.changeStrategy(new ShuffleStrategy());
        PlayableSource playlist = stubSource("PL", t1, t2, t3, t4);
        controller.play(List.of(playlist), playlist, t1);

        controller.skipTrack();
        Track afterSkip = controller.getCurrentTrack();
        controller.previousTrack();

        assertNotEquals(afterSkip, controller.getCurrentTrack());
        assertEquals(t1, controller.getCurrentTrack(),
                "Shuffle ⏮ deve tornare alla traccia precedentemente riprodotta");
    }

    /**
     * @brief Verifica che in modalità Shuffle sul catalogo, previousTrack torni alla reale traccia precedente.
     *
     * Controlla che, trattando ogni traccia del catalogo come una sorgente
     * a sé stante, dopo due skip consecutivi l'azione "indietro" recuperi
     * la traccia realmente precedente (e non sempre la stessa) sia come
     * traccia corrente che come sorgente corrente.
     */
    @Test
    void shuffle_catalog_previousReturnsTheActuallyPreviousTrack() {
        controller.changeStrategy(new ShuffleStrategy());
        List<PlayableSource> catalogQueue = List.of(t1, t2, t3, t4);
        controller.play(catalogQueue, t1, null);

        controller.skipTrack();
        Track playedBefore = controller.getCurrentTrack();
        controller.skipTrack();

        controller.previousTrack();

        assertEquals(playedBefore, controller.getCurrentTrack(),
                "Shuffle nel catalogo: ⏮ deve tornare alla traccia realmente precedente, non sempre la stessa");
        assertEquals(playedBefore, controller.getCurrentSource(),
                "Nel catalogo la sorgente coincide con la traccia: deve seguire lo storico");
    }

    /**
     * @brief Verifica che in modalità Loop, previousSource non cambi la sorgente corrente.
     *
     * Controlla che l'azione "indietro" a livello di sorgente, in modalità
     * Loop, mantenga il controller sulla sorgente attualmente in riproduzione
     * senza passare a un'altra playlist.
     */
    @Test
    void loop_previousSource_staysOnCurrentSource() {
        controller.changeStrategy(new LoopStrategy());
        controller.play(List.of(s1, s2, s3), s2, t3);

        controller.previousSource();

        assertEquals(s2, controller.getCurrentSource(),
                "In Loop, ⏮⏮ deve restare sulla sorgente corrente, non andare a un'altra playlist");
    }

    /**
     * @brief Verifica che in modalità Loop, previousTrack dalla prima traccia si avvolga sull'ultima della stessa sorgente.
     *
     * Controlla che, trovandosi sulla prima traccia di una playlist, l'azione
     * "indietro" non cambi sorgente ma riporti ciclicamente all'ultima traccia
     * della stessa playlist.
     */
    @Test
    void loop_previousTrack_atFirstTrack_wrapsWithinSameSource() {
        controller.changeStrategy(new LoopStrategy());
        PlayableSource playlist = stubSource("PL", t1, t2, t3);
        controller.play(List.of(playlist, s2), playlist, t1);

        controller.previousTrack();

        assertEquals(playlist, controller.getCurrentSource(),
                "In Loop, ⏮ non deve cambiare playlist");
        assertEquals(t3, controller.getCurrentTrack(),
                "In Loop, ⏮ dalla prima traccia torna all'ultima della stessa playlist");
    }
}