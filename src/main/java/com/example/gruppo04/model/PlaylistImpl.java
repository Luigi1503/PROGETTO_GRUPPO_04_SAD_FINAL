package com.example.gruppo04.model;

import java.util.*;

import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;

public class PlaylistImpl implements Playlist {
    private String name;
    private final String id;
    private List<Track> tracks = new ArrayList<>();
    private int playCount;

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
        this.playCount = 0;
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
    public boolean addTrackAt(int index, Track track){
        if (tracks.contains(track)) {
            return false;
        }
        int i = Math.max(0, Math.min(index, tracks.size()));
        tracks.add(i, track);
        return true;
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


    /**
     * @brief Restituisce tutte le tracce contenute nella playlist.
     * * @details Scorre tutti gli elementi registrati nella playlist ed estrae
     * iterativamente le tracce per inserirle in una nuova lista aggregata.
     * Restituisce una nuova istanza di ArrayList per preservare l'incapsulamento
     * ed evitare che modifiche alla lista restituita alterino lo stato interno della playlist.
     * * @return Una List<Track> contenente tutti i brani attualmente presenti nella playlist.
     */
    @Override
    public List<Track> getTracks() {
        return Collections.unmodifiableList(new ArrayList<>(this.tracks));
    }

    /**
     * @brief Genera e restituisce un dizionario contenente i metadati generali della playlist.
     * * @details Fornisce le informazioni di contesto del contenitore (Nome, Tipo e numero di brani).
     * Utilizza una LinkedHashMap per garantire che i componenti dell'interfaccia utente
     * leggano e mostrino i dati esattamente nell'ordine in cui sono stati inseriti.
     * Il tipo è esplicitamente impostato a "Playlist" per differenziarlo dai singoli brani.
     * * @return Una Map<String, String> contenente le coppie chiave-valore con i dettagli della playlist.
     */
    @Override
    public Map<String, String> getDisplayName() {
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("Nome", this.name);
        meta.put("Tipo", "Playlist");
        meta.put("Brani Totali", String.valueOf(this.tracks.size()));
        return meta;
    }

    @Override
    public void incrementPlayCount() {
        this.playCount++;
    }

    @Override
    public int getPlayCount() {
        return this.playCount;
    }

    @Override
    public void moveTrack(int from, int to) {
        Track track = tracks.remove(from);
        tracks.add(to, track);
    }
}
