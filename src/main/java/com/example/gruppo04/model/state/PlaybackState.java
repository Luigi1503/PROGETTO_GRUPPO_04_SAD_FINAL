package com.example.gruppo04.model.state;


import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Track;

import java.util.ArrayList;
import java.util.List;

public class PlaybackState {

    private PlayerState currentState;
    private List<PlayableSource> queue;
    private int currentTime;
    private int currentSourceIndex;
    private int currentTrackIndex;
    private Track currentTrack;

    public PlaybackState() {
        this.currentState = new StoppedState();
        this.queue = new ArrayList<>();

        this.currentTime = 0;
        this.currentSourceIndex = 0;
        this.currentTrackIndex = 0;
        this.currentTrack = null;
    }










    public void changeState(PlayerState newState){
        this.currentState = newState;
    }


    public void resetPlaybackTime() {
        this.currentTime = 0;
    }

    public void loadQueue(List<PlayableSource> sources) {
        this.queue = new ArrayList<>(sources);

        this.currentSourceIndex = 0;
        this.currentTrackIndex = 0;

        if (!this.queue.isEmpty() && !this.queue.get(0).getTracks().isEmpty()) {
            this.currentTrack = this.queue.get(0).getTracks().get(0);
        }
    }


    public List<PlayableSource> getQueue(){
        return this.queue;
    }

    public Track getCurrentTrack(){
        return this.currentTrack;
    }

    public PlayableSource getCurrentSource(){
        if (this.queue == null || this.queue.isEmpty() || this.currentSourceIndex >= this.queue.size() || this.currentSourceIndex < 0) {
            return null;
        }
        return this.queue.get(this.currentSourceIndex);
    }

    public void setCurrentTrack(Track track){
        this.currentTrack = track;
    }

    public PlayableSource nextSource(){
        this.currentSourceIndex++;
        this.currentTrackIndex = 0;

        if (this.currentSourceIndex < this.queue.size()) {
            return this.queue.get(this.currentSourceIndex);
        }


        return null;
    }
    public void pause(){
        this.currentState.pause(this);
    }

    public void stop(){
        this.currentState.stop(this);
    }

    public void play() {
        this.currentState.play(this);
    }

    public boolean isPlaying(){
        return this.currentState.isPlaying();
    }


    public void addToQueue(PlayableSource source){
        this.queue.add(source);
    }


    public void removeFromQueue(PlayableSource source){
        this.queue.remove(source);
    }




}
