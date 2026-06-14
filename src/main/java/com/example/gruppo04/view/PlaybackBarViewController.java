package com.example.gruppo04.view;


import com.example.gruppo04.observer.PlaybackStartedPayload;
import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.*;
import com.example.gruppo04.model.strategy.LoopStrategy;
import com.example.gruppo04.model.strategy.PlaybackStrategy;
import com.example.gruppo04.model.strategy.SequentialStrategy;
import com.example.gruppo04.model.strategy.ShuffleStrategy;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.util.TrackFormatter;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;


/**
 * @brief Controller JavaFX della barra di riproduzione.
 * @details Mostra le informazioni sulla traccia corrente e permette
 * di controllare la riproduzione tramite {@link PlaybackController}.
 * Implementa {@link CatalogObserver} per aggiornarsi automaticamente
 * quando cambia la traccia in riproduzione.
 */
public class PlaybackBarViewController implements CatalogObserver {

    /** @brief Label che mostra il titolo della traccia corrente. */
    @FXML private Label labelTrackTitle;

    /** @brief Label che mostra l'artista della traccia corrente. */
    @FXML private Label labelTrackArtist;

    /** @brief Label che mostra l'anno della traccia corrente. */
    @FXML private Label labelTrackYear;

    /** @brief Label che mostra il genere della traccia corrente. */
    @FXML private Label labelTrackGenre;

    /** @brief Bottone play/pausa. */
    @FXML private Button btnPlayPause;

    /** @brief Bottone brano precedente. */
    @FXML private Button btnPrevious;

    /** @brief Bottone brano successivo. */
    @FXML private Button btnSkip;

    /** @brief Bottone stop riproduzione. */
    @FXML private Button btnStop;

    /**
     * @brief Bottone skip playlist successiva.
     * @details Visibile solo durante la riproduzione di una playlist.
     */
    @FXML private Button btnSkipPlaylist;

    /**
     * @brief Bottone playlist precedente.
     * @details Simmetrico a {@link #btnSkipPlaylist}: visibile solo durante la
     * riproduzione di una playlist. Torna alla sorgente precedente nella coda.
     */
    @FXML private Button btnPreviousPlaylist;

    /** @brief Barra di avanzamento della traccia corrente. */
    @FXML private Slider progressBar;

    /** @brief Label che mostra il tempo corrente di riproduzione. */
    @FXML private Label labelCurrentTime;

    /** @brief Label che mostra la durata totale della traccia corrente. */
    @FXML private Label labelTotalTime;

    /** @brief Button che definisce la modalità di riproduzione attualmente selezionata - Sequenziale*/
    @FXML private Button btnSequential;

    /** @brief Button che definisce la modalità di riproduzione attualmente selezionata - Shuffle*/
    @FXML private Button btnShuffle;

    /** @brief Button che definisce la modalità di riproduzione attualmente selezionata - Loop*/
    @FXML private Button btnLoop;

    /** @brief Label che mostra il nome della playlist in riproduzione. */
    @FXML private Label labelPlaylistName;

    /** @brief Controller MVC della riproduzione. */
    private PlaybackController playbackController;

    /** @brief Traccia attualmente in riproduzione. */
    private Track currentTrack;

    /** @brief Timer per l'aggiornamento in tempo reale della barra di avanzamento. */
    private AnimationTimer progressTimer;

    /**
     * @brief Indica che l'utente sta interagendo con la barra (drag/click).
     * @details Quando true il timer non sovrascrive il valore dello slider, così
     * il trascinamento dell'utente non viene "combattuto" dall'aggiornamento automatico.
     */
    private boolean userSeeking = false;


    /**
     * @brief Costruttore senza parametri richiesto da FXMLLoader.
     */
    public PlaybackBarViewController() {
    }

    /**
     * @brief Inizializza la barra di riproduzione con le dipendenze necessarie.
     * @details Registra questo controller come Observer del catalogo e
     * configura il timer per l'aggiornamento in tempo reale.
     * Da chiamare dopo il caricamento dell'FXML.
     *
     * @param playbackController il controller MVC della riproduzione
     * @param catalog            il catalogo musicale
     */
    public void init(PlaybackController playbackController, MusicCatalog catalog) {
        this.playbackController = playbackController;
        btnPlayPause.setDisable(true);
        btnPrevious.setDisable(true);
        btnSkip.setDisable(true);
        btnStop.setDisable(true);
        catalog.registerObserver(this);
        setupProgressTimer();
        setupSeekControls();
    }

    /**
     * @brief Abilita il riposizionamento (seek) tramite la barra di avanzamento.
     * @details L'utente può trascinare o cliccare sulla barra per spostarsi nel
     * brano. Durante l'interazione viene mostrata in anteprima la nuova posizione
     * sulle label del tempo; al rilascio viene applicato il seek sull'audio reale.
     */
    private void setupSeekControls() {
        // Inizio interazione: blocca l'aggiornamento automatico del timer.
        progressBar.setOnMousePressed(e -> userSeeking = true);

        // Trascinamento: anteprima del minutaggio corrispondente alla posizione.
        progressBar.setOnMouseDragged(e -> updateTimeLabelsPreview());

        // Rilascio (sia per click che per fine trascinamento): applica il seek.
        progressBar.setOnMouseReleased(e -> {
            applySeekFromSlider();
            userSeeking = false;
        });
    }

    /**
     * @brief Aggiorna le label del tempo in base alla posizione corrente dello slider.
     * @details Usato come anteprima durante il trascinamento, senza toccare l'audio.
     */
    private void updateTimeLabelsPreview() {
        if (currentTrack == null) return;
        int total = currentTrack.getDuration();
        int target = (int) (progressBar.getValue() * total);
        labelCurrentTime.setText(TrackFormatter.formatDuration(target));
        labelTotalTime.setText("-" + TrackFormatter.formatDuration(total - target));
    }

    /**
     * @brief Applica il riposizionamento dell'audio in base al valore dello slider.
     * @details Converte la frazione (0..1) dello slider in secondi e delega al
     * {@link PlaybackController}, riflettendo lo spostamento sulla riproduzione reale.
     */
    private void applySeekFromSlider() {
        if (currentTrack == null) return;
        double target = progressBar.getValue() * currentTrack.getDuration();
        playbackController.seek(target);
        updateTimeLabelsPreview();
    }

    /**
     * @brief Chiamato automaticamente da FXMLLoader dopo il caricamento dell'FXML.
     * @details Inizializza lo stato iniziale della barra di riproduzione.
     * Di default è attivo il bottone di sequenzializzazione
     */
    @FXML
    void initialize() {
        progressBar.setValue(0);
        setSkipPlaylistVisible(false); // nascondi i bottoni di navigazione playlist
        setActiveMode(btnSequential);
    }

    /**
     * @brief Chiamato dal catalogo quando si verifica un cambiamento.
     * @details Aggiorna la barra con le informazioni della traccia corrente
     * in risposta agli eventi di modifica del catalogo.
     *
     * @param event l'evento contenente i dettagli del cambiamento
     */
    @Override
    public void onCatalogChanged(CatalogEvent event) {
        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackBarViewController.onCatalogChanged] evento ricevuto: " + event.getType());
        // ──────────────────────────────────────────────────────────────────────

        switch (event.getType()) {
            case TRACK_REMOVED, PLAYLIST_TRACK_REMOVED:
                Track removedTrack = (Track) event.getTarget();
                if (removedTrack.equals(currentTrack))
                    playbackController.stop();
                break;

            case PLAYBACK_STARTED:
                PlaybackStartedPayload payload = (PlaybackStartedPayload) event.getTarget();
                this.currentTrack = payload.getCurrentTrack();
                startPlayback(currentTrack);

                boolean isPlaylist = payload.getCurrentSource() instanceof Playlist;
                setSkipPlaylistVisible(isPlaylist);

                if (isPlaylist)
                    updatePlaylistName(((Playlist) payload.getCurrentSource()).getName());
                else
                    updatePlaylistName(null);
                break;

            case TRACK_CHANGED:
                Track newTrack = (Track) event.getTarget();
                this.currentTrack = newTrack;
                updateTrackInfo(newTrack);
                progressTimer.start();
                btnPlayPause.setText("⏸");
                break;

            case SOURCE_CHANGED:
                PlayableSource source =  (PlayableSource) event.getTarget();
                // La sorgente è una playlist solo durante la riproduzione di playlist;
                // dal catalogo è una traccia singola e non ha un nome di playlist.
                if (source instanceof Playlist playlist)
                    updatePlaylistName(playlist.getName());
                else
                    updatePlaylistName(null);
                break;


            case PLAYBACK_STOPPED:
                Platform.runLater(() -> {
                    resetLabels();
                    updatePlaylistName(null);
                    btnPlayPause.setDisable(true);
                    btnPrevious.setDisable(true);
                    btnSkip.setDisable(true);
                    btnStop.setDisable(true);
                    // nascondi i bottoni playlist solo se la riproduzione è davvero ferma
                    if (playbackController.isStopped()) {
                        setSkipPlaylistVisible(false);
                    }
                });
                break;

            case STRATEGY_CHANGED:
                PlaybackStrategy strategy = (PlaybackStrategy) event.getTarget();
                if (strategy instanceof SequentialStrategy) setActiveMode(btnSequential);
                else if (strategy instanceof ShuffleStrategy) setActiveMode(btnShuffle);
                else if (strategy instanceof LoopStrategy) setActiveMode(btnLoop);
                break;

            default:
                break;
        }
    }


    /**
     * @brief Aggiorna le label della barra con i dati della traccia fornita.
     * @details Da chiamare quando cambia la traccia in riproduzione.
     *
     * @param track la traccia da mostrare nella barra; null per resettare
     */
    public void updateTrackInfo(Track track) {
        this.currentTrack = track;
        if (track != null) {
            labelTrackTitle.setText(track.getTitle());
            labelTrackArtist.setText(track.getAuthor());
            labelTrackYear.setText(String.valueOf(track.getYear()));
            labelTrackGenre.setText(track.getGenre());
            labelTotalTime.setText(TrackFormatter.formatDuration(track.getDuration()));
            resetProgress();
        } else
            resetLabels();
    }


    /**
     * @brief Mostra o nasconde il bottone di skip playlist.
     *
     * @param visible true se si sta riproducendo una playlist, false altrimenti
     */
    public void setSkipPlaylistVisible(boolean visible) {
        btnSkipPlaylist.setVisible(visible);
        btnSkipPlaylist.setManaged(visible);
        btnPreviousPlaylist.setVisible(visible);
        btnPreviousPlaylist.setManaged(visible);
    }

    /**
     * @brief Gestisce il click su Play/Pausa.
     */
    @FXML
    void handlePlayPause(ActionEvent event) {
        if (playbackController.isPlaying()) {
            // mette in pausa e ferma il timer
            playbackController.pause();
            btnPlayPause.setText("▶");
            progressTimer.stop();
        } else {
            // riprende la riproduzione
            playbackController.resume();
            btnPlayPause.setText("⏸");
            progressTimer.start();
        }
    }

    /**
     * @brief Gestisce il click su Stop Traccia corrente.
     * @details Ferma immediatamente la traccia corrente in riproduzione, disabilita tutti i pulsanti della barra
     * di riproduzione, in tal modo la traccia non può più essere avviata.
     */
    @FXML
    private void handleStop(ActionEvent event) {
        playbackController.stop();
        resetLabels();
        setSkipPlaylistVisible(false);
        btnPlayPause.setDisable(true);
        btnPrevious.setDisable(true);
        btnSkip.setDisable(true);
        btnStop.setDisable(true);
        updatePlaylistName(null);
        System.out.println("[PlaybackBarViewController.handleStop] Pulsante Stop premuto: UI resettata");
    }

    /**
     * @brief Gestisce il click su Skip Traccia precedente.
     * @details Nel catalogo riavvia la traccia corrente. Invece in una playlist se la traccia corrente in riproduzione
     * ha superato i 10 secondi allora essa riparte da capo
     * altrimenti torna alla traccia precedente.
     */
    @FXML
    void handlePrevious(ActionEvent event) {
        double elapsed = playbackController.getCurrentAudioTime();
        if (elapsed <= 10) // Entro i primi 10s: torna alla traccia precedente.
            playbackController.previousTrack();
        else // Oltre i 10s: riavvia la traccia corrente dall'inizio.
            playbackController.restartTrack();
    }

    /**
     * @brief Gestisce il click su Skip Traccia successiva.
     * @details Salta alla traccia successiva e azzera il progresso della barra.
     */
    @FXML
    void handleSkipTrack(ActionEvent event) {
        playbackController.skipTrack();

    }

    /**
     * @brief Gestisce il click su Skip Playlist successiva.
     * @details Salta alla playlist successiva nella coda di riproduzione
     * e azzera il progresso della barra.
     */
    @FXML
    void handleSkipPlaylist(ActionEvent event) {
        playbackController.skipSource();
        resetProgress();
        if (playbackController.isStopped())
            setSkipPlaylistVisible(false);
    }

    /**
     * @brief Gestisce il click su Playlist precedente.
     * @details Simmetrico a {@link #handleSkipPlaylist}: torna alla sorgente
     * precedente nella coda e ne riproduce l'ultima traccia.
     */
    @FXML
    void handlePreviousPlaylist(ActionEvent event) {
        playbackController.previousSource();
        resetProgress();
    }

    /**
     * @brief Configura il timer per l'aggiornamento in tempo reale
     * della barra di avanzamento.
     * @details Usa {@link AnimationTimer} per aggiornare la UI sul thread
     * JavaFX ogni secondo. Al termine della traccia esegue lo skip automatico.
     */
    private void setupProgressTimer() {
        progressTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (currentTrack != null && playbackController.isPlaying() && !userSeeking) {
                    double realElapsedSeconds = playbackController.getCurrentAudioTime();
                    double total = currentTrack.getDuration();

                    if (total > 0) {
                        progressBar.setValue(realElapsedSeconds / total);
                        labelCurrentTime.setText(TrackFormatter.formatDuration((int) realElapsedSeconds));
                        labelTotalTime.setText("-"+TrackFormatter.formatDuration((int) (total - realElapsedSeconds)));
                    }
                }
            }
        };
    }



    /**
     * @brief Azzera il progresso della barra di avanzamento e il tempo trascorso.
     */
    private void resetProgress() {
        progressBar.setValue(0);
        labelCurrentTime.setText("0:00");
    }

    /**
     * @brief Reimposta tutte le label ai valori di default.
     * @details Chiamato quando nessuna traccia è in riproduzione.
     */
    private void resetLabels() {
        labelTrackTitle.setText("Nessuna traccia");
        labelTrackArtist.setText("—");
        labelTrackYear.setText("—");
        labelTrackGenre.setText("—");
        labelTotalTime.setText("0:00");
        resetProgress();

        btnPlayPause.setText("▶");
        progressTimer.stop();

        labelPlaylistName.setVisible(false);
        labelPlaylistName.setManaged(false);

    }

    /**
     * @brief Imposta la modalità di riproduzione sequenziale.
     * @details Chiama {@link PlaybackController#changeStrategy} con
     * {@link SequentialStrategy} ed evidenzia il bottone corrispondente.
     */
    @FXML
    void handleSequential(ActionEvent event) {
        playbackController.changeStrategy(new SequentialStrategy());
        setActiveMode(btnSequential);
    }

    /**
     * @brief Imposta la modalità di riproduzione shuffle.
     * @details Chiama {@link PlaybackController#changeStrategy} con
     * {@link ShuffleStrategy} ed evidenzia il bottone corrispondente.
     */
    @FXML
    void handleShuffle(ActionEvent event) {
        playbackController.changeStrategy(new ShuffleStrategy());
        setActiveMode(btnShuffle);
    }

    /**
     * @brief Imposta la modalità di riproduzione loop.
     * @details Chiama {@link PlaybackController#changeStrategy} con
     * {@link LoopStrategy} ed evidenzia il bottone corrispondente.
     */
    @FXML
    void handleLoop(ActionEvent event) {
        playbackController.changeStrategy(new LoopStrategy());
        setActiveMode(btnLoop);
    }

    /**
     * @brief Evidenzia il bottone della modalità attiva e ripristina gli altri.
     * @details Il bottone attivo riceve un bordo verde per indicare
     * la modalità correntemente selezionata.
     *
     * @param active il bottone da evidenziare
     */
    private void setActiveMode(Button active) {
        for (Button btn : new Button[]{btnSequential, btnShuffle, btnLoop}) {
            btn.getStyleClass().remove("mode-btn-active");
        }
        active.getStyleClass().add("mode-btn-active");
    }
    /**
     * Avvia la riproduzione dalla barra aggiornando le label e il timer.
     * Da chiamare dopo {@link PlaybackController#play} per sincronizzare
     * la barra con il nuovo stato.
     *
     * @param track la traccia con cui aggiornare la barra
     */
    public void startPlayback(Track track) {
        // ── DEBUG ──────────────────────────────────────────────────────────────
        System.out.println("[PlaybackBarViewController.startPlayback] chiamato con: "
                + (track != null ? track.getTitle() : "null"));
        // ──────────────────────────────────────────────────────────────────────

        updateTrackInfo(track);
        btnPlayPause.setText("⏸");
        btnPlayPause.setDisable(false);
        btnPrevious.setDisable(false);
        btnSkip.setDisable(false);
        btnStop.setDisable(false);
        progressTimer.start();
    }

    /**
     * @brief Aggiorna il nome della playlist in riproduzione.
     * @details Mostra il nome se si sta riproducendo una playlist,
     * nasconde la label se si riproduce dal catalogo.
     *
     * @param playlistName il nome della playlist, null per nascondere la label
     */
    public void updatePlaylistName(String playlistName) {
        if (playlistName != null) {
            labelPlaylistName.setText("▶ " + playlistName);
            labelPlaylistName.setVisible(true);
            labelPlaylistName.setManaged(true);
        } else {
            labelPlaylistName.setVisible(false);
            labelPlaylistName.setManaged(false);
        }
    }

}