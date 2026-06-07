package com.example.gruppo04.persistence;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.observer.ConcreteMusicCatalog;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @brief Gestore della persistenza per il catalogo musicale.
 * * Questa classe implementa il pattern Singleton ed è responsabile del salvataggio
 * e caricamento dello stato del catalogo musicale (tracce e playlist) su memoria persistente
 * tramite il meccanismo di serializzazione nativa Java.
 */
public class PersistenceManager {

    /** @brief Istanza unica del gestore della persistenza (pattern Singleton). */
    private static volatile PersistenceManager instance;

    /** @brief Percorso di default utilizzato per il salvataggio e il caricamento del catalogo. */
    private String defaultFilePath = "catalog.ser";

    /**
     * @brief Costruttore privato per impedire l'istanziazione diretta dall'esterno,
     * rispettando il contratto del pattern Singleton.
     */
    private PersistenceManager() {}

    /**
     * @brief Restituisce l'unica istanza attiva di {@code PersistenceManager}.
     * * @return L'istanza singleton di {@code PersistenceManager}.
     */
    public static PersistenceManager getInstance() {
        if (instance == null) {
            synchronized (PersistenceManager.class) {
                if (instance == null) {
                    instance = new PersistenceManager();
                }
            }
        }
        return instance;
    }

    /**
     * @brief Imposta il percorso del file predefinito per le operazioni di persistenza.
     * * @param filePath Il nuovo percorso relativo o assoluto del file da usare come default.
     */
    public void setDefaultFilePath(String filePath) {
        this.defaultFilePath = filePath;
    }

    /**
     * @brief Restituisce il percorso del file predefinito attualmente configurato.
     * * @return La stringa del percorso del file di default.
     */
    public String getDefaultFilePath() {
        return this.defaultFilePath;
    }

    /**
     * @brief Salva lo stato corrente del catalogo musicale sul file predefinito.
     * * @param catalog Il catalogo musicale di cui salvare lo stato; non deve essere null.
     * @throws IOException Se si verifica un errore durante la scrittura sul file.
     * @throws IllegalArgumentException Se il parametro catalog è null.
     */
    public void save(MusicCatalog catalog) throws IOException {
        save(catalog, defaultFilePath);
    }

    /**
     * @brief Salva lo stato corrente del catalogo musicale sul file specificato dal percorso.
     * * @param catalog Il catalogo musicale da persistere; non deve essere null.
     * @param filePath Il percorso del file di destinazione; non deve essere nullo o vuoto.
     * @throws IOException Se si verifica un errore di I/O durante il salvataggio.
     * @throws IllegalArgumentException Se catalog è nullo o se filePath è nullo/vuoto.
     */
    public void save(MusicCatalog catalog, String filePath) throws IOException {
        if (catalog == null) {
            throw new IllegalArgumentException("Il catalogo non può essere nullo.");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Il percorso del file non può essere nullo o vuoto.");
        }

        SerializableCatalogData data = new SerializableCatalogData(catalog.getAllTracks(), catalog.getPlaylists());

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
        }
    }

    /**
     * @brief Carica lo stato del catalogo musicale dal file predefinito.
     * * @return Il catalogo musicale popolato con lo stato caricato.
     * @throws IOException Se si verifica un errore durante la lettura del file.
     * @throws ClassNotFoundException Se la classe del file serializzato non è trovata.
     */
    public MusicCatalog load() throws IOException, ClassNotFoundException {
        return load(defaultFilePath);
    }

    /**
     * @brief Carica lo stato del catalogo musicale dal file specificato dal percorso.
     * * Ripristina lo stato del catalogo caricando le tracce e le playlist persistite,
     * aggiornando direttamente il Singleton del catalogo per preservare gli observer attivi.
     * * @param filePath Il percorso del file da cui caricare lo stato; non deve essere nullo o vuoto.
     * @return Il catalogo musicale popolato con lo stato ripristinato.
     * @throws IOException Se si verifica un errore di I/O durante il caricamento o se il file non esiste.
     * @throws ClassNotFoundException Se la classe dell'oggetto letto non è definita nell'applicazione.
     * @throws IllegalArgumentException Se filePath è nullo o vuoto.
     */
    public MusicCatalog load(String filePath) throws IOException, ClassNotFoundException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Il percorso del file non può essere nullo o vuoto.");
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            SerializableCatalogData data = (SerializableCatalogData) ois.readObject();
            ConcreteMusicCatalog catalog = ConcreteMusicCatalog.getInstance();
            catalog.restoreState(data.getTracks(), data.getPlaylists());
            return catalog;
        }
    }

    /**
     * @brief Classe di utilità interna per serializzare i dati del catalogo.
     * * Raggruppa le collezioni di tracce e playlist in un unico oggetto serializzabile
     * in modo da poter salvare e caricare l'intero stato in un'unica operazione di lettura/scrittura.
     */
    private static class SerializableCatalogData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** @brief Collezione di tracce da serializzare. */
        private final List<Track> tracks;

        /** @brief Lista di playlist da serializzare. */
        private final List<Playlist> playlists;

        /**
         * @brief Costruisce un oggetto wrapper contenente copie delle collezioni da serializzare.
         * * @param tracks Le tracce del catalogo.
         * @param playlists Le playlist del catalogo.
         */
        public SerializableCatalogData(Collection<Track> tracks, List<Playlist> playlists) {
            this.tracks = new ArrayList<>(tracks);
            this.playlists = new ArrayList<>(playlists);
        }

        /**
         * @brief Restituisce la lista di tracce caricate.
         * @return Lista di tracce.
         */
        public List<Track> getTracks() {
            return tracks;
        }

        /**
         * @brief Restituisce la lista di playlist caricate.
         * @return Lista di playlist.
         */
        public List<Playlist> getPlaylists() {
            return playlists;
        }
    }
}
