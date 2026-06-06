package com.example.gruppo04.controller;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.PlaybackStrategy;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.state.PlaybackState;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogEventType;
import com.example.gruppo04.observer.CatalogObserver;
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
        this.catalog = ConcreteMusicCatalog.getInstance();
        catalog.registerObserver(this);
    }

    /**
     * Avvia la riproduzione della sorgente fornita secondo la strategia specificata.
     * <p>
     * Carica la sorgente come coda e imposta lo stato a PLAYING.
     * </p>
     *
     * @param source   la sorgente da riprodurre (traccia singola o playlist)
     * @param strategy la strategia di riproduzione da applicare
     */
    public void play(PlayableSource source, PlaybackStrategy strategy) {
        this.strategy = strategy;
        state.loadQueue(List.of(source));
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
        Track track = strategy.nextTrack(state.getCurrentSource().getTracks(), state.getCurrentTrack());
        if (track != null) {
            state.setCurrentTrack(track);
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
        PlayableSource nextSource = state.nextSource();
        if (nextSource != null) {
            PlayableSource first = strategy.nextSource(nextSource.getTracks(), null);
            state.setCurrentTrack(first);
        } else {
            state.stop();
        }
    }

    /**
     * Aggiunge una sorgente in fondo alla coda di riproduzione corrente.
     * <p>
     * A differenza di {@link #play(PlayableSource, PlaybackStrategy)}, non interrompe
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
}