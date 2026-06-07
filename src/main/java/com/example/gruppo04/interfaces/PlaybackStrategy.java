package com.example.gruppo04.interfaces;

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
}
