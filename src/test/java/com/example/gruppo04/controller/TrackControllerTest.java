package com.example.gruppo04.controller;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @file TrackControllerTest.java
 * @brief Suite di test per {@link TrackController}.
 * @details Verifica che il controller gestisca correttamente l'inizializzazione,
 * deleghi la creazione e modifica delle tracce al modello, e comunichi
 * le variazioni di stato al catalogo musicale centrale in modo accurato.
 */
class TrackControllerTest {

    /** @brief Il catalogo musicale usato per il test. */
    private MusicCatalog catalog;

    /** @brief Il controller sotto test. */
    private TrackController controller;

    /**
     * @brief Inizializza l'ambiente prima di ogni singolo test.
     * @details Recupera l'istanza Singleton di ConcreteMusicCatalog ed esegue un reset
     * dello stato interno prima di inniettarlo nel controller. Questo garantisce l'isolamento,
     * assicurando che ogni test parta da una tabula rasa senza tracce residue.
     */
    @BeforeEach
    void setUp() {
        ConcreteMusicCatalog concreteCatalog = ConcreteMusicCatalog.getInstance();

        concreteCatalog.reset();

        this.catalog = concreteCatalog;
        this.controller = new TrackController(catalog);
    }

    /**
     * @brief Verifica la sicurezza del costruttore.
     * @details Assicura che il passaggio di un catalogo nullo provochi
     * il lancio di un'eccezione, impedendo la creazione di un controller invalido.
     */
    @Test
    void constructor_nullCatalog_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new TrackController(null));
    }

    /**
     * @brief Verifica l'aggiunta di una traccia con dati validi.
     * @details Controlla che, passando parametri corretti, il controller crei
     * la traccia e la inserisca effettivamente nel catalogo.
     */
    @Test
    void addTrack_validData_addsToCatalog() {
        controller.addTrack("Nuovo Titolo", "Nuovo Autore", "Rock", 2024, 180, "file.mp3");

        Collection<Track> tracks = catalog.getAllTracks();
        assertEquals(1, tracks.size(), "Il catalogo dovrebbe contenere esattamente 1 traccia");

        Track savedTrack = tracks.iterator().next();
        assertEquals("Nuovo Titolo", savedTrack.getTitle());
    }

    /**
     * @brief Verifica che il controller propaghi le eccezioni di validazione.
     * @details Tenta di aggiungere una traccia con un anno non valido (-50) e verifica
     * che l'eccezione generata da TrackImpl venga correttamente fatta "rimbalzare"
     * dal controller, impedendo il salvataggio nel catalogo.
     */
    @Test
    void addTrack_invalidData_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.addTrack("Titolo", "Autore", "Pop", -50, 180, null)
        );

        assertEquals(0, catalog.getAllTracks().size());
    }

    /**
     * @brief Verifica la corretta modifica di una traccia esistente.
     * @details Aggiunge una traccia al catalogo, la recupera, ne modifica i parametri
     * tramite il controller e verifica che i nuovi valori siano stati applicati.
     */
    @Test
    void updateTrack_validData_updatesTrackInCatalog() {
        controller.addTrack("Vecchio Titolo", "Vecchio Autore", "Jazz", 2000, 100, "vecchio_file.mp3");
        Track trackToUpdate = catalog.getAllTracks().iterator().next();

        controller.updateTrack(trackToUpdate, "Titolo Aggiornato", "Autore Aggiornato", "Rock", 2024, 300, "nuovo_file.mp3");

        // Controlliamo che l'oggetto sia mutato
        assertEquals("Titolo Aggiornato", trackToUpdate.getTitle());
        assertEquals("Autore Aggiornato", trackToUpdate.getAuthor());
        assertEquals("Rock", trackToUpdate.getGenre());
        assertEquals(2024, trackToUpdate.getYear());
        assertEquals(300, trackToUpdate.getDuration());
        assertEquals("nuovo_file.mp3", trackToUpdate.getFilePath());
    }

    /**
     * @brief Verifica la sicurezza del metodo di aggiornamento.
     * @details Assicura che il tentativo di aggiornare una traccia nulla
     * scateni un'eccezione dedicata e blocchi l'operazione.
     */
    @Test
    void updateTrack_nullTrack_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.updateTrack(null, "Titolo", "Autore", "Pop", 2020, 200, null)
        );
    }

    /**
     * @brief Verifica l'eliminazione di una traccia dal sistema.
     * @details Inserisce una traccia, verifica che sia presente, poi chiama
     * il metodo di rimozione e si accerta che il catalogo torni a essere vuoto.
     */
    @Test
    void removeTrack_validTrack_removesFromCatalog() {
        controller.addTrack("Traccia da eliminare", "Autore", "Pop", 2020, 200, null);
        assertEquals(1, catalog.getAllTracks().size());

        Track trackToRemove = catalog.getAllTracks().iterator().next();
        controller.removeTrack(trackToRemove);

        assertEquals(0, catalog.getAllTracks().size(), "Il catalogo dovrebbe essere vuoto dopo l'eliminazione");
    }

    /**
     * @brief Verifica il comportamento in caso di rimozione di un riferimento nullo.
     * @details Il metodo non dovrebbe scatenare alcuna eccezione (NullPointerException),
     * ma semplicemente ignorare l'operazione mantenendo intatto il catalogo.
     */
    @Test
    void removeTrack_nullTrack_doesNothing() {
        controller.addTrack("Traccia Safe", "Autore", "Pop", 2020, 200, null);

        assertDoesNotThrow(() -> controller.removeTrack(null));

        assertEquals(1, catalog.getAllTracks().size());
    }

    /**
     * @brief Verifica il recupero di tutte le tracce.
     * @details Controlla che il controller restituisca correttamente l'intera
     * collezione di tracce appoggiandosi al catalogo.
     */
    @Test
    void getAllTracks_returnsAllCatalogTracks() {
        controller.addTrack("Brano 1", "Autore 1", "Pop", 2020, 200, null);
        controller.addTrack("Brano 2", "Autore 2", "Rock", 2021, 210, null);

        Collection<Track> allTracks = controller.getAllTracks();

        assertNotNull(allTracks);
        assertEquals(2, allTracks.size(), "Dovrebbero esserci esattamente 2 tracce");
    }

    /**
     * @brief Verifica il meccanismo di rollback in caso di aggiornamento parzialmente invalido.
     * @details TARGET: Integrità atomica dello stato del Modello.
     * Inserisce una traccia valida e tenta di aggiornarla modificando il titolo (valido)
     * ma inserendo un anno non valido (futuro). Il sistema deve sollevare un'eccezione,
     * bloccare l'aggiornamento sul catalogo e ripristinare tutti i campi originali della traccia.
     */
    @Test
    void updateTrack_partialInvalidData_rollsBackAllChanges() {
        // Precondizione: inseriamo una traccia integra
        controller.addTrack("Starlight", "Muse", "Rock", 2006, 240, "starlight.mp3");
        Track trackToUpdate = catalog.getAllTracks().iterator().next();

        // Azione: tentiamo un aggiornamento misto (titolo OK, ma anno illegale nel 2027)
        int annoInvalido = java.time.Year.now().getValue() + 1;

        assertThrows(IllegalArgumentException.class, () ->
                controller.updateTrack(trackToUpdate, "Nuovo Titolo", "Muse", "Rock", annoInvalido, 240, "starlight.mp3")
        );

        // Verifica post-rollback: i campi dell'oggetto NON devono aver recepito modifiche parziali
        assertEquals("Starlight", trackToUpdate.getTitle(),
                "Il titolo deve essere stato ripristinato al valore originale a causa del rollback.");
        assertEquals(2006, trackToUpdate.getYear(),
                "L'anno deve essere rimasto quello originale.");
    }

    /**
     * @brief Verifica che il metodo di aggiunta rifiuti stringhe vuote o nulle per i campi chiave.
     * @details TARGET: Validazione dell'input e barriere di sicurezza del controller.
     * Controlla che il passaggio di un titolo nullo o composto solo da spazi vuoti venga
     * intercettato dal sistema, propaghi l'eccezione attesa e non sporchi il catalogo centrale.
     */
    @Test
    void addTrack_nullOrEmptyTitle_throwsExceptionAndDoesNotSave() {
        // Controllo con stringa vuota
        assertThrows(IllegalArgumentException.class, () ->
                controller.addTrack("   ", "Autore Valido", "Pop", 2020, 180, null)
        );

        // Controllo con null
        assertThrows(IllegalArgumentException.class, () ->
                controller.addTrack(null, "Autore Valido", "Pop", 2020, 180, null)
        );

        // Il catalogo deve essere rimasto pulito
        assertEquals(0, catalog.getAllTracks().size(),
                "Il catalogo non deve memorizzare tracce con input di testo non validi.");
    }
}