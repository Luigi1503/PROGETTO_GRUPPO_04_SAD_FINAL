package com.example.gruppo04.model;

import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.PlaybackStrategy;
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

    @Override
    public PlayableSource nextSource(List<PlayableSource> sources, int currentIndex) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        int randomIndex = random.nextInt(sources.size());
        return sources.get(randomIndex);
    }
}
