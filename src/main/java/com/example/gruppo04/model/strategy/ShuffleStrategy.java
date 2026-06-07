package com.example.gruppo04.model.strategy;

import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Track;
import java.util.List;
import java.util.Random;

/**
 * @brief Strategia di riproduzione casuale (Shuffle).
 *
 * Riproduce un brano casuale estratto dalla lista.
 */
public class ShuffleStrategy implements PlaybackStrategy {

    private final Random random = new Random();
    /**
     * {@inheritDoc}
     * Restituisce una sorgente casuale dalla lista (può coincidere con la corrente).
     */
    @Override
    public PlayableSource nextSource(List<PlayableSource> sources, int currentIndex) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        if (sources.size() == 1) {
            // unica sorgente → restituisce se stessa (comportamento loop)
            return sources.get(0);
        }
        int randomIndex;
        do {
            randomIndex = random.nextInt(sources.size());
        } while (randomIndex == currentIndex);
        return sources.get(randomIndex);
    }
    /**
     * {@inheritDoc}
     * Restituisce una traccia casuale diversa da quella corrente.
     * Se la sorgente ha una sola traccia, restituisce {@code null}
     * per segnalare di passare alla sorgente successiva.
     */
    @Override
    public Track nextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        if (tracks.size() == 1) {
            // unica traccia → fine sorgente → skipSource
            return null;
        }
        int randomIndex;
        do {
            randomIndex = random.nextInt(tracks.size());
        } while (randomIndex == currentIndex);
        return tracks.get(randomIndex);
    }
}
