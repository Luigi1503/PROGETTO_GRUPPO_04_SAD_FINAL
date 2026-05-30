package com.example.gruppo04.model;

import java.util.*;

public class PlaylistImpl implements Playlist {
    private String nome;
    private final String id;
    private List<Track> tracks = new ArrayList<>();

    /**
     * Implementazione di {@link Playlist} basata su lista ad accesso ordinato.
     * L'uguaglianza è definita sul solo id immutabile.
     *
     * @param nome il nome della playlist
     * @throws IllegalArgumentException se {@code nome} è {@code null} o vuoto
     */
    public PlaylistImpl(String nome) {
        if(nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException("Campo Nome Vuoto");
        }
        this.nome = nome;
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String getNome(){
        return this.nome;
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
    public void setNome(String nome){
        if(nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException("Campo Nome Vuoto");
        }
        this.nome = nome;
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
    public List<Track> getTracks(){
        return Collections.unmodifiableList(this.tracks);
    }


    @Override
    public String toString() {
        return this.nome;
    }
}
