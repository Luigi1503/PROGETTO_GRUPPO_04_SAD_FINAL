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
     * @param tracks La lista di tracce disponibili.
     * @param currentIndex L'indice della traccia corrente nella lista.
     * @return La traccia successiva selezionata, o null se non esiste una traccia successiva.
     */
    Track nextTrack(List<Track> tracks, int currentIndex);
}
