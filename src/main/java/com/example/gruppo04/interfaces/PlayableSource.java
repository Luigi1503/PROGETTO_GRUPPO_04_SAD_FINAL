package com.example.gruppo04.interfaces;

import java.util.List;
import java.util.Map;

/**
 * @brief Interfaccia base per tutti gli elementi riproducibili del sistema musicale.
 * * @details Definisce il contratto comune per entità singole (come le tracce)
 * e per entità aggregate (come le playlist). Grazie a questa interfaccia,
 * i controller della UI e il motore del Media Player possono trattare
 * qualsiasi sorgente in modo polimorfico e trasparente, senza dover
 * implementare logiche separate per brani o playlist.
 */
public interface PlayableSource extends java.io.Serializable {

    /**
     * @brief Recupera la sequenza di brani associata a questa sorgente.
     * * @details Il comportamento varia in base all'implementazione:
     * - Se la sorgente è una singola {@link Track}, restituisce una lista di un solo elemento (se stessa).
     * - Se la sorgente è una {@link Playlist}, restituisce l'elenco completo dei brani contenuti.
     * * @return Una {@code List<Track>} rappresentante la coda da riprodurre.
     */
    public List<Track> getTracks();

    /**
     * @brief Recupera il dizionario dei metadati descrittivi della sorgente.
     * * @details Restituisce le informazioni necessarie per popolare dinamicamente
     * i componenti dell'interfaccia grafica (etichette, tabelle, player).
     * L'implementazione deve garantire chiavi coerenti (es. "Titolo", "Autore"
     * per le tracce; "Nome", "Brani Totali" per le playlist).
     * * @return Una {@code Map<String, String>} contenente le coppie chiave-valore da mostrare nella UI.
     */
    public Map<String, String> getDisplayName();
}