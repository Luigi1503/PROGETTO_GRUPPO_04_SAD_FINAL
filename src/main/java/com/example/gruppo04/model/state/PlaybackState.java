package com.example.gruppo04.model.state;


import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.strategy.PlaybackStrategy;
import com.example.gruppo04.model.strategy.SequentialStrategy;

import java.util.ArrayList;
import java.util.List;

public class PlaybackState {

    private PlayerState currentState;
    private List<PlayableSource> queue;
    private int currentTime;
    private int currentSourceIndex;
    private int currentTrackIndex;
    private Track currentTrack;
    private PlaybackStrategy strategy;

    public PlaybackState() {
        this.currentState = new StoppedState();
        this.queue = new ArrayList<>();

        this.currentTime = 0;
        this.currentSourceIndex = 0;
        this.currentTrackIndex = 0;
        this.currentTrack = null;
        //necessaria per inizializzare correttamente la strategia di default
        this.strategy = new SequentialStrategy();
    }

    public void setStrategy(PlaybackStrategy strategy) {
        this.strategy = strategy;
    }

    public PlaybackStrategy getStrategy() {
        return this.strategy;
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

    /**
     * Aggiorna la coda di riproduzione con un nuovo insieme di sorgenti
     * (es. dopo la modifica di una playlist) preservando la sorgente e la
     * traccia attualmente in riproduzione.
     * <p>
     * La sorgente corrente viene ri-localizzata nella nuova coda tramite
     * {@code equals} (per le playlist l'uguaglianza è basata sull'id), così da
     * agganciare l'istanza aggiornata e mantenere coerente l'indice corrente.
     * Se la sorgente corrente non è più presente, l'indice viene riportato in
     * un intervallo valido.
     * </p>
     *
     * @param sources la nuova lista di sorgenti; non deve essere {@code null}
     */
    public void refreshQueue(List<PlayableSource> sources) {
        PlayableSource current = getCurrentSource();
        this.queue = new ArrayList<>(sources);

        int idx = (current != null) ? this.queue.indexOf(current) : -1;
        if (idx >= 0) {
            this.currentSourceIndex = idx;
            // Re-align currentTrack to the instance contained in the refreshed source
            Track oldCurrentTrack = this.currentTrack;
            if (oldCurrentTrack != null) {
                List<Track> newTracks = this.queue.get(idx).getTracks();
                int newTrackIdx = newTracks.indexOf(oldCurrentTrack);
                if (newTrackIdx >= 0) {
                    this.currentTrack = newTracks.get(newTrackIdx);
                } else {
                    // If the current track no longer exists in the refreshed source,
                    // fall back to the first track (if any) to keep a valid state.
                    if (!newTracks.isEmpty()) {
                        this.currentTrack = newTracks.get(0);
                    } else {
                        this.currentTrack = null;
                    }
                }
            }
        } else if (this.currentSourceIndex >= this.queue.size()) {
            this.currentSourceIndex = Math.max(0, this.queue.size() - 1);
        }
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

    public boolean isStopped() {
        return this.currentState.isStopped();
    }

    public boolean isPaused() {
        return this.currentState.isPaused();
    }

    public void setCurrentSource(PlayableSource source) {
        this.currentSourceIndex = this.queue.indexOf(source);
        if (!source.getTracks().isEmpty()) {
            this.currentTrack = source.getTracks().get(0);
        }
    }

    public PlayerState getStatus() {
        return this.currentState;
    }
}
