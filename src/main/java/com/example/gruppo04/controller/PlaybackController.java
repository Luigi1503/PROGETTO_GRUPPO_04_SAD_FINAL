package com.example.gruppo04.controller;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.model.strategy.PlaybackStrategy;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.strategy.SequentialStrategy;
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
     * e dalla traccia specificata, secondo la strategia attualmente selezionata.
     * <p>
     * Sovrascrive la coda corrente e imposta lo stato a PLAYING.
     * </p>
     *
     * @param queue      la lista ordinata di sorgenti da riprodurre; non deve essere {@code null}
     * @param startFrom  la sorgente da cui partire nella coda; non deve essere {@code null}
     * @param startTrack la traccia da cui partire nella sorgente; se {@code null} parte dalla prima
     */
    public void play(List<PlayableSource> queue, PlayableSource startFrom, Track startTrack) {
        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackController.play] === AVVIO RIPRODUZIONE ===");
        System.out.println("[PlaybackController.play] Dimensione coda: " + queue.size());
        for (int i = 0; i < queue.size(); i++) {
            PlayableSource src = queue.get(i);
            System.out.println("[PlaybackController.play]   [" + i + "] source: "
                    + src.getDisplayName()
                    + " | tracce: " + src.getTracks().size());
        }
        System.out.println("[PlaybackController.play] startFrom: " + startFrom.getDisplayName());
        System.out.println("[PlaybackController.play] startTrack: "
                + (startTrack != null ? startTrack.getTitle() : "null (prima della sorgente)"));
        System.out.println("[PlaybackController.play] strategia attiva: "
                + strategy.getClass().getSimpleName());
        // ──────────────────────────────────────────────────────────────────────

        state.loadQueue(queue);
        state.setCurrentSource(startFrom);
        if (startTrack != null) {
            state.setCurrentTrack(startTrack);
        }
        state.play();

        // Notifica la barra di riproduzione che è iniziata una nuova riproduzione.
        // isPlaylist = true se startFrom è una Playlist, false se è una traccia singola del catalogo.
        boolean isPlaylist = (startFrom instanceof Playlist);
        String playlistName = null;
        if (isPlaylist) {
            playlistName = ((Playlist) startFrom).getName();
        }
        catalog.notifyPlaybackStarted(state.getCurrentTrack(), isPlaylist, playlistName);
        System.out.println("[PlaybackController.play] notifyPlaybackStarted inviato | isPlaylist=" + isPlaylist);

        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackController.play] Dopo state.play():");
        System.out.println("[PlaybackController.play]   isPlaying=" + state.isPlaying()
                + " | isStopped=" + state.isStopped());
        System.out.println("[PlaybackController.play]   currentTrack="
                + (state.getCurrentTrack() != null ? state.getCurrentTrack().getTitle() : "null"));
        System.out.println("[PlaybackController.play]   currentSource="
                + (state.getCurrentSource() != null
                ? state.getCurrentSource().getDisplayName() : "null"));
        // ──────────────────────────────────────────────────────────────────────
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
        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackController.skipTrack] === SKIP TRACCIA ===");
        System.out.println("[PlaybackController.skipTrack] currentSource: "
                + (state.getCurrentSource() != null
                ? state.getCurrentSource().getDisplayName() : "null"));
        System.out.println("[PlaybackController.skipTrack] currentTrack: "
                + (state.getCurrentTrack() != null
                ? state.getCurrentTrack().getTitle() : "null"));
        // ──────────────────────────────────────────────────────────────────────

        List<Track> tracks = state.getCurrentSource().getTracks();
        int currentIndex = tracks.indexOf(state.getCurrentTrack());

        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackController.skipTrack] indice traccia corrente: " + currentIndex
                + " | totale tracce nella sorgente: " + tracks.size()
                + " | strategia: " + strategy.getClass().getSimpleName());
        // ──────────────────────────────────────────────────────────────────────

        // Delega alla strategia attiva la selezione della traccia successiva.
        // null significa che la sorgente è terminata secondo la strategia corrente
        // e si deve passare alla sorgente successiva.
        Track next = strategy.nextTrack(tracks, currentIndex);
        if (next != null) {
            state.setCurrentTrack(next);
            System.out.println("[PlaybackController.skipTrack] → traccia successiva (strategia): " + next.getTitle());
        } else {
            System.out.println("[PlaybackController.skipTrack] → fine sorgente secondo la strategia, delego a skipSource()");
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
        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackController.skipSource] === SKIP SORGENTE ===");
        List<PlayableSource> queue = state.getQueue();
        int currentIndex = queue.indexOf(state.getCurrentSource());
        System.out.println("[PlaybackController.skipSource] coda totale: " + queue.size()
                + " | indice corrente: " + currentIndex);
        System.out.println("[PlaybackController.skipSource] strategia: "
                + strategy.getClass().getSimpleName());
        // ──────────────────────────────────────────────────────────────────────

        PlayableSource nextSource = strategy.nextSource(queue, currentIndex);

        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackController.skipSource] nextSource restituito dalla strategia: "
                + (nextSource != null ? nextSource.getDisplayName() : "null → stop"));
        // ──────────────────────────────────────────────────────────────────────

        if (nextSource != null) {
            // verifica che la sorgente successiva abbia tracce
            if (nextSource.getTracks().isEmpty()) {
                // salta anche questa sorgente
                state.setCurrentSource(nextSource);
                skipSource();
                return;
            }
            state.setCurrentSource(nextSource);
            state.setCurrentTrack(nextSource.getTracks().get(0));
            System.out.println("[PlaybackController.skipSource] → prima traccia della nuova sorgente: "
                    + nextSource.getTracks().get(0).getTitle());
        } else {
            state.stop();
            System.out.println("[PlaybackController.skipSource] → riproduzione fermata (stop)");
        }
    }

    /**
     * Aggiunge una sorgente in fondo alla coda di riproduzione corrente.
     *
     * @param source la sorgente da aggiungere in coda; non deve essere {@code null}
     */
    public void addToQueue(PlayableSource source) {
        state.addToQueue(source);
    }

    /**
     * Gestisce le notifiche di modifica del catalogo musicale.
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
     * Torna alla traccia precedente nella sorgente corrente.
     */
    public void previousTrack() {
        List<Track> tracks = state.getCurrentSource().getTracks();
        int currentIndex = tracks.indexOf(state.getCurrentTrack());
        if (currentIndex > 0) {
            state.setCurrentTrack(tracks.get(currentIndex - 1));
        } else {
            state.setCurrentTrack(tracks.get(0));
        }
    }

    /**
     * Aggiorna la modalità di riproduzione attiva.
     *
     * @param newStrategy la nuova strategia di riproduzione da applicare;
     *                    non deve essere {@code null}
     */
    public void changeStrategy(PlaybackStrategy newStrategy) {
        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackController.changeStrategy] nuova strategia: "
                + newStrategy.getClass().getSimpleName());
        // ──────────────────────────────────────────────────────────────────────
        this.strategy = newStrategy;
        catalog.notifyStrategyChanged(newStrategy);
    }

    /**
     * @return {@code true} se la riproduzione è ferma
     */
    public boolean isStopped() {
        return state.isStopped();
    }

    /**
     * @return la traccia corrente, o {@code null} se nessuna traccia è in riproduzione
     */
    public Track getCurrentTrack() {
        return state.getCurrentTrack();
    }

    /**
     * Riprende la riproduzione dalla pausa.
     */
    public void resume() {
        if (!state.isPlaying()) {
            state.play();
        }
    }

    /**
     * @return il nome della sorgente corrente se è una playlist, null altrimenti
     */
    public String getCurrentSourceName() {
        PlayableSource source = state.getCurrentSource();
        if (source instanceof Playlist) {
            return ((Playlist) source).getName();
        }
        return null;
    }
}