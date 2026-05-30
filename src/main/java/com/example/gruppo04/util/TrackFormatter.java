package com.example.gruppo04.util;

/**
 * Classe di utilità per la formattazione dei dati delle tracce.
 * Non istanziabile — espone solo metodi statici.
 */
public class TrackFormatter {

    /** Costruttore privato — classe non istanziabile. */
    private TrackFormatter() {}

    /**
     * Converte una durata in secondi nel formato mm:ss.
     *
     * @param seconds la durata in secondi
     * @return stringa nel formato mm:ss
     */
    public static String formatDuration(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}