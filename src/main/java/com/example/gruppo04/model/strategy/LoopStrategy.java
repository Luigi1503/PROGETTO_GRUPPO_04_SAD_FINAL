package com.example.gruppo04.model.strategy;

import com.example.gruppo04.interfaces.PlayableSource;
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
        // In Loop la riproduzione resta sulla stessa sorgente: lo skip playlist
        // (e l'eventuale fine coda) riavvia la playlist corrente, senza passare
        // alla successiva.
        if (currentIndex >= 0 && currentIndex < sources.size()) {
            return sources.get(currentIndex);
        }
        return sources.get(0);
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
