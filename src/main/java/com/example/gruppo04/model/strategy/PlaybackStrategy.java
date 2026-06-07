package com.example.gruppo04.model.strategy;

import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Track;

import java.util.List;

/**
 * @brief Interfaccia per la strategia di riproduzione dei brani musicali.
 *
 * Definisce il contratto per la selezione della sorgente successiva
 * all'interno di una lista di sorgenti riproducibili, in base alla
 * strategia scelta.
 */
public interface PlaybackStrategy {

    /**
     * @brief Seleziona la sorgente successiva nella lista.
     *
     * @param sources la lista di {@link PlayableSource} disponibili.
     * @param currentIndex l'indice della sorgente corrente nella lista.
     * @return la sorgente successiva selezionata, o {@code null} se non esiste.
     */
    PlayableSource nextSource(List<PlayableSource> sources, int currentIndex);

    /**
     * @brief Seleziona la traccia successiva all'interno di una sorgente.
     *
     * <p>La logica varia in base alla strategia:</p>
     * <ul>
     *   <li><b>Sequential</b>: traccia successiva in ordine; {@code null} se è l'ultima.</li>
     *   <li><b>Loop</b>: traccia successiva in ordine; torna alla prima se è l'ultima.</li>
     *   <li><b>Shuffle</b>: traccia casuale tra quelle disponibili, diversa dalla corrente.</li>
     * </ul>
     *
     * @param tracks       la lista di {@link Track} della sorgente corrente.
     * @param currentIndex l'indice della traccia corrente nella lista.
     * @return la traccia successiva selezionata, o {@code null} se la sorgente è terminata
     *         e si deve passare alla sorgente successiva.
     */
    Track nextTrack(List<Track> tracks, int currentIndex);
}
