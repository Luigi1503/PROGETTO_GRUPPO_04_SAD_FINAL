package com.example.gruppo04.model;

import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.PlaybackStrategy;
import com.example.gruppo04.interfaces.Track;
import java.util.List;

/**
 * @brief Strategia di riproduzione a ciclo continuo (Loop).
 * 
 * Riproduce i brani in sequenza, ricominciando dall'inizio una volta terminata la lista.
 */
public class LoopStrategy implements PlaybackStrategy {

    @Override
    public PlayableSource nextSource(List<PlayableSource> sources, int currentIndex) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        int nextIndex = (currentIndex + 1) % sources.size();
        if (nextIndex < 0) {
            nextIndex = 0;
        }
        return sources.get(nextIndex);
    }

    /**
     * {@inheritDoc}
     * Restituisce la traccia successiva in modo ciclico: dopo l'ultima
     * ricomincia dalla prima, senza mai uscire dalla sorgente corrente.
     */
    @Override
    public Track nextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        int nextIndex = (currentIndex + 1) % tracks.size();
        return tracks.get(nextIndex);
    }
}
