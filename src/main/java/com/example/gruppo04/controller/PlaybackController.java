package com.example.gruppo04.controller;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.model.strategy.PlaybackStrategy;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.factory_method.AutomaticPlaylistService;
import com.example.gruppo04.model.strategy.SequentialStrategy;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogEventType;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.model.state.PlaybackState;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import com.example.gruppo04.model.AudioEngine;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

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
    private MusicCatalog catalog;
    private AudioEngine audioEngine;

    /**
     * Storico di riproduzione effettivo (coppie sorgente+traccia), usato dalle
     * strategie che basano il "torna indietro" (⏮) sullo storico reale anziché
     * sull'ordine — vedi {@link PlaybackStrategy#usesHistoryForPrevious()}.
     * Viene alimentato a ogni avanzamento e azzerato all'avvio di una nuova coda.
     */
    private final Deque<HistoryEntry> playbackHistory = new ArrayDeque<>();

    /** Voce dello storico di riproduzione: la sorgente e la traccia riprodotte. */
    private record HistoryEntry(PlayableSource source, Track track) {
    }

    /**
     * Costruisce un {@code PlaybackController} con lo stato di riproduzione fornito.
     *
     * @param state lo stato di riproduzione da utilizzare; non deve essere {@code null}
     */
    public PlaybackController(PlaybackState state) {
        this.state = state;
        this.state.setStrategy(new SequentialStrategy()); // default
        this.catalog = ConcreteMusicCatalog.getInstance();
        catalog.registerObserver(this);
        this.audioEngine = new AudioEngine();
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
                + state.getStrategy().getClass().getSimpleName());
        // ──────────────────────────────────────────────────────────────────────

        // Nuova coda → lo storico di riproduzione precedente non è più rilevante.
        playbackHistory.clear();

        state.loadQueue(queue);
        state.setCurrentSource(startFrom);
        if (startTrack != null) {
            state.setCurrentTrack(startTrack);
        }
        state.play();
        incrementPlayCounters(startFrom);
        avviaAudioFisico();

        // Notifica la barra di riproduzione che è iniziata una nuova riproduzione.
        // isPlaylist = true se startFrom è una Playlist, false se è una traccia singola del catalogo.
        boolean isPlaylist = (startFrom instanceof Playlist);

        catalog.notifyPlaybackStarted(state.getCurrentTrack(), isPlaylist, startFrom);
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

    private void incrementPlayCounters(PlayableSource startFrom) {
        Track currentTrack = state.getCurrentTrack();
        if (currentTrack != null) {
            currentTrack.incrementPlayCount();
        }
        if (startFrom instanceof Playlist playlist) {
            playlist.incrementPlayCount();
        }
    }

    private void avviaAudioFisico() {
        Track currentTrack = state.getCurrentTrack();
        System.out.println("[PlaybackController.avviaAudioFisico] richiesto avvio audio per: "
                + (currentTrack != null ? currentTrack.getTitle() : "null"));
        if (currentTrack != null) {
            String pathFileMp3 = currentTrack.getFilePath();

            // === CONTROLLO DI SICUREZZA ===
            if (pathFileMp3 == null) {
                // Nessun file audio associato: non si avvia l'audio fisico, ma la
                // riproduzione logica (stato, traccia/sorgente corrente) resta valida.
                // NON si delega a skipTrack(): in Loop/Shuffle nextTrack non termina
                // mai la sorgente, quindi un avanzamento automatico qui genererebbe
                // ricorsione infinita (StackOverflow).
                System.err.println("[PlaybackController] ATTENZIONE: La traccia '"
                        + currentTrack.getTitle() + "' non ha un percorso MP3 valido (è null)!");
                return;
            }

            audioEngine.playTrack(pathFileMp3, () -> {
                System.out.println("[PlaybackController] Canzone terminata naturalmente. Passo alla prossima!");
                this.skipTrack();
            });
        }
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
            audioEngine.pause();
        }
    }

    /**
     * Interrompe la riproduzione corrente.
     * <p>
     * Non ha effetto se la riproduzione non è in corso.
     * </p>
     */
    public void stop() {
        state.stop();
        audioEngine.stop();
        // Notifica le viste così che azzerino l'evidenziazione della traccia.
        catalog.notifyPlaybackStopped();
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

        // Registra il brano corrente nello storico prima di avanzare, così che
        // il "torna indietro" possa riportare esattamente qui.
        recordHistory();

        List<Track> tracks = state.getCurrentSource().getTracks();
        int currentIndex = tracks.indexOf(state.getCurrentTrack());

        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackController.skipTrack] indice traccia corrente: " + currentIndex
                + " | totale tracce nella sorgente: " + tracks.size()
                + " | strategia: " + state.getStrategy().getClass().getSimpleName());
        // ──────────────────────────────────────────────────────────────────────

        // Delega alla strategia attiva la selezione della traccia successiva.
        // null significa che la sorgente è terminata secondo la strategia corrente
        // e si deve passare alla sorgente successiva.
        Track next = state.getStrategy().nextTrack(tracks, currentIndex);
        if (next != null) {
            state.setCurrentTrack(next);
            incrementPlayCounters(next);
            state.play();
            avviaAudioFisico();
            System.out.println("[PlaybackController.skipTrack] → traccia successiva (strategia): " + next.getTitle());
            catalog.notifyTrackChanged(state.getCurrentTrack());
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
        // Registra il brano corrente nello storico prima di avanzare. Quando
        // skipSource è invocato da skipTrack la voce è già presente: recordHistory
        // de-duplica e non la inserisce due volte.
        recordHistory();

        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackController.skipSource] === SKIP SORGENTE ===");
        List<PlayableSource> queue = state.getQueue();
        int currentIndex = queue.indexOf(state.getCurrentSource());
        System.out.println("[PlaybackController.skipSource] coda totale: " + queue.size()
                + " | indice corrente: " + currentIndex);
        System.out.println("[PlaybackController.skipSource] strategia: "
                + state.getStrategy().getClass().getSimpleName());
        // ──────────────────────────────────────────────────────────────────────

        PlayableSource nextSource = state.getStrategy().nextSource(queue, currentIndex);

        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackController.skipSource] nextSource restituito dalla strategia: "
                + (nextSource != null ? nextSource.getDisplayName() : "null → stop"));
        // ──────────────────────────────────────────────────────────────────────

        if (nextSource != null) {
            // verifica che la sorgente successiva abbia tracce
            if (nextSource.getTracks().isEmpty()) {
                // salta anche questa sorgente
                state.setCurrentSource(nextSource);
                incrementPlayCounters(nextSource);
                skipSource();
                return;
            }
                state.setCurrentSource(nextSource);
                state.setCurrentTrack(nextSource.getTracks().get(0));
                incrementPlayCounters(nextSource);
            System.out.println("[PlaybackController.skipSource] → prima traccia della nuova sorgente: "
                    + nextSource.getTracks().get(0).getTitle());
            state.play();
            avviaAudioFisico();
            catalog.notifyTrackChanged(state.getCurrentTrack());
            catalog.notifySourceChanged(nextSource);
        } else {
            stop();
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
                System.out.println("[PlaybackController] Traccia in riproduzione rimossa dal catalogo. Stop.");
                this.stop();
            } else if (state.getQueue().contains(removed)) {
                state.removeFromQueue(removed);
            }
        }
        else if (event.getType() == CatalogEventType.PLAYLIST_TRACK_REMOVED) {
            Track removed = (Track) event.getTarget();
            if (removed.equals(state.getCurrentTrack())) {
                System.out.println("[PlaybackController] Traccia in riproduzione rimossa dalla playlist. Stop.");
                this.stop();
            } else {
                // La playlist modificata potrebbe essere più avanti nella coda:
                // riallinea la coda al contenuto aggiornato del catalogo.
                refreshQueueFromCatalog();
            }
        }
        else if (event.getType() == CatalogEventType.PLAYLIST_REMOVED) {
            PlayableSource removed = (PlayableSource) event.getTarget();
            if (removed.equals(state.getCurrentSource())) {
                System.out.println("[PlaybackController] La playlist in riproduzione è stata eliminata. Stop.");
                this.stop();
            } else {
                refreshQueueFromCatalog();
            }
        }
        else if (event.getType() == CatalogEventType.PLAYLIST_TRACK_ADDED
                || event.getType() == CatalogEventType.PLAYLIST_CONTENT_CHANGED
                || event.getType() == CatalogEventType.PLAYLIST_REORDERED
                || event.getType() == CatalogEventType.PLAYLIST_RENAMED
                || event.getType() == CatalogEventType.PLAYLIST_ADDED) {
            // Una playlist è stata modificata/aggiunta: aggiorna la coda così che
            // lo skip alla playlist successiva rifletta lo stato corrente.
            refreshQueueFromCatalog();
        }
    }

    /**
     * Riallinea la coda di riproduzione alle playlist attualmente presenti nel
     * catalogo, preservando la sorgente e la traccia in riproduzione.
     * <p>
     * Agisce solo quando la riproduzione corrente è basata su playlist: durante
     * la riproduzione di tracce singole del catalogo la coda non è composta da
     * playlist e non va sovrascritta.
     * </p>
     */
    private void refreshQueueFromCatalog() {
        if (state.getCurrentSource() instanceof Playlist playlist) {
            state.refreshQueue(buildContinuationQueue(playlist));
        }
    }

    /**
     * Avvia la riproduzione di una playlist, costruendo automaticamente la coda
     * di continuazione corretta: le altre playlist manuali del catalogo se
     * {@code playlist} è manuale, le playlist automatiche se è generata.
     *
     * @param playlist   la playlist da riprodurre; non deve essere {@code null}
     * @param startTrack la traccia da cui partire; se {@code null} parte dalla prima
     */
    public void playPlaylist(Playlist playlist, Track startTrack) {
        play(buildContinuationQueue(playlist), playlist, startTrack);
    }

    /**
     * Costruisce la coda di continuazione per la playlist data: le playlist
     * manuali del catalogo se {@code playlist} ne fa parte, altrimenti le
     * playlist generate automaticamente.
     */
    private List<PlayableSource> buildContinuationQueue(Playlist playlist) {
        List<PlayableSource> queue = new ArrayList<>();
        if (catalog.getPlaylists().contains(playlist)) {
            queue.addAll(catalog.getPlaylists());
        } else {
            queue.addAll(AutomaticPlaylistService.getInstance().refresh(catalog));
        }
        return queue;
    }
    /**
     * Riavvia la traccia corrente dall'inizio, senza cambiare traccia né sorgente.
     */
    public void restartTrack() {
        state.play();
        avviaAudioFisico();
        catalog.notifyTrackChanged(state.getCurrentTrack());
    }

    /**
     * Torna alla traccia precedente secondo la strategia attiva.
     * <p>
     * Per le strategie che usano lo storico reale ({@link
     * PlaybackStrategy#usesHistoryForPrevious()}, es. Shuffle) torna al brano che
     * ha effettivamente preceduto quello corrente — anche se appartiene a un'altra
     * sorgente, come nel catalogo — oppure riavvia il brano corrente se lo storico
     * è vuoto. Per le strategie deterministiche delega invece alla strategia
     * (simmetrico a {@link #skipTrack()} → {@link PlaybackStrategy#nextTrack});
     * un valore {@code null} indica l'inizio della sorgente e si passa alla
     * sorgente precedente riprendendone l'ultima traccia (sequenza inversa continua).
     * Rispetta il pannello Help: in ordine (Sequential), brano precedentemente
     * riprodotto (Shuffle), ciclica/stessa traccia (Loop).
     * </p>
     */
    public void previousTrack() {
        if (state.getStrategy().usesHistoryForPrevious()) {
            playPreviousFromHistory();
            return;
        }

        List<Track> tracks = state.getCurrentSource().getTracks();
        int currentIndex = tracks.indexOf(state.getCurrentTrack());

        Track previous = state.getStrategy().previousTrack(tracks, currentIndex);
        if (previous != null) {
            state.setCurrentTrack(previous);
            incrementPlayCounters(previous);
            state.play();
            avviaAudioFisico();
            catalog.notifyTrackChanged(state.getCurrentTrack());
        } else {
            // Inizio sorgente secondo la strategia: torna alla sorgente precedente
            // riprendendone l'ultima traccia (sequenza inversa continua).
            goToPreviousSource(true);
        }
    }

    /**
     * Registra la sorgente e la traccia correnti in cima allo storico di
     * riproduzione, evitando voci duplicate consecutive.
     */
    private void recordHistory() {
        PlayableSource source = state.getCurrentSource();
        Track track = state.getCurrentTrack();
        if (source == null || track == null) {
            return;
        }
        HistoryEntry top = playbackHistory.peek();
        if (top != null && Objects.equals(top.source(), source) && Objects.equals(top.track(), track)) {
            return;
        }
        playbackHistory.push(new HistoryEntry(source, track));
    }

    /**
     * Riproduce il brano precedentemente riprodotto preso dallo storico reale,
     * spostandosi anche su un'altra sorgente se necessario (es. nel catalogo dove
     * ogni traccia è una sorgente a sé). Le voci non più valide (sorgente assente
     * dalla coda o traccia rimossa) vengono scartate. Se lo storico è vuoto,
     * riavvia il brano corrente da capo.
     */
    private void playPreviousFromHistory() {
        while (!playbackHistory.isEmpty()) {
            HistoryEntry entry = playbackHistory.pop();
            if (state.getQueue().contains(entry.source())
                    && entry.source().getTracks().contains(entry.track())) {
                state.setCurrentSource(entry.source());
                state.setCurrentTrack(entry.track());
                incrementPlayCounters(entry.source());
                avviaAudioFisico();
                catalog.notifyTrackChanged(state.getCurrentTrack());
                if (entry.source() instanceof Playlist playlist) {
                    catalog.notifySourceChanged(playlist);
                }
                return;
            }
        }
        // Nessun brano precedente nello storico → riavvia la traccia corrente.
        avviaAudioFisico();
        catalog.notifyTrackChanged(state.getCurrentTrack());
    }

    /**
     * Torna alla sorgente precedente nella coda secondo la strategia attiva
     * (pulsante "playlist precedente"), facendone ripartire la prima traccia.
     * <p>
     * Simmetrico a {@link #skipSource()}: la scelta della sorgente è delegata alla
     * strategia ({@link PlaybackStrategy#previousSource}) e rispetta il pannello
     * Help — playlist precedente (Sequential), playlist casuale (Shuffle), playlist
     * corrente che riparte da capo (Loop).
     * </p>
     */
    public void previousSource() {
        goToPreviousSource(false);
    }

    /**
     * Sposta la riproduzione alla sorgente precedente scelta dalla strategia.
     *
     * @param playLastTrack se {@code true} riprende l'ultima traccia della sorgente
     *        precedente (usato dal "torna alla traccia precedente" quando attraversa
     *        i confini della sorgente); se {@code false} riparte dalla prima traccia
     *        (usato dal pulsante "playlist precedente", simmetrico a {@link #skipSource()}).
     */
    private void goToPreviousSource(boolean playLastTrack) {
        List<PlayableSource> queue = state.getQueue();
        int currentIndex = queue.indexOf(state.getCurrentSource());

        PlayableSource prevSource = state.getStrategy().previousSource(queue, currentIndex);

        if (prevSource == null) {
            // Nessuna sorgente precedente: riavvia la traccia corrente da capo.
            avviaAudioFisico();
            catalog.notifyTrackChanged(state.getCurrentTrack());
            return;
        }

        // Salta eventuali sorgenti precedenti vuote (simmetrico a skipSource()).
        if (prevSource.getTracks().isEmpty()) {
            state.setCurrentSource(prevSource);
            goToPreviousSource(playLastTrack);
            return;
        }

        state.setCurrentSource(prevSource);
        List<Track> prevTracks = prevSource.getTracks();
        Track target = playLastTrack
                ? prevTracks.get(prevTracks.size() - 1)
                : prevTracks.get(0);
        state.setCurrentTrack(target);
        incrementPlayCounters(prevSource);
        avviaAudioFisico();

        catalog.notifyTrackChanged(state.getCurrentTrack());
        if (prevSource instanceof Playlist playlist) {
            catalog.notifySourceChanged(playlist);
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
        this.state.setStrategy(newStrategy);
        catalog.notifyStrategyChanged(newStrategy);
    }

    /**
     * @return {@code true} se la riproduzione è ferma
     */
    public boolean isStopped() {
        return state.isStopped();
    }

    /**
     * @return {@code true} se la riproduzione è in corso
     */
    public boolean isPlaying() {
        return state.isPlaying();
    }

    /**
     * @return {@code true} se la riproduzione è in pausa (ancora attiva, non ferma)
     */
    public boolean isPaused() {
        return state.isPaused();
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
            audioEngine.resume();
        }
    }

    /**
     * @return la sorgente corrente 
     */
    public PlayableSource getCurrentSource() {
        return state.getCurrentSource();
    }
    public double getCurrentAudioTime() {
        return audioEngine.getCurrentTimeInSeconds();
    }

    /**
     * Sposta la riproduzione della traccia corrente al tempo indicato.
     * <p>
     * Non ha effetto se nessuna traccia è in riproduzione. La nuova posizione
     * si riflette immediatamente sull'audio (o al successivo resume se in pausa).
     * </p>
     *
     * @param targetSeconds posizione desiderata in secondi dall'inizio della traccia
     */
    public void seek(double targetSeconds) {
        Track current = state.getCurrentTrack();
        if (current == null) {
            return;
        }
        audioEngine.seek(targetSeconds, current.getDuration());
    }

    /**
     * @return il nome semplice della strategia di riproduzione attiva
     *         (es. "SequentialStrategy", "ShuffleStrategy", "LoopStrategy")
     */
    /**
     * @return la strategia di riproduzione attualmente attiva
     */
    public PlaybackStrategy getCurrentStrategy() {
        return state.getStrategy();
    }
}
