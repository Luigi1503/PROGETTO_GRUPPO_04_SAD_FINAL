package com.example.gruppo04.model;

import java.util.List;
import java.util.Objects;

public class Playlist {
    private String nome;
    private List<Track> tracks;

    public Playlist(String nome) { //costruttore
        if(nome == null) {
            throw new NullPointerException("nome Nullo");
        }
        this.nome = nome;
    }


    public String getNome(){
        return this.nome;
    }

    public void setNome(String nome){
        if(nome == null) {
            throw new NullPointerException("nome Nullo");
        }
        this.nome = nome;
    }
   @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return nome.equals(playlist.nome);
   }

   @Override
    public int hashCode() {
        return Objects.hash(nome);
   }

    @Override
    public String toString() {
        return this.nome;
    }
}
