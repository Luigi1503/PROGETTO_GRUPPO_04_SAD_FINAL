package com.example.gruppo04.model;

import java.util.UUID;

public interface ITrack {

    UUID getId();

    String getTitle();
    void setTitle(String title);

    String getAuthor();
    void setAuthor(String author);

    String getGenre();
    void setGenre(String genre);

    int getYear();
    void setYear(int year);

    int getDuration();
    void setDuration(int duration);




}