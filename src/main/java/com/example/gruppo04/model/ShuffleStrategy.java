package com.example.gruppo04.model;

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
    public Track nextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        int randomIndex = random.nextInt(tracks.size());
        return tracks.get(randomIndex);
    }
}
