package com.example.gruppo04.model.strategy;

import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Track;
import java.util.List;
import java.util.Random;

/**
 * @brief Strategia di riproduzione casuale (Shuffle).
 *
 * <p>In avanti estrae sempre un elemento casuale. Il "torna indietro" a livello
 * di traccia (⏮) è gestito dal client tramite lo storico di riproduzione reale
 * (vedi {@link #usesHistoryForPrevious()}), così da poter tornare al brano che
 * ha effettivamente preceduto quello corrente anche quando si trova in un'altra
 * sorgente (es. nel catalogo). Il "playlist precedente" (⏮⏮) resta casuale,
 * coerentemente con la voce del pannello Help "playlist casuale dal catalogo".</p>
 */
public class ShuffleStrategy implements PlaybackStrategy {

    private final Random random = new Random();

    /**
     * {@inheritDoc}
     * Restituisce una sorgente casuale diversa dalla corrente.
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

    /**
     * {@inheritDoc}
     * Restituisce una sorgente casuale diversa dalla corrente
     * ("playlist casuale dal catalogo").
     */
    @Override
    public PlayableSource previousSource(List<PlayableSource> sources, int currentIndex) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        if (sources.size() == 1) {
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
     * In Shuffle la navigazione ⏮ è gestita dal client tramite lo storico reale
     * ({@link #usesHistoryForPrevious()}); questo metodo non viene quindi usato
     * per il "torna indietro" e si limita a restituire la traccia corrente
     * (riavvio da capo) come comportamento di sicurezza.
     */
    @Override
    public Track previousTrack(List<Track> tracks, int currentIndex) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        if (currentIndex >= 0 && currentIndex < tracks.size()) {
            return tracks.get(currentIndex);
        }
        return tracks.get(0);
    }

    /**
     * {@inheritDoc}
     * In Shuffle ⏮ deve tornare al brano realmente precedente (anche in un'altra
     * sorgente), quindi si appoggia allo storico di riproduzione del client.
     */
    @Override
    public boolean usesHistoryForPrevious() {
        return true;
    }
}