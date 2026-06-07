package com.example.gruppo04.observer;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.model.PlaylistImpl;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.interfaces.PlaybackStrategy;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementazione concreta del catalogo musicale.
 * <p>
 * Questa classe agisce come sorgente di verità (Aggregate Root) per le tracce e le playlist.
 * Implementa il pattern Singleton per garantire un'unica istanza del catalogo nell'applicazione,
 * e il pattern Observer (ruolo di Subject) per notificare alle viste o ai controller registrati
 * ogni cambiamento di stato interno.
 * </p>
 */
public class ConcreteMusicCatalog implements MusicCatalog {

    /** Istanza unica del catalogo, volatile per garantire visibilità corretta in ambienti multi-thread. */
    private static volatile ConcreteMusicCatalog instance;

    /**
     * Costruttore privato: impedisce l'istanziazione diretta dall'esterno,
     * rispettando il contratto del pattern Singleton.
     */
    private ConcreteMusicCatalog() {}

    /**
     * Restituisce l'unica istanza di {@code ConcreteMusicCatalog}.
     * <p>
     * Utilizza il pattern <b>Double-Checked Locking</b> per garantire la thread-safety
     * senza introdurre sincronizzazione ad ogni invocazione.
     * </p>
     *
     * @return l'istanza singleton del catalogo
     */
    public static ConcreteMusicCatalog getInstance() {
        if (instance == null) {
            synchronized (ConcreteMusicCatalog.class) {
                if (instance == null) {
                    instance = new ConcreteMusicCatalog();
                }
            }
        }
        return instance;
    }

    /** Mappa per la memorizzazione delle tracce, garantisce l'ordine di inserimento e lookup O(1). */
    private final Map<UUID, Track> tracks = new LinkedHashMap<>();

    /** Lista per la memorizzazione ordinata delle playlist. */
    private final List<Playlist> playlists = new ArrayList<>();

    /**
     * Lista thread-safe degli observer registrati. Evita ConcurrentModificationException
     * se un observer si registra/deregistra durante una fase di notifica.
     */
    private final List<CatalogObserver> observers = new CopyOnWriteArrayList<>();


    /**
     * {@inheritDoc}
     * <p>In questa implementazione, il nome viene automaticamente ripulito da spazi iniziali e finali (trim)
     * prima della creazione dell'istanza {@link PlaylistImpl}.</p>
     */
    @Override
    public boolean createPlaylist(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della playlist non può essere nullo o vuoto.");
        }

        String correctName = name.trim();

        for (Playlist p : playlists) {
            if (p.getName().equalsIgnoreCase(correctName)) {
                return false;
            }
        }

        Playlist newPlaylist = new PlaylistImpl(correctName);
        playlists.add(newPlaylist);
        notifyObservers(CatalogEventType.PLAYLIST_ADDED, newPlaylist);

        return true;
    }

    /**
     * {@inheritDoc}
     * <p>Questa implementazione effettua il trim del nuovo nome ed evita di sollevare conflitti
     * se il nuovo nome coincide con il nome attuale della playlist stessa.</p>
     */
    @Override
    public boolean renamePlaylist(Playlist playlist, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nuovo nome non può essere nullo o vuoto.");
        }

        if (!playlists.contains(playlist)) {
            return false;
        }

        String correctName = newName.trim();

        for (Playlist p : playlists) {
            if (p != playlist && p.getName().equalsIgnoreCase(correctName)) {
                return false;
            }
        }

        playlist.setName(correctName);
        notifyObservers(CatalogEventType.PLAYLIST_RENAMED, playlist);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean deletePlaylist(Playlist playlist) {
        if (playlists.remove(playlist)) {
            notifyObservers(CatalogEventType.PLAYLIST_REMOVED, playlist);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public List<Playlist> getPlaylists() {
        return Collections.unmodifiableList(playlists);
    }

    /**
     * Aggiunge una nuova traccia al catalogo.
     * In caso di successo notifica gli observer con l'evento {@code TRACK_ADDED}.
     *
     * @param track la traccia da aggiungere; non deve essere {@code null}
     * @throws IllegalArgumentException se {@code track} è {@code null}, se esiste
     *         già una traccia con lo stesso titolo e autore, oppure se il filepath è già usato
     */
    public void addTrack(Track track) {
        if (track == null) throw new IllegalArgumentException("La traccia non può essere nulla.");

        for (Track t : tracks.values()) {
            if (t.getTitle().equalsIgnoreCase(track.getTitle()) &&
                    t.getAuthor().equalsIgnoreCase(track.getAuthor())) {
                throw new IllegalArgumentException("Titolo esiste già: un brano di '" + track.getAuthor() +
                        "' intitolato '" + track.getTitle() + "' è già presente nel catalogo.");
            }

            if (track.getFilePath() != null && t.getFilePath() != null) {
                if (t.getFilePath().equalsIgnoreCase(track.getFilePath())) {
                    throw new IllegalArgumentException("Questo file MP3 è già stato associato al brano '" +
                            t.getTitle() + "'. Seleziona un file diverso.");
                }
            }
        }

        tracks.put(track.getId(), track);
        notifyObservers(CatalogEventType.TRACK_ADDED, track);
    }

    /**
     * Aggiunge una traccia a una playlist del catalogo.
     * In caso di successo notifica gli observer con l'evento {@code PLAYLIST_TRACK_ADDED}.
     *
     * @param playlist la playlist destinazione
     * @param track    la traccia da aggiungere
     * @return {@code true} se aggiunta, {@code false} se già presente nella playlist
     */
    public boolean addTrackToPlaylist(Playlist playlist, Track track) {
        if (track == null) {
            throw new IllegalArgumentException("La traccia da aggiungere non può essere null.");
        }
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
     * Rimuove una traccia dal catalogo e, a cascata, da tutte le playlist che la contengono.
     * In caso di successo notifica gli observer con l'evento {@code TRACK_REMOVED}.
     *
     * @param trackId l'identificatore univoco della traccia da rimuovere
     */
    public void removeTrack(UUID trackId) {
        Track trackToRemove = tracks.remove(trackId);
        if (trackToRemove != null) {
            for (Playlist playlist : playlists) {
                playlist.removeTrack(trackToRemove);
            }
            notifyObservers(CatalogEventType.TRACK_REMOVED, trackToRemove);
        }
    }

    /**
     * Rimuove una traccia da una playlist specifica.
     * In caso di successo notifica gli observer con l'evento {@code PLAYLIST_TRACK_REMOVED}.
     *
     * @param playlist la playlist da cui rimuovere la traccia
     * @param track    la traccia da rimuovere
     * @return {@code true} se rimossa, {@code false} se non era presente
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
     * Applica controlli di integrità per impedire duplicati o collisioni sui filepath.
     * In caso di successo notifica gli observer con l'evento {@code TRACK_UPDATED}.
     *
     * @param updatedTrack la traccia con i dati aggiornati
     * @throws IllegalArgumentException se crea duplicati o la traccia non è trovata
     */
    public void updateTrack(Track updatedTrack) {
        if (updatedTrack == null || !tracks.containsKey(updatedTrack.getId())) {
            throw new IllegalArgumentException("Traccia non trovata.");
        }

        for (Track t : tracks.values()) {
            if (t.getId().equals(updatedTrack.getId())) continue;

            if (t.getTitle().equalsIgnoreCase(updatedTrack.getTitle()) &&
                    t.getAuthor().equalsIgnoreCase(updatedTrack.getAuthor())) {
                throw new IllegalArgumentException("Titolo già esistente: un brano di '" + updatedTrack.getAuthor() +
                        "' intitolato '" + updatedTrack.getTitle() + "' è già presente nel catalogo.");
            }

            if (updatedTrack.getFilePath() != null && t.getFilePath() != null) {
                if (t.getFilePath().equalsIgnoreCase(updatedTrack.getFilePath())) {
                    throw new IllegalArgumentException("Questo file MP3 è già stato associato al brano '" +
                            t.getTitle() + "'. Seleziona un file diverso.");
                }
            }
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
     * Registra un nuovo observer. Se già presente o nullo, l'operazione viene ignorata.
     *
     * @param observer l'oggetto che desidera ricevere le notifiche
     */
    public void registerObserver(CatalogObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Rimuove un observer precedentemente registrato.
     *
     * @param observer l'observer da deregistrare
     */
    public void unregisterObserver(CatalogObserver observer) {
        observers.remove(observer);
    }

    /**
     * Costruisce e invia un evento a tutti gli observer registrati.
     *
     * @param type   la tipologia dell'evento
     * @param target l'oggetto che ha subito la modifica
     */
    private void notifyObservers(CatalogEventType type, Object target) {
        CatalogEvent event = new CatalogEvent(type, this, target);
        for (CatalogObserver observer : observers) {
            observer.onCatalogChanged(event);
        }
    }

    /**
     * Ripristina lo stato interno del catalogo.
     * <b>Metodo di utilità utilizzato esclusivamente nei test</b> per garantire isolamento tra i vari casi di test.
     */
    public void reset() {
        tracks.clear();
        playlists.clear();
        observers.clear();
    }

    public void notifyStrategyChanged(PlaybackStrategy strategy) {
        notifyObservers(CatalogEventType.STRATEGY_CHANGED, strategy);
    }

    public void notifyPlaybackStarted(Track currentTrack, boolean isPlaylist) {
        notifyObservers(CatalogEventType.PLAYBACK_STARTED,
                new PlaybackStartedPayload(currentTrack, isPlaylist));
    }
}