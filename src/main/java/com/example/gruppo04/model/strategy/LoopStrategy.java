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
        // In Loop la coda di sorgenti è ciclica: dopo l'ultima sorgente si torna
        // alla prima, così che lo skip playlist non fermi mai la riproduzione.
        int nextIndex = (currentIndex + 1) % sources.size();
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

    /**
     * {@inheritDoc}
     * In Loop la riproduzione resta sulla stessa sorgente: il "playlist precedente"
     * riavvia la playlist corrente, senza passare a quella precedente.
     */
    @Override
    public PlayableSource previousSource(List<PlayableSource> sources, int currentIndex) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        if (currentIndex >= 0 && currentIndex < sources.size()) {
            return sources.get(currentIndex);
        }
        return sources.get(0);
    }

    /**
     * {@inheritDoc}
     * Restituisce la traccia precedente in modo ciclico: dalla prima torna
     * all'ultima, senza mai uscire dalla sorgente corrente. Con un'unica
     * traccia resta sulla stessa.
     */
    @Override
    public Track previousTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        int prevIndex = (currentIndex - 1 + tracks.size()) % tracks.size();
        return tracks.get(prevIndex);
    }
}
