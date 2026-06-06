package com.example.gruppo04.controller;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.PlaybackStrategy;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.SequentialStrategy;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogEventType;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.model.state.PlaybackState;
import com.example.gruppo04.observer.ConcreteMusicCatalog;

import java.util.List;

/**
 * Controller responsabile della gestione della riproduzione musicale.
 * <p>
 * Agisce come Client dei pattern State e Strategy:
 * delega la gestione dello stato a {@link PlaybackState}
 * e la selezione della traccia successiva a {@link PlaybackStrategy}.
 * </p>
 */
public class PlaybackController implements CatalogObserver {

    private PlaybackState state;
    private PlaybackStrategy strategy;
    private MusicCatalog catalog;

    /**
     * Costruisce un {@code PlaybackController} con lo stato di riproduzione fornito.
     *
     * @param state lo stato di riproduzione da utilizzare; non deve essere {@code null}
     */
    public PlaybackController(PlaybackState state) {
        this.state = state;
        this.strategy = new SequentialStrategy(); // default
        this.catalog = ConcreteMusicCatalog.getInstance();
        catalog.registerObserver(this);
    }

    /**
     * Avvia la riproduzione della coda fornita a partire dalla sorgente
     * e dalla traccia specificate, secondo la strategia attualmente selezionata.
     * <p>
     * Sovrascrive la coda corrente e imposta lo stato a PLAYING.
     * </p>
     *
     * @param queue      la lista ordinata di sorgenti da riprodurre; non deve essere {@code null}
     * @param startFrom  la sorgente da cui partire nella coda; non deve essere {@code null}
     * @param startTrack la traccia da cui partire nella sorgente; se {@code null} parte dalla prima
     */
    public void play(List<PlayableSource> queue, PlayableSource startFrom, Track startTrack) {
        state.loadQueue(queue);
        state.setCurrentSource(startFrom);
        if (startTrack != null) {
            state.setCurrentTrack(startTrack);
        }
        state.play();
    }
    /**
     * Mette in pausa la riproduzione corrente.
     * <p>
     * Non ha effetto se la riproduzione non è in corso.
     * </p>
     */
    public void pause() {
        if (state.isPlaying()) {
            state.pause();
        }
    }
    /**
     * Salta la traccia corrente e avanza alla traccia successiva
     * all'interno della sorgente corrente secondo la strategia attiva.
     * <p>
     * Se non esiste una traccia successiva nella sorgente corrente,
     * delega a {@link #skipSource()}.
     * </p>
     */
    public void skipTrack() {
        List<Track> tracks = state.getCurrentSource().getTracks();
        int currentIndex = tracks.indexOf(state.getCurrentTrack());
        if (currentIndex + 1 < tracks.size()) {
            state.setCurrentTrack(tracks.get(currentIndex + 1));
        } else {
            skipSource();
        }
    }

    /**
     * Salta l'intera sorgente corrente e avanza alla sorgente successiva nella coda.
     * <p>
     * Se non esiste una sorgente successiva, ferma la riproduzione.
     * </p>
     */
    public void skipSource() {
        List<PlayableSource> queue = state.getQueue();
        int currentIndex = queue.indexOf(state.getCurrentSource());
        PlayableSource nextSource = strategy.nextSource(queue, currentIndex);
        if (nextSource != null) {
            state.setCurrentSource(nextSource);
            state.setCurrentTrack(nextSource.getTracks().get(0));
        } else {
            state.stop();
        }
    }

    /**
     * Aggiunge una sorgente in fondo alla coda di riproduzione corrente.
     * <p>
     * A differenza di , non interrompe
     * la riproduzione in corso — la sorgente verrà riprodotta al termine di quelle
     * già presenti in coda.
     * </p>
     *
     * @param source la sorgente da aggiungere in coda; non deve essere {@code null}
     */
    public void addToQueue(PlayableSource source) {
        state.addToQueue(source);
    }

    /**
     * Gestisce le notifiche di modifica del catalogo musicale.
     * <p>
     * Reagisce alla rimozione di tracce e playlist aggiornando la coda
     * di riproduzione di conseguenza:
     * </p>
     * <ul>
     *   <li>Se l'elemento rimosso è quello in riproduzione, avanza
     *       automaticamente al successivo.</li>
     *   <li>Se l'elemento rimosso è in coda ma non ancora riprodotto,
     *       viene rimosso dalla coda senza interrompere la riproduzione.</li>
     * </ul>
     *
     * @param event l'evento di modifica del catalogo; non deve essere {@code null}
     */
    @Override
    public void onCatalogChanged(CatalogEvent event) {
        if (event.getType() == CatalogEventType.TRACK_REMOVED) {
            Track removed = (Track) event.getTarget();
            if (removed.equals(state.getCurrentTrack())) {
                skipTrack();
            } else if (state.getQueue().contains(removed)) {
                state.removeFromQueue(removed);
            }
        } else if (event.getType() == CatalogEventType.PLAYLIST_REMOVED) {
            PlayableSource removed = (PlayableSource) event.getTarget();
            if (removed.equals(state.getCurrentSource())) {
                skipSource();
            } else if (state.getQueue().contains(removed)) {
                state.removeFromQueue(removed);
            }
        }
    }

    /**
     * Aggiorna la modalità di riproduzione attiva e notifica la UI
     * tramite il pattern Observer.
     * <p>
     * Il cambio di modalità non interrompe la riproduzione in corso ---
     * la nuova strategia verrà applicata a partire dalla traccia successiva.
     * </p>
     *
     * @param newStrategy la nuova strategia di riproduzione da applicare;
     *                    non deve essere {@code null}
     */
    public void changeStrategy(PlaybackStrategy newStrategy) {
        this.strategy = newStrategy;
        catalog.notifyStrategyChanged(newStrategy);
    }

    /**
     * @brief Verifica se la riproduzione è ferma.
     * @details Restituisce {@code true} se lo stato corrente è {@code STOPPED}.
     *
     * @return {@code true} se la riproduzione è ferma, {@code false} altrimenti
     */
    public boolean isStopped() {
        return state.isStopped();
    }

    /**
     * @brief Restituisce la traccia attualmente in riproduzione.
     * @details Delega a {@link PlaybackState#getCurrentTrack()}.
     *
     * @return la traccia corrente, o {@code null} se nessuna traccia è in riproduzione
     */
    public Track getCurrentTrack() {
        return state.getCurrentTrack();
    }

    /**
     * @brief Riprende la riproduzione dalla pausa.
     * @details Non ha effetto se la riproduzione è già in corso.
     * Non ricarica la coda ma riprende dal punto in cui era stata messa in pausa.
     */
    public void resume() {
        if (!state.isPlaying()) {
            state.play();
        }
    }
}