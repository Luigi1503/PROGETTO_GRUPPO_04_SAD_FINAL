package com.example.gruppo04.model.state;



//Classe concreta del pattern State quando la canzone viene avviata

public class PlayingState  implements PlayerState{
    @Override
    public void play(PlaybackState context) {
        //Non facciamo niente perché gia siamo in riproduzione
    }

    @Override
    public void pause(PlaybackState context) {
        context.changeState(new PausedState());
    }

    @Override
    public void stop(PlaybackState context) {
        context.resetPlaybackTime();
        context.changeState(new StoppedState());

    }

    @Override
    public boolean isPlaying() {
        return true;
    }

    @Override
    public boolean isStopped(){
        return false;
    }
}
