package com.example.gruppo04.model;

import com.example.gruppo04.interfaces.PlaybackStrategy;
import com.example.gruppo04.interfaces.Track;
import java.util.List;

/**
 * @brief Strategia di riproduzione sequenziale.
 * 
 * Riproduce i brani nell'ordine in cui si trovano nella lista.
 */
public class SequentialStrategy implements PlaybackStrategy {

    @Override
    public Track nextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        int nextIndex = currentIndex + 1;
        if (nextIndex >= 0 && nextIndex < tracks.size()) {
            return tracks.get(nextIndex);
        }
        return null;
    }
}
