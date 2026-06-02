package com.example.gruppo04.model;

import com.example.gruppo04.interfaces.Track;
import org.junit.jupiter.api.Test;

import java.time.Year;

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
}