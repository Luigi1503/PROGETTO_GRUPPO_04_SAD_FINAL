package com.example.gruppo04.model.state;

/**
 * @brief Interfaccia per gli stati del riproduttore musicale (Pattern State).
 * Definisce i comandi a cui ogni stato concreto deve saper rispondere.
 */
public interface PlayerState {

    /**
     * @brief Gestisce la richiesta di avvio riproduzione.
     * @param context Il Context che mantiene la coda e gli indici.
     */
    void play(PlaybackState context);

    /**
     * @brief Gestisce la richiesta di messa in pausa.
     * @param context Il Context che mantiene la coda e gli indici.
     */
    void pause(PlaybackState context);

    /**
     * @brief Gestisce la richiesta di stop definitivo (con azzeramento istante).
     * @param context Il Context che mantiene la coda e gli indici.
     */
    void stop(PlaybackState context);

    /**
     * @brief Indica se lo stato attuale corrisponde a una riproduzione attiva.
     * Requisito esplicito del task T22.
     * @return true se la musica è in esecuzione, false altrimenti.
     */
    boolean isPlaying();

    /**
     * @brief Indica se la riproduzione è stata fermata del tutto (stato Stopped).
     * Nota: la pausa NON è uno stato fermo, quindi {@code PausedState} ritorna
     * {@code false} qui e {@code true} su {@link #isPaused()}.
     * @return true se la musica è ferma, false altrimenti.
     */
    boolean isStopped();

    /**
     * @brief Indica se lo stato attuale corrisponde a una riproduzione in pausa.
     * Permette alle View di distinguere "in pausa" (riproduzione ancora attiva,
     * brano e sorgente correnti da mantenere) da "ferma".
     * @return true se la musica è in pausa, false altrimenti.
     */
    boolean isPaused();
}