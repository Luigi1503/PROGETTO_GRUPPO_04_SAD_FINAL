package com.example.gruppo04.model.state;

//Classe concreta del pattern State quando la canzone viene "killata" insomma non viene messa in pausa
public class StoppedState implements PlayerState{
    @Override
    public void play(PlaybackState context) {
        context.changeState(new PlayingState());
    }

    @Override
    public void pause(PlaybackState context) {
        //siamo gia fermi
    }

    @Override
    public void stop(PlaybackState context) {
        //siamo gia fermi
    }

    @Override
    public boolean isPlaying() {
        return false;
    }
}