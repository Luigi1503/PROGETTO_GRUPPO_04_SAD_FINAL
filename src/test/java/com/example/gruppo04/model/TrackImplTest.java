package com.example.gruppo04.model;

import com.example.gruppo04.interfaces.Track;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @file TrackImplTest.java
 * @brief Suite di test per la classe {@link TrackImpl}.
 * @details Verifica che la logica di dominio, i vincoli sui dati e l'identità
 * della traccia vengano gestiti correttamente.
 */
class TrackImplTest {

    /**
     * @brief Verifica la corretta creazione di una traccia con dati validi.
     * @details Controlla che il costruttore istanzi correttamente l'oggetto e che
     * i metodi getter restituiscano i valori attesi, verificando la presenza di un UUID non nullo.
     */
    @Test
    void constructor_validData_createsTrackSuccessfully() {
        Track track = new TrackImpl("Starlight", "Muse", "Rock", 2006, 240, "starlight.mp3");

        assertNotNull(track.getId(), "L'UUID non deve mai essere nullo");
        assertEquals("Starlight", track.getTitle());
        assertEquals("Muse", track.getAuthor());
        assertEquals("Rock", track.getGenre());
        assertEquals(2006, track.getYear());
        assertEquals(240, track.getDuration());
        assertEquals("starlight.mp3", track.getFilePath());
    }

    /**
     * @brief Verifica la validazione del titolo.
     * @details Assicura che l'inserimento di un titolo vuoto o nullo
     * sollevi un'eccezione di tipo {@link IllegalArgumentException}.
     */
    @Test
    void setTitle_emptyOrNull_throwsException() {
        Track track = new TrackImpl("Valid Title", "Valid Author", "Pop", 2000, 180, null);

        assertThrows(IllegalArgumentException.class, () -> track.setTitle(""));
        assertThrows(IllegalArgumentException.class, () -> track.setTitle("   "));
        assertThrows(IllegalArgumentException.class, () -> track.setTitle(null));
    }

    /**
     * @brief Verifica la validazione dell'autore.
     * @details Assicura che l'inserimento di un autore vuoto o nullo
     * sollevi un'eccezione di tipo {@link IllegalArgumentException}.
     */
    @Test
    void setAuthor_emptyOrNull_throwsException() {
        Track track = new TrackImpl("Valid Title", "Valid Author", "Pop", 2000, 180, null);

        assertThrows(IllegalArgumentException.class, () -> track.setAuthor(""));
        assertThrows(IllegalArgumentException.class, () -> track.setAuthor("   "));
        assertThrows(IllegalArgumentException.class, () -> track.setAuthor(null));
    }

    /**
     * @brief Verifica la validazione dell'anno con valori non consentiti.
     * @details Controlla che l'inserimento di un anno precedente al 1900 o
     * successivo all'anno corrente sollevi un'eccezione.
     */
    @Test
    void setYear_invalidYear_throwsException() {
        Track track = new TrackImpl("Valid Title", "Valid Author", "Pop", 2000, 180, null);
        int annoFuturo = Year.now().getValue() + 1;

        assertThrows(IllegalArgumentException.class, () -> track.setYear(1899));
        assertThrows(IllegalArgumentException.class, () -> track.setYear(annoFuturo));
    }

    /**
     * @brief Verifica l'aggiornamento dell'anno con limiti validi.
     * @details Assicura che l'impostazione dell'anno minimo (1900) e dell'anno
     * corrente venga accettata dal sistema senza errori.
     */
    @Test
    void setYear_validYear_updatesSuccessfully() {
        Track track = new TrackImpl("Valid Title", "Valid Author", "Pop", 2000, 180, null);
        int annoCorrente = Year.now().getValue();

        track.setYear(1900); // Limite minimo
        assertEquals(1900, track.getYear());

        track.setYear(annoCorrente); // Limite massimo
        assertEquals(annoCorrente, track.getYear());
    }

    /**
     * @brief Verifica la validazione della durata con valori non consentiti.
     * @details Controlla che l'inserimento di una durata negativa o pari a zero
     * sollevi un'eccezione.
     */
    @Test
    void setDuration_invalidDuration_throwsException() {
        Track track = new TrackImpl("Valid Title", "Valid Author", "Pop", 2000, 180, null);

        assertThrows(IllegalArgumentException.class, () -> track.setDuration(0));
        assertThrows(IllegalArgumentException.class, () -> track.setDuration(-10));
    }

    /**
     * @brief Verifica il contratto di uguaglianza basato sull'UUID.
     * @details Assicura che due istanze diverse di TrackImpl, pur avendo gli
     * stessi parametri testuali, risultino disuguali a causa dei differenti UUID generati.
     */
    @Test
    void equals_differentUUIDs_returnsFalse() {
        Track track1 = new TrackImpl("Same Title", "Same Author", "Pop", 2020, 200, "file.mp3");
        Track track2 = new TrackImpl("Same Title", "Same Author", "Pop", 2020, 200, "file.mp3");

        assertNotEquals(track1, track2, "Le due tracce dovrebbero essere diverse a causa di UUID differenti");
    }

    /**
     * @brief Verifica la coerenza dell'identità dell'oggetto.
     * @details Controlla che un oggetto risulti uguale a se stesso e che la
     * generazione del suo codice hash rimanga costante.
     */
    @Test
    void equals_sameInstance_returnsTrue() {
        Track track = new TrackImpl("Valid Title", "Valid Author", "Pop", 2020, 200, "file.mp3");

        assertEquals(track, track, "Un oggetto deve essere uguale a se stesso");
        assertEquals(track.hashCode(), track.hashCode(), "L'hashcode deve essere costante");
    }

    /**
     * @brief Verifica la creazione di una traccia senza un file audio associato.
     * @details Assicura che il sistema consenta la creazione di tracce puramente testuali
     * passando un filePath nullo o vuoto, senza sollevare eccezioni.
     */
    @Test
    void constructor_nullOrEmptyFilePath_createsTrackSuccessfully() {
        assertDoesNotThrow(() -> new TrackImpl("Valid Title", "Valid Author", "Pop", 2000, 180, null),
                "Dovrebbe essere possibile creare una traccia con filePath null");

        assertDoesNotThrow(() -> new TrackImpl("Valid Title", "Valid Author", "Pop", 2000, 180, ""),
                "Dovrebbe essere possibile creare una traccia con filePath vuoto");
    }


    /**
     * @brief Verifica il corretto incapsulamento della traccia in una lista.
     * @details Controlla l'implementazione del contratto PlayableSource, assicurandosi
     * che il metodo restituisca una lista non nulla contenente unicamente l'istanza
     * della traccia stessa.
     */
    @Test
    void getTracks_returnsSingletonListWithSelf() {
        Track track = new TrackImpl("Valid Title", "Valid Author", "Pop", 2000, 180, "file.mp3");

        List<Track> tracks = track.getTracks();

        assertNotNull(tracks, "La lista restituita non deve essere nulla");
        assertEquals(1, tracks.size(), "La lista deve contenere esattamente un elemento");
        assertEquals(track, tracks.get(0), "L'unico elemento della lista deve essere la traccia stessa");
    }

    /**
     * @brief Verifica la corretta generazione dei metadati e il loro ordine.
     * @details Controlla che il dizionario generato contenga esattamente le quattro
     * chiavi previste ("Titolo", "Autore", "Genere", "Anno") con i valori corretti.
     * Verifica inoltre che l'ordine di iterazione della mappa (garantito dalla LinkedHashMap)
     * sia rigorosamente rispettato per la corretta visualizzazione nella UI.
     */
    @Test
    void getDisplayName_returnsOrderedMetadataMap() {
        Track track = new TrackImpl("Valid Title", "Valid Author", "Pop", 2000, 180, "file.mp3");

        Map<String, String> meta = track.getDisplayName();

        assertNotNull(meta, "La mappa dei metadati non deve essere nulla");
        assertEquals(4, meta.size(), "La mappa deve contenere esattamente 4 elementi");

        // Verifica la correttezza dei valori associati alle chiavi
        assertEquals("Valid Title", meta.get("Titolo"));
        assertEquals("Valid Author", meta.get("Autore"));
        assertEquals("Pop", meta.get("Genere"));
        assertEquals("2000", meta.get("Anno"));

        // Verifica l'ordine di inserimento (Cruciale per la LinkedHashMap)
        java.util.Iterator<String> keyIterator = meta.keySet().iterator();
        assertEquals("Titolo", keyIterator.next(), "La prima chiave deve essere 'Titolo'");
        assertEquals("Autore", keyIterator.next(), "La seconda chiave deve essere 'Autore'");
        assertEquals("Genere", keyIterator.next(), "La terza chiave deve essere 'Genere'");
        assertEquals("Anno", keyIterator.next(), "La quarta chiave deve essere 'Anno'");
    }
}