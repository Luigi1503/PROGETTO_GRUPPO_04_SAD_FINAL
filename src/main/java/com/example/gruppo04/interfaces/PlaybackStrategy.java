package com.example.gruppo04.interfaces;

import java.util.List;

/**
 * @brief Interfaccia per la strategia di riproduzione dei brani musicali.
 * 
 * Questa interfaccia definisce il contratto per la selezione della traccia
 * successiva all'interno di una lista di tracce, in base alla strategia scelta.
 */
public interface PlaybackStrategy {

    /**
     * @brief Seleziona la traccia successiva nella lista.
     * 
     * @param sources La lista di {@PlayableSource} disponibili.
     * @param currentIndex L'indice della traccia corrente nella lista.
     * @return La source successiva selezionata, o null se non esiste una traccia successiva.
     */
    PlayableSource nextSource(List<PlayableSource> sources, int currentIndex);
}
