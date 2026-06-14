package com.example.gruppo04.model;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.File;
import java.io.FileInputStream;

public class AudioEngine {

    private AdvancedPlayer player;
    private Thread playbackThread;
    private FileInputStream fis;

    private String currentFilePath;
    private long lastBytePosition = 0;
    private long totalFileSize = 0;

    private volatile boolean isPlaying = false;
    private volatile boolean isPaused = false;

    private long startTimeMillis = 0;
    private long timeAccruedBeforePause = 0;

    private Runnable currentOnFinished;

    /**
     * Identifica la riproduzione attualmente valida. Ogni nuova riproduzione
     * (o stop/pause) incrementa il contatore: i thread di riproduzione e i loro
     * callback confrontano la generazione con cui sono stati avviati con questo
     * valore e, se non corrispondono, si auto-annullano. Questo evita che uno
     * skip rapido lasci attiva una riproduzione "fantasma" o muta a causa della
     * natura asincrona di {@code player.play()}.
     */
    private int playbackGeneration = 0;

    public synchronized void playTrack(String filePath, Runnable onFinished) {
        if (filePath == null) {
            System.err.println("[AudioEngine] Impossibile riprodurre: il filePath fornito è NULL.");
            return;
        }
        System.out.println("[AudioEngine.playTrack] avvio riproduzione file: " + filePath);
        stop();

        this.currentFilePath = filePath;
        this.currentOnFinished = onFinished;
        this.totalFileSize = new File(filePath).length();
        this.lastBytePosition = 0;
        this.timeAccruedBeforePause = 0;
        this.isPaused = false;

        startPlayback();
    }

    private synchronized void startPlayback() {
        // Questa riproduzione diventa quella corrente: invalida eventuali thread precedenti.
        final int gen = ++playbackGeneration;
        isPlaying = true;
        startTimeMillis = System.currentTimeMillis();

        // Snapshot dei parametri: i campi condivisi potrebbero cambiare se parte un nuovo brano.
        final String path = currentFilePath;
        final long seekPos = lastBytePosition;
        final long fileSize = totalFileSize;
        final Runnable onFinished = currentOnFinished;

        Thread t = new Thread(() -> {
            FileInputStream localFis = null;
            try {
                localFis = new FileInputStream(path);
                if (seekPos > 0) {
                    localFis.getChannel().position(seekPos);
                }

                final AdvancedPlayer localPlayer = new AdvancedPlayer(localFis);

                // Pubblica il player solo se questa riproduzione è ancora quella corrente.
                // Se nel frattempo è arrivato uno stop()/nuovo brano, chiudo senza mai
                // aprire la linea audio (così non sottraggo l'audio al brano successivo).
                synchronized (AudioEngine.this) {
                    if (gen != playbackGeneration) {
                        localPlayer.close();
                        closeQuietly(localFis);
                        return;
                    }
                    player = localPlayer;
                    fis = localFis;
                }

                final FileInputStream fisRef = localFis;
                localPlayer.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent evt) {
                        synchronized (AudioEngine.this) {
                            // Callback di una riproduzione ormai superata: ignora.
                            if (gen != playbackGeneration) {
                                return;
                            }
                            isPlaying = false;
                            if (!isPaused) {
                                try {
                                    if (fisRef.getChannel().position() >= fileSize - 2000
                                            && onFinished != null) {
                                        onFinished.run();
                                    }
                                } catch (Exception e) {
                                    // posizione non leggibile: ignora
                                }
                            }
                        }
                    }
                });

                System.out.println("[AudioEngine] player.play() avviato per: " + path);
                localPlayer.play();

            } catch (Exception e) {
                System.err.println("[AudioEngine] Errore riproduzione: " + e.getMessage());
                synchronized (AudioEngine.this) {
                    if (gen == playbackGeneration) {
                        isPlaying = false;
                    }
                }
                closeQuietly(localFis);
            }
        });

        t.setDaemon(true);
        playbackThread = t;
        t.start();
    }

    public synchronized void pause() {
        if (isPlaying && !isPaused) {
            // Invalida la riproduzione corrente: il suo callback non deve avanzare al brano dopo.
            playbackGeneration++;
            isPaused = true;
            isPlaying = false;

            timeAccruedBeforePause += (System.currentTimeMillis() - startTimeMillis);

            try {
                if (fis != null) {
                    lastBytePosition = fis.getChannel().position();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (player != null) {
                player.close();
                player = null;
            }
            playbackThread = null;
        }
    }

    public synchronized void resume() {
        if (isPaused) {
            isPaused = false;
            startPlayback();
        }
    }

    /**
     * Riposiziona la riproduzione corrente al tempo indicato.
     * <p>
     * La posizione in byte viene stimata in modo proporzionale rispetto alla
     * durata totale della traccia (approccio coerente con il riposizionamento
     * per byte già usato in pausa/resume). Su MP3 a bitrate costante la stima è
     * praticamente esatta; su VBR è approssimata. Se la traccia è in pausa la
     * nuova posizione viene applicata e usata al successivo {@code resume()};
     * se è in riproduzione, riparte immediatamente dal nuovo punto.
     *
     * @param targetSeconds        posizione desiderata in secondi dall'inizio
     * @param totalDurationSeconds durata totale della traccia in secondi
     */
    public synchronized void seek(double targetSeconds, double totalDurationSeconds) {
        // Niente da riposizionare se la riproduzione è ferma o i dati non sono validi.
        if ((!isPlaying && !isPaused) || totalDurationSeconds <= 0 || totalFileSize <= 0) {
            return;
        }

        double clamped = Math.max(0.0, Math.min(targetSeconds, totalDurationSeconds));
        long bytePos = (long) ((clamped / totalDurationSeconds) * totalFileSize);

        boolean wasPaused = isPaused;

        // Invalida il thread/callback in volo e chiude la linea audio corrente.
        playbackGeneration++;
        if (player != null) {
            player.close();
            player = null;
        }
        if (fis != null) {
            closeQuietly(fis);
            fis = null;
        }
        playbackThread = null;

        // Allinea posizione in byte e tempo accumulato al nuovo punto.
        lastBytePosition = bytePos;
        timeAccruedBeforePause = (long) (clamped * 1000);

        if (wasPaused) {
            // Resta in pausa: la nuova posizione verrà usata al prossimo resume().
            isPlaying = false;
            isPaused = true;
        } else {
            // Riprende immediatamente dal nuovo punto.
            isPaused = false;
            startPlayback();
        }
    }

    public synchronized void stop() {
        // Invalida la riproduzione corrente: il vecchio thread, quando la sua play()
        // termina (o quando scopre la generazione cambiata), si chiude da solo.
        playbackGeneration++;
        isPlaying = false;
        isPaused = false;
        lastBytePosition = 0;
        timeAccruedBeforePause = 0;

        if (player != null) {
            player.close();
            player = null;
        }
        if (fis != null) {
            closeQuietly(fis);
            fis = null;
        }
        playbackThread = null;
    }

    private static void closeQuietly(FileInputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * @return Il tempo corrente della riproduzione in secondi reali, tenendo conto delle pause.
     */
    public synchronized double getCurrentTimeInSeconds() {
        if (!isPlaying && !isPaused) {
            return 0.0;
        }
        if (isPaused) {
            return timeAccruedBeforePause / 1000.0;
        } else {
            long currentSessionTime = System.currentTimeMillis() - startTimeMillis;
            return (timeAccruedBeforePause + currentSessionTime) / 1000.0;
        }
    }
}