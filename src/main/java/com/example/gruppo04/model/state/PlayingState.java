package com.example.gruppo04.model.state;

/**
 * @class PlayingState
 * @brief Classe concreta del pattern State quando la canzone viene avviata.
 *
 * Rappresenta lo stato di riproduzione attiva: la traccia corrente è
 * effettivamente in esecuzione.
 */
public class PlayingState  implements PlayerState{

    /**
     * @brief Metodo invocato quando si tenta di avviare la riproduzione già attiva.
     *
     * Non esegue alcuna transizione poiché lo stato è già {@link PlayingState}.
     * @param context Il contesto {@link PlaybackState} (non utilizzato).
     */
    @Override
    public void play(PlaybackState context) {
        //Non facciamo niente perché gia siamo in riproduzione
    }

    /**
     * @brief Metti in pausa la riproduzione passando allo stato {@link PausedState}.
     * @param context Il contesto {@link PlaybackState} su cui applicare la transizione.
     */
    @Override
    public void pause(PlaybackState context) {
        context.changeState(new PausedState());
    }

    /**
     * @brief Interrompe la riproduzione passando allo stato {@link StoppedState}.
     *
     * Azzera il tempo di riproduzione corrente prima di effettuare la transizione.
     * @param context Il contesto {@link PlaybackState} su cui applicare la transizione.
     */
    @Override
    public void stop(PlaybackState context) {
        context.resetPlaybackTime();
        context.changeState(new StoppedState());
    }

    /**
     * @brief Indica se la riproduzione è attualmente in corso.
     * @return true, poiché questo è lo stato di riproduzione attiva.
     */
    @Override
    public boolean isPlaying() {
        return true;
    }

    /**
     * @brief Indica se la riproduzione è ferma (stopped).
     * @return false, poiché la riproduzione è attiva.
     */
    @Override
    public boolean isStopped(){
        return false;
    }

    /**
     * @brief Indica se la riproduzione è attualmente in pausa.
     * @return false, poiché la riproduzione è attiva e non in pausa.
     */
    @Override
    public boolean isPaused(){
        return false;
    }
}