package com.example.gruppo04.model.state;

/**
 * @class StoppedState
 * @brief Classe concreta del pattern State quando la canzone viene "killata", ossia interrotta completamente e non semplicemente messa in pausa.
 *
 * Rappresenta lo stato di riproduzione ferma: nessuna traccia è in
 * esecuzione e il tempo di riproduzione è stato azzerato.
 */
public class StoppedState implements PlayerState{

    /**
     * @brief Avvia la riproduzione passando allo stato {@link PlayingState}.
     * @param context Il contesto {@link PlaybackState} su cui applicare la transizione.
     */
    @Override
    public void play(PlaybackState context) {
        context.changeState(new PlayingState());
    }

    /**
     * @brief Metodo invocato quando si tenta di metter in pausa uno stato già fermo.
     *
     * Non esegue alcuna transizione poiché lo stato è già {@link StoppedState}.
     * @param context Il contesto {@link PlaybackState} (non utilizzato).
     */
    @Override
    public void pause(PlaybackState context) {
        //siamo gia fermi
    }

    /**
     * @brief Metodo invocato quando si tenta di fermare uno stato già fermo.
     *
     * Non esegue alcuna transizione poiché lo stato è già {@link StoppedState}.
     * @param context Il contesto {@link PlaybackState} (non utilizzato).
     */
    @Override
    public void stop(PlaybackState context) {
        //siamo gia fermi
    }

    /**
     * @brief Indica se la riproduzione è attualmente in corso.
     * @return false, poiché la riproduzione è ferma.
     */
    @Override
    public boolean isPlaying() {
        return false;
    }

    /**
     * @brief Indica se la riproduzione è ferma (stopped).
     * @return true, poiché questo è lo stato di riproduzione ferma.
     */
    @Override
    public boolean isStopped(){
        return true;
    }

    /**
     * @brief Indica se la riproduzione è attualmente in pausa.
     * @return false, poiché lo stato fermo non equivale a pausa.
     */
    @Override
    public boolean isPaused(){
        return false;
    }
}