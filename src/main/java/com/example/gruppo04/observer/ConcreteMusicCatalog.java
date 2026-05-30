package com.example.gruppo04.observer;

import com.example.gruppo04.model.MusicCatalog;
import com.example.gruppo04.model.Playlist;
import com.example.gruppo04.model.PlaylistImpl;
import com.example.gruppo04.model.Track;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementazione concreta del catalogo musicale.
 * <p>
 * Questa classe agisce come sorgente di verità (Aggregate Root) per le tracce e le playlist.
 * Implementa inoltre il pattern Observer (ruolo di Subject) per notificare alle viste
 * o ai controller registrati ogni cambiamento di stato interno.
 * </p>
 */
public class ConcreteMusicCatalog implements MusicCatalog {

    /** Mappa per la memorizzazione delle tracce, garantisce l'ordine di inserimento e lookup O(1). */
    private final Map<UUID, Track> tracks = new LinkedHashMap<>();

    /** Lista per la memorizzazione ordinata delle playlist. */
    private final List<Playlist> playlists = new ArrayList<>();

    /** * Lista thread-safe degli observer registrati. Evita ConcurrentModificationException
     * se un observer si registra/deregistra durante una fase di notifica.
     */
    private final List<CatalogObserver> observers = new CopyOnWriteArrayList<>();


    /**
     * {@inheritDoc}
     * <p>In questa implementazione, il nome viene automaticamente ripulito da spazi iniziali e finali (trim)
     * prima della creazione dell'istanza {@link PlaylistImpl}.</p>
     */
    @Override
    public boolean createPlaylist(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della playlist non può essere nullo o vuoto.");
        }

        String nomePulito = nome.trim();

        // Controllo univocità rispetto alle playlist esistenti
        for (Playlist p : playlists) {
            if (p.getNome().equalsIgnoreCase(nomePulito)) {
                return false; // Esiste già una playlist con questo nome
            }
        }

        Playlist nuovaPlaylist = new PlaylistImpl(nomePulito);
        playlists.add(nuovaPlaylist);
        notifyObservers(CatalogEventType.PLAYLIST_ADDED, nuovaPlaylist);

        return true;
    }

    /**
     * {@inheritDoc}
     * <p>Questa implementazione effettua il trim del nuovo nome ed evita di sollevare conflitti
     * se il nuovo nome coincide con il nome attuale della playlist stessa.</p>
     */
    @Override
    public boolean renamePlaylist(Playlist playlist, String nuovoNome) {
        if (nuovoNome == null || nuovoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nuovo nome non può essere nullo o vuoto.");
        }

        if (!playlists.contains(playlist)) {
            return false; // La playlist non appartiene a questo catalogo
        }

        String nomePulito = nuovoNome.trim();

        // Controllo univocità: il nuovo nome non deve appartenere a un'ALTRA playlist
        for (Playlist p : playlists) {
            if (p != playlist && p.getNome().equalsIgnoreCase(nomePulito)) {
                return false;
            }
        }

        playlist.setNome(nomePulito); // Assume che Playlist abbia un setter per il nome
        notifyObservers(CatalogEventType.PLAYLIST_RENAMED, playlist);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deletePlaylist(Playlist playlist) {
        if (playlists.remove(playlist)) {
            notifyObservers(CatalogEventType.PLAYLIST_REMOVED, playlist);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Playlist> getPlaylists() {
        return Collections.unmodifiableList(playlists);
    }

    /**
     * Aggiunge una nuova traccia al catalogo.
     * In caso di successo notifica gli observer con l'evento {@code TRACK_ADDED}.
     *
     * @param track la traccia da aggiungere; non deve essere {@code null}
     * @throws IllegalArgumentException se {@code track} è {@code null} o se esiste
     * già una traccia con lo stesso titolo nel catalogo
     */
    public void addTrack(Track track) {
        if (track == null) throw new IllegalArgumentException("La traccia non può essere nulla.");

        // Controllo duplicati base
        for (Track t : tracks.values()) {
            if (t.getTitle().equalsIgnoreCase(track.getTitle())) {
                throw new IllegalArgumentException("Una traccia con questo titolo esiste già.");
            }
        }

        tracks.put(track.getId(), track);
        notifyObservers(CatalogEventType.TRACK_ADDED, track);
    }

    /**
     * Aggiunge una nuova traccia alla playlist
     * In caso di successo notifica gli observer con l'evento {@code PLAYLIST_TRACK_ADDED}.
     * @param playlist la playlist a cui aggiungere la traccia
     * @param track    la traccia da aggiungere
     * @return {@code true} se aggiunta, {@code false} se la traccia è già
     *         presente nella playlist
     */

    public boolean addTrackToPlaylist(Playlist playlist, Track track) {
        if (!playlists.contains(playlist)) {
            throw new IllegalArgumentException("La playlist non appartiene a questo catalogo.");
        }
        if (!tracks.containsKey(track.getId())) {
            throw new IllegalArgumentException("La traccia non è presente nel catalogo.");
        }
        boolean added = playlist.addTrack(track);
        if (added) {
            notifyObservers(CatalogEventType.PLAYLIST_TRACK_ADDED, track);
        }
        return added;
    }


    /**
     * Rimuove una traccia dal catalogo dato il suo identificatore univoco (UUID).
     * <p>
     * <b>Effetto a cascata:</b> La rimozione di una traccia dal catalogo la elimina
     * automaticamente da tutte le playlist in cui è inclusa.
     * </p>
     * Se la traccia è trovata e rimossa, notifica gli observer con l'evento {@code TRACK_REMOVED}.
     *
     * @param trackId l'identificatore univoco della traccia da rimuovere
     */
    public void removeTrack(UUID trackId) {
        Track trackToRemove = tracks.remove(trackId);
        if (trackToRemove != null) {
            for (Playlist playlist : playlists) {
                playlist.getTracks().remove(trackToRemove);
            }
            notifyObservers(CatalogEventType.TRACK_REMOVED, trackToRemove);
        }
    }

    /**
     * Rimuove una nuova traccia dalla playlist
     * In caso di successo notifica gli observer con l'evento {@code PLAYLIST_TRACK_REMOVED}.
     * @param playlist la playlist in cui si deve rimuovere la traccia
     * @param track    la traccia da rimuovere
     * @return {@code true} se rimossa, {@code false} se la traccia non è
     *          presente nella playlist
     */
    public boolean removeTrackFromPlaylist(Playlist playlist, Track track) {
        if (playlist.getTracks().contains(track)) {
            playlist.removeTrack(track);
            notifyObservers(CatalogEventType.PLAYLIST_TRACK_REMOVED, track);
            return true;
        }
        return false;
    }

    /**
     * Aggiorna i dati di una traccia esistente nel catalogo.
     * In caso di successo notifica gli observer con l'evento {@code TRACK_UPDATED}.
     *
     * @param updatedTrack la traccia con i dati aggiornati; il suo ID deve corrispondere
     * a una traccia già presente nel catalogo
     * @throws IllegalArgumentException se {@code updatedTrack} è {@code null} o non
     * corrisponde a nessuna traccia nota nel catalogo
     */
    public void updateTrack(Track updatedTrack) {
        if (updatedTrack == null || !tracks.containsKey(updatedTrack.getId())) {
            throw new IllegalArgumentException("Traccia non trovata.");
        }
        tracks.put(updatedTrack.getId(), updatedTrack);
        notifyObservers(CatalogEventType.TRACK_UPDATED, updatedTrack);
    }

    /**
     * Restituisce tutte le tracce presenti nel catalogo, mantenendo l'ordine di inserimento.
     *
     * @return una vista in sola lettura della collezione di tracce (mai {@code null})
     */
    public Collection<Track> getAllTracks() {
        return Collections.unmodifiableCollection(tracks.values());
    }

    /**
     * Registra un nuovo observer per l'ascolto degli eventi di dominio del catalogo.
     * Se l'observer è già registrato o è nullo, l'operazione viene ignorata.
     *
     * @param observer l'oggetto che desidera ricevere le notifiche
     */
    public void registerObserver(CatalogObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Rimuove un observer precedentemente registrato, cessando l'invio delle notifiche.
     *
     * @param observer l'observer da deregistrare
     */
    public void unregisterObserver(CatalogObserver observer) {
        observers.remove(observer);
    }

    /**
     * Metodo interno di utilità per costruire e inviare eventi a tutti gli observer in ascolto.
     *
     * @param type   la tipologia dell'evento (es. TRACK_ADDED, PLAYLIST_REMOVED)
     * @param target l'oggetto specifico che ha subito la modifica (es. istanza di Track o Playlist)
     */
    private void notifyObservers(CatalogEventType type, Object target) {
        CatalogEvent event = new CatalogEvent(type, this, target);
        for (CatalogObserver observer : observers) {
            observer.onCatalogChanged(event);
        }
    }
}