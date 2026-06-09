package com.example.gruppo04.model;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class AudioEngine {
    private MediaPlayer mediaPlayer;

    public void playTrack(String filePath, Runnable onFinished) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        File file = new File(filePath);
        Media media = new Media(file.toURI().toString());

        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();

        mediaPlayer.setOnEndOfMedia(() -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
    public void resume() {
        if (mediaPlayer != null) mediaPlayer.play();
    }

    public double getCurrentTimeInSeconds() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentTime().toSeconds();
        }
        return 0.0;
    }
}