package com.example.gruppo04.model.state;


import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.strategy.PlaybackStrategy;
import com.example.gruppo04.model.strategy.SequentialStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * @class PlaybackState
 * @brief Mantiene lo stato corrente della riproduzione musicale.
 *
 * Funziona da contesto per il pattern State (gestendo le transizioni tra
 * {@link PlayerState}) e mantiene la coda di sorgenti riproducibili, la
 * traccia/sorgente correntemente in riproduzione e la strategia di
 * navigazione attiva (pattern Strategy).
 */
public class PlaybackState {

    private PlayerState currentState;
    private List<PlayableSource> queue;
    private int currentTime;
    private int currentSourceIndex;
    private int currentTrackIndex;
    private Track currentTrack;
    private PlaybackStrategy strategy;

    /**
     * @brief Costruisce un nuovo stato di riproduzione con valori di default.
     *
     * Inizializza lo stato a {@link StoppedState}, la coda vuota, gli indici
     * a zero, nessuna traccia corrente e la strategia di default a
     * {@link SequentialStrategy}.
     */
    public PlaybackState() {
        this.currentState = new StoppedState();
        this.queue = new ArrayList<>();

        this.currentTime = 0;
        this.currentSourceIndex = 0;
        this.currentTrackIndex = 0;
        this.currentTrack = null;
        this.strategy = new SequentialStrategy();
    }

    /**
     * @brief Imposta la strategia di navigazione da utilizzare.
     * @param strategy La nuova {@link PlaybackStrategy} da applicare.
     */
    public void setStrategy(PlaybackStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * @brief Restituisce la strategia di navigazione attualmente impostata.
     * @return La {@link PlaybackStrategy} corrente.
     */
    public PlaybackStrategy getStrategy() {
        return this.strategy;
    }

    /**
     * @brief Cambia lo stato corrente del player (pattern State).
     * @param newState Il nuovo {@link PlayerState} da impostare.
     */
    public void changeState(PlayerState newState){
        this.currentState = newState;
    }


    /**
     * @brief Azzera il tempo di riproduzione corrente.
     */
    public void resetPlaybackTime() {
        this.currentTime = 0;
    }

    /**
     * @brief Carica una nuova coda di sorgenti riproducibili, ripartendo dall'inizio.
     *
     * Sostituisce la coda corrente, azzera gli indici di sorgente e traccia,
     * e imposta come traccia corrente la prima traccia della prima sorgente
     * (se presente).
     * @param sources La lista di sorgenti da caricare in coda.
     */
    public void loadQueue(List<PlayableSource> sources) {
        this.queue = new ArrayList<>(sources);

        this.currentSourceIndex = 0;
        this.currentTrackIndex = 0;

        if (!this.queue.isEmpty() && !this.queue.get(0).getTracks().isEmpty()) {
            this.currentTrack = this.queue.get(0).getTracks().get(0);
        }
    }


    /**
     * @brief Restituisce la coda di sorgenti riproducibili.
     * @return La lista delle sorgenti attualmente in coda.
     */
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
            Track oldCurrentTrack = this.currentTrack;
            if (oldCurrentTrack != null) {
                List<Track> newTracks = this.queue.get(idx).getTracks();
                int newTrackIdx = newTracks.indexOf(oldCurrentTrack);
                if (newTrackIdx >= 0) {
                    this.currentTrack = newTracks.get(newTrackIdx);
                } else {
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

    /**
     * @brief Restituisce la traccia attualmente in riproduzione.
     * @return La traccia corrente, o null se non impostata.
     */
    public Track getCurrentTrack(){
        return this.currentTrack;
    }

    /**
     * @brief Restituisce la sorgente attualmente in riproduzione.
     * @return La sorgente corrente, o null se la coda è vuota o l'indice non è valido.
     */
    public PlayableSource getCurrentSource(){
        if (this.queue == null || this.queue.isEmpty() || this.currentSourceIndex >= this.queue.size() || this.currentSourceIndex < 0) {
            return null;
        }
        return this.queue.get(this.currentSourceIndex);
    }

    /**
     * @brief Imposta la traccia attualmente in riproduzione.
     * @param track La traccia da impostare come corrente.
     */
    public void setCurrentTrack(Track track){
        this.currentTrack = track;
    }

    /**
     * @brief Avanza alla sorgente successiva nella coda.
     *
     * Incrementa l'indice di sorgente e azzera l'indice di traccia.
     * @return La nuova sorgente corrente, oppure null se non ci sono più sorgenti in coda.
     */
    public PlayableSource nextSource(){
        this.currentSourceIndex++;
        this.currentTrackIndex = 0;

        if (this.currentSourceIndex < this.queue.size()) {
            return this.queue.get(this.currentSourceIndex);
        }


        return null;
    }

    /**
     * @brief Metti in pausa la riproduzione, delegando allo stato corrente.
     */
    public void pause(){
        this.currentState.pause(this);
    }

    /**
     * @brief Interrompe la riproduzione, delegando allo stato corrente.
     */
    public void stop(){
        this.currentState.stop(this);
    }

    /**
     * @brief Avvia/riprende la riproduzione, delegando allo stato corrente.
     */
    public void play() {
        this.currentState.play(this);
    }

    /**
     * @brief Indica se la riproduzione è attualmente in corso.
     * @return true se lo stato corrente è in riproduzione, false altrimenti.
     */
    public boolean isPlaying(){
        return this.currentState.isPlaying();
    }


    /**
     * @brief Aggiunge una sorgente in coda alla lista di riproduzione.
     * @param source La sorgente da aggiungere.
     */
    public void addToQueue(PlayableSource source){
        this.queue.add(source);
    }


    /**
     * @brief Rimuove una sorgente dalla coda di riproduzione.
     * @param source La sorgente da rimuovere.
     */
    public void removeFromQueue(PlayableSource source){
        this.queue.remove(source);
    }

    /**
     * @brief Indica se la riproduzione è ferma.
     * @return true se lo stato corrente è stopped, false altrimenti.
     */
    public boolean isStopped() {
        return this.currentState.isStopped();
    }

    /**
     * @brief Indica se la riproduzione è in pausa.
     * @return true se lo stato corrente è paused, false altrimenti.
     */
    public boolean isPaused() {
        return this.currentState.isPaused();
    }

    /**
     * @brief Imposta la sorgente corrente individuandola nella coda.
     *
     * Aggiorna l'indice di sorgente corrente in base alla posizione della
     * sorgente fornita nella coda, e imposta come traccia corrente la prima
     * traccia della sorgente (se presente).
     * @param source La sorgente da impostare come corrente.
     */
    public void setCurrentSource(PlayableSource source) {
        this.currentSourceIndex = this.queue.indexOf(source);
        if (!source.getTracks().isEmpty()) {
            this.currentTrack = source.getTracks().get(0);
        }
    }

    /**
     * @brief Restituisce lo stato corrente del player.
     * @return Il {@link PlayerState} attualmente attivo.
     */
    public PlayerState getStatus() {
        return this.currentState;
    }
}