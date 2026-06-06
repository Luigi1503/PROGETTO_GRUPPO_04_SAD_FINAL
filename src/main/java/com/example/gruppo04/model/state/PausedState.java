package com.example.gruppo04.model.state;


//Classe concreta del pattern State quando la canzone viene messa in pausa

public class PausedState implements PlayerState{
    @Override
    public void play(PlaybackState context) {
        context.changeState(new PlayingState());
    }

    @Override
    public void pause(PlaybackState context) {
        //non metto nulla siamo gia in pausa
    }

    @Override
    public void stop(PlaybackState context) {
        context.changeState(new StoppedState());
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isStopped(){
        return true;
    }
}