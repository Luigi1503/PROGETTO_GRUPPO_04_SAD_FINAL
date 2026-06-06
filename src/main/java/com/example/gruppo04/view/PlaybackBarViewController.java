package com.example.gruppo04.view;

import com.example.gruppo04.controller.PlaybackController;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.*;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;
import com.example.gruppo04.util.TrackFormatter;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import com.example.gruppo04.model.*;

import java.util.logging.Logger;

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

    /**
     * @brief Bottone skip playlist successiva.
     * @details Visibile solo durante la riproduzione di una playlist.
     */
    @FXML private Button btnSkipPlaylist;

    /** @brief Barra di avanzamento della traccia corrente. */
    @FXML private ProgressBar progressBar;

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

    /** @brief Controller MVC della riproduzione. */
    private PlaybackController playbackController;

    /** @brief Catalogo musicale, usato per la registrazione come Observer. */
    private MusicCatalog catalog;

    /** @brief Indica se la riproduzione è attualmente in corso. */
    private boolean isPlaying = false;

    /** @brief Traccia attualmente in riproduzione. */
    private Track currentTrack;

    /** @brief Timer per l'aggiornamento in tempo reale della barra di avanzamento. */
    private AnimationTimer progressTimer;

    /** @brief Secondi trascorsi dall'inizio della riproduzione della traccia corrente. */
    private long elapsedSeconds = 0;

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
        this.catalog = catalog;
        this.catalog.registerObserver(this);
        setupProgressTimer();
    }

    /**
     * @brief Chiamato automaticamente da FXMLLoader dopo il caricamento dell'FXML.
     * @details Inizializza lo stato iniziale della barra di riproduzione.
     * Di default è attivo il bottone di sequenzializzazione
     */
    @FXML
    void initialize() {
        progressBar.setProgress(0);
        btnSkipPlaylist.setVisible(false); // è invisibile
        btnSkipPlaylist.setManaged(false); // ed è escluso dal layout
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
        switch (event.getType()) {
            case TRACK_REMOVED:
                // Se la traccia rimossa è quella in riproduzione
                // la barra si aggiorna con la nuova traccia corrente.
                Track removedTrack = (Track) event.getTarget();
                if (removedTrack.equals(currentTrack)) {
                    if (playbackController.isStopped()) {
                        resetLabels();
                    } else {
                        updateTrackInfo(playbackController.getCurrentTrack());
                    }
                }
                break;

            case PLAYLIST_REMOVED:
                if (playbackController.isStopped()) {
                    // nessuna playlist successiva quindi la riproduzione si ferma
                    setSkipPlaylistVisible(false);
                    resetLabels();
                } else {
                    // c'è una playlist successiva in coda quindi si aggiorna la traccia
                    updateTrackInfo(playbackController.getCurrentTrack());
                    // btnSkipPlaylist rimane visibile perché siamo ancora in una playlist
                }
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
        } else {
            resetLabels();
        }
    }

    /**
     * @brief Mostra o nasconde il bottone di skip playlist.
     * @details Da chiamare quando si inizia o si termina
     * la riproduzione di una playlist.
     *
     * @param visible true se si sta riproducendo una playlist, false altrimenti
     */
    public void setSkipPlaylistVisible(boolean visible) {
        btnSkipPlaylist.setVisible(visible);
        btnSkipPlaylist.setManaged(visible);
    }

    /**
     * @brief Gestisce il click su Play/Pausa.
     * @details Alterna tra riproduzione e pausa aggiornando il timer
     * e il testo del bottone.
     */
    @FXML
    void handlePlayPause(@SuppressWarnings("unused") ActionEvent event) {
        if (isPlaying) {
            playbackController.pause();
            isPlaying = false;
            btnPlayPause.setText("▶");
            progressTimer.stop();
        } else {
            if (!playbackController.isStopped()) {
                // riprende dalla pausa
                playbackController.resume();
                isPlaying = true;
                btnPlayPause.setText("⏸");
                progressTimer.start();
            }
            // se è STOPPED non fa nulla — il play viene avviato
            // da PlaylistDetailView o TrackListView
        }
    }

    /**
     * @brief Gestisce il click su Skip Traccia precedente.
     * @details Torna alla traccia precedente e azzera il progresso della barra.
     */
    @FXML
    void handlePrevious(ActionEvent event) {
        playbackController.previousTrack();
        resetProgress();
        updateTrackInfo(playbackController.getCurrentTrack());
    }

    /**
     * @brief Gestisce il click su Skip Traccia successiva.
     * @details Salta alla traccia successiva e azzera il progresso della barra.
     */
    @FXML
    void handleSkipTrack(ActionEvent event) {
        playbackController.skipTrack();
        resetProgress();
        updateTrackInfo(playbackController.getCurrentTrack());
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
        if (playbackController.isStopped()) {
            resetLabels();
            setSkipPlaylistVisible(false);
        } else {
            updateTrackInfo(playbackController.getCurrentTrack());
        }
    }

    /**
     * @brief Configura il timer per l'aggiornamento in tempo reale
     * della barra di avanzamento.
     * @details Usa {@link AnimationTimer} per aggiornare la UI sul thread
     * JavaFX ogni secondo. Al termine della traccia esegue lo skip automatico.
     */
    private void setupProgressTimer() {
        progressTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            //il seguente metodo è chiamato circa 60 volte al secondo da AnimationTimer
            //now è il tempo corrente in nanosecondi
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000L) {
                    //è trascorso 1 secondo
                    lastUpdate = now;
                    if (currentTrack != null && isPlaying) {
                        elapsedSeconds++;
                        int total = currentTrack.getDuration();
                        if (elapsedSeconds >= total) {
                            //la traccia è terminata
                            elapsedSeconds = total;
                            progressTimer.stop();
                            isPlaying = false;
                            btnPlayPause.setText("▶");
                            playbackController.skipTrack();
                            updateTrackInfo(playbackController.getCurrentTrack());
                            if (!playbackController.isStopped()) {
                                //c'è una traccia successiva
                                isPlaying = true;
                                btnPlayPause.setText("⏸");
                                progressTimer.start();
                            }
                        } else {
                            //la traccia non è finita
                            double progress = (double) elapsedSeconds / total;
                            progressBar.setProgress(progress);
                            labelCurrentTime.setText(
                                    TrackFormatter.formatDuration((int) elapsedSeconds));
                        }
                    }
                }
            }
        };
    }

    /**
     * @brief Azzera il progresso della barra di avanzamento e il tempo trascorso.
     */
    private void resetProgress() {
        elapsedSeconds = 0;
        progressBar.setProgress(0);
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
        labelCurrentTime.setText("0:00");
        progressBar.setProgress(0);
        isPlaying = false;
        btnPlayPause.setText("▶");
        progressTimer.stop();
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
}