package com.example.gruppo04.model;

import java.util.*;

import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;

public class PlaylistImpl implements Playlist {
    private String name;
    private final String id;
    private List<PlayableSource> tracks = new ArrayList<>();

    /**
     * Implementazione di {@link Playlist} basata su lista ad accesso ordinato.
     * L'uguaglianza è definita sul solo id immutabile.
     *
     * @param name il nome della playlist
     * @throws IllegalArgumentException se {@code nome} è {@code null} o vuoto
     */
    public PlaylistImpl(String name) {
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Campo Nome Vuoto");
        }
        this.name = name;
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String getName(){
        return this.name;
    }

    /**
     * Restituisce l'identificatore univoco e immutabile della playlist.
     *
     * @return l'id della playlist
     */
    public String getId(){
        return this.id;
    }

    @Override
    public void setName(String name){
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Campo Nome Vuoto");
        }
        this.name = name;
    }
   @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistImpl playlist = (PlaylistImpl) o;
        return id.equals(playlist.id) ;
   }

    @Override
    public int hashCode() {
        return Objects.hash(id);
   }

    @Override
    public boolean addTrack(Track track){
        boolean result = false;
        if(!tracks.contains(track)) {
            tracks.add(track);
            result = true;
        }
        return result;
    }

    @Override
    public boolean removeTrack(Track track){
        boolean result = false;
        if(tracks.contains(track)) {
            tracks.remove(track);
            result = true;
        }
        return result;
    }




    @Override
    public String toString() {
        return this.name;
    }



    @Override
    public List<Track> getTracks() {
        List<Track> allTracks = new ArrayList<>();
        for (PlayableSource element : this.tracks) {
            allTracks.addAll(element.getTracks());
        }
        return allTracks;

    }

}
