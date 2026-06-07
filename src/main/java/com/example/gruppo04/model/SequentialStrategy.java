package com.example.gruppo04.model;

import com.example.gruppo04.interfaces.PlayableSource;
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
    public PlayableSource nextSource(List<PlayableSource> sources, int currentIndex) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        int nextIndex = currentIndex + 1;
        if (nextIndex >= 0 && nextIndex < sources.size()) {
            return sources.get(nextIndex);
        }
        return null;
    }
}
