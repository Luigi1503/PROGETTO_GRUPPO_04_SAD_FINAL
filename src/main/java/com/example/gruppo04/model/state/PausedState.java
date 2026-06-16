package com.example.gruppo04.model.state;

/**
 * @class PausedState
 * @brief Classe concreta del pattern State quando la canzone viene messa in pausa.
 *
 * Rappresenta lo stato di riproduzione in pausa: la riproduzione è
 * temporaneamente sospesa ma resta "attiva", distinguendosi quindi dallo
 * stato {@link StoppedState}.
 */
public class PausedState implements PlayerState {

    /**
     * @brief Riprende la riproduzione passando allo stato {@link PlayingState}.
     * @param context Il contesto {@link PlaybackState} su cui applicare la transizione.
     */
    @Override
    public void play(PlaybackState context) {
        context.changeState(new PlayingState());
    }

    /**
     * @brief Metodo invocato quando si tenta di metter in pausa uno stato già in pausa.
     *
     * Non esegue alcuna transizione poiché lo stato è già {@link PausedState}.
     * @param context Il contesto {@link PlaybackState} (non utilizzato).
     */
    @Override
    public void pause(PlaybackState context) {
        //non metto nulla siamo gia in pausa
    }

    /**
     * @brief Interrompe la riproduzione passando allo stato {@link StoppedState}.
     * @param context Il contesto {@link PlaybackState} su cui applicare la transizione.
     */
    @Override
    public void stop(PlaybackState context) {
        context.changeState(new StoppedState());
    }

    /**
     * @brief Indica se la riproduzione è attualmente in corso.
     * @return false, poiché in pausa la riproduzione non è in corso.
     */
    @Override
    public boolean isPlaying() {
        return false;
    }

    /**
     * @brief Indica se la riproduzione è ferma (stopped).
     *
     * In pausa la riproduzione è ancora "attiva": non è uno stato fermo.
     * È distinguibile dallo stato Stopped tramite isPaused().
     * @return false, poiché lo stato di pausa non equivale a stop.
     */
    @Override
    public boolean isStopped(){
        // In pausa la riproduzione è ancora "attiva": NON è uno stato fermo.
        // Distinguibile da Stopped tramite isPaused().
        return false;
    }

    /**
     * @brief Indica se la riproduzione è attualmente in pausa.
     * @return true, poiché questo è lo stato di pausa.
     */
    @Override
    public boolean isPaused(){
        return true;
    }
}