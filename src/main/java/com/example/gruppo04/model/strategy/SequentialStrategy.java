package com.example.gruppo04.model.strategy;

import com.example.gruppo04.interfaces.PlayableSource;
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

    /**
            * {@inheritDoc}
     * Restituisce la traccia all'indice successivo, o {@code null} se è l'ultima
     * (segnale per passare alla sorgente successiva).
            */
    @Override
    public Track nextTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        int nextIndex = currentIndex + 1;
        if (nextIndex < tracks.size()) {
            return tracks.get(nextIndex);
        }
        return null; // fine sorgente → skipSource
    }

    /**
     * {@inheritDoc}
     * Restituisce la sorgente all'indice precedente, o {@code null} se è la prima.
     */
    @Override
    public PlayableSource previousSource(List<PlayableSource> sources, int currentIndex) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        int prevIndex = currentIndex - 1;
        if (prevIndex >= 0 && prevIndex < sources.size()) {
            return sources.get(prevIndex);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Restituisce la traccia all'indice precedente, o {@code null} se è la prima
     * (segnale per passare alla sorgente precedente).
     */
    @Override
    public Track previousTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        int prevIndex = currentIndex - 1;
        if (prevIndex >= 0) {
            return tracks.get(prevIndex);
        }
        return null; // inizio sorgente → previousSource
    }
}
