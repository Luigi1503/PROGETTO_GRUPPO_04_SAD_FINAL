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
    private volatile boolean stoppedManually = false;

    private long startTimeMillis = 0;
    private long timeAccruedBeforePause = 0;

    private Runnable currentOnFinished;

    public synchronized void playTrack(String filePath, Runnable onFinished) {
        if (filePath == null) {
            System.err.println("[AudioEngine] Impossibile riprodurre: il filePath fornito è NULL.");
            return;
        }
        stop();

        this.currentFilePath = filePath;
        this.currentOnFinished = onFinished;
        this.totalFileSize = new File(filePath).length();
        this.lastBytePosition = 0;
        this.timeAccruedBeforePause = 0;
        this.isPaused = false;
        this.stoppedManually = false;

        startPlayback();
    }

    private synchronized void startPlayback() {
        isPlaying = true;
        startTimeMillis = System.currentTimeMillis();

        playbackThread = new Thread(() -> {
            try {
                fis = new FileInputStream(currentFilePath);

                if (lastBytePosition > 0) {
                    fis.getChannel().position(lastBytePosition);
                }

                player = new AdvancedPlayer(fis);

                player.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent evt) {
                        isPlaying = false;

                        if (!stoppedManually && !isPaused) {
                            try {
                                if (fis != null && fis.getChannel().position() >= totalFileSize - 2000) {
                                    if (currentOnFinished != null) {
                                        currentOnFinished.run();
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                });

                player.play();

            } catch (Exception e) {
                System.err.println("[AudioEngine] Errore riproduzione: " + e.getMessage());
                isPlaying = false;
            }
        });

        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    public synchronized void pause() {
        if (isPlaying && !isPaused) {
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
            }
        }
    }

    public synchronized void resume() {
        if (isPaused) {
            isPaused = false;
            startPlayback();
        }
    }

    public synchronized void stop() {
        stoppedManually = true;
        isPlaying = false;
        isPaused = false;
        lastBytePosition = 0;
        timeAccruedBeforePause = 0;

        if (player != null) {
            player.close();
            player = null;
        }
        if (fis != null) {
            try { fis.close(); } catch (Exception e) {}
            fis = null;
        }
        if (playbackThread != null) {
            playbackThread.interrupt();
            playbackThread = null;
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