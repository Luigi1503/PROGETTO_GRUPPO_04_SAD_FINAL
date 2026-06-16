package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;
import com.example.gruppo04.model.PlaylistImpl;
import com.example.gruppo04.model.TrackImpl;
import com.example.gruppo04.model.factory_method.AutoPlaylistGenerator;
import com.example.gruppo04.model.factory_method.AutomaticPlaylistService;
import com.example.gruppo04.model.factory_method.GenrePlaylistGenerator;
import com.example.gruppo04.observer.ConcreteMusicCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class CommandTest
 * @brief Unit test per le implementazioni concrete di {@link Command}.
 *
 * Verifica il comportamento di execute() e undo() per tutte le operazioni
 * del catalogo musicale (Playlist, Tracce e Autogeneratori).
 */
class CommandTest {


    private MusicCatalog catalog;
    private Track track1;
    private Track track2;
    private Playlist playlist;

    /**
     * @brief Inizializza il fixture di test prima di ogni esecuzione.
     *
     * Resetta il catalogo musicale (singleton), crea due tracce di esempio
     * e una playlist, e popola il catalogo con questi dati.
     */
    @BeforeEach
    void setUp() {
        catalog = ConcreteMusicCatalog.getInstance();
        ((ConcreteMusicCatalog) catalog).reset();

        track1 = new TrackImpl("Hold back the river", "James Bay", "Rock", 2014, 354, "holdBack.mp3");
        track2 = new TrackImpl("Someday", "OneRepublic", "Pop", 2021, 391, "someDay.mp3");
        playlist = new PlaylistImpl("Rock Classics");

        catalog.addTrack(track1);
        catalog.addTrack(track2);

        catalog.addPlaylistAt(0, playlist);
    }



    /**
     * @brief Verifica execute() e undo() di {@link AddPlaylistCommand}.
     *
     * Controlla che l'esecuzione aggiunga una nuova playlist al catalogo
     * e che l'undo la rimuova correttamente, ripristinando lo stato iniziale.
     */
    @Test
    void addPlaylistCommand_executeAndUndo() {
        AddPlaylistCommand cmd = new AddPlaylistCommand(catalog, "Nuova Playlist");

        cmd.execute();
        assertTrue(cmd.wasExecuted());
        assertEquals(2, catalog.getPlaylists().size());
        assertEquals("Nuova Playlist", catalog.getPlaylists().get(1).getName());

        cmd.undo();
        assertEquals(1, catalog.getPlaylists().size());
    }


    /**
     * @brief Verifica execute() e undo() di {@link RemovePlaylistCommand}.
     *
     * Controlla che l'esecuzione rimuova la playlist dal catalogo
     * e che l'undo la ripristini correttamente.
     */
    @Test
    void removePlaylistCommand_executeAndUndo() {
        RemovePlaylistCommand cmd = new RemovePlaylistCommand(playlist, catalog);

        cmd.execute();
        assertTrue(cmd.wasExecuted());
        assertEquals(0, catalog.getPlaylists().size());

        cmd.undo();
        assertEquals(1, catalog.getPlaylists().size());
        assertTrue(catalog.getPlaylists().contains(playlist));
    }


    /**
     * @brief Verifica execute() e undo() di {@link AddTrackCommand}.
     *
     * Controlla che l'esecuzione aggiunga la nuova traccia al catalogo
     * e che l'undo la rimuova correttamente.
     */
    @Test
    void addTrackCommand_executeAndUndo() {
        Track track3 = new TrackImpl("Nuova", "Autore", "Pop", 2023, 200, "nuova.mp3");
        AddTrackCommand cmd = new AddTrackCommand(track3, catalog);

        cmd.execute();
        assertTrue(catalog.getAllTracks().contains(track3));

        cmd.undo();
        assertFalse(catalog.getAllTracks().contains(track3));
    }


    /**
     * @brief Verifica execute() e undo() di {@link RemoveTrackCommand} con effetto a cascata sulla playlist.
     *
     * Controlla che la rimozione di una traccia dal catalogo la elimini
     * anche da tutte le playlist che la contengono, e che l'undo
     * ripristini sia il catalogo che la playlist allo stato originale.
     */
    @Test
    void removeTrackCommand_executeAndUndo_cascadesToPlaylist() {
        catalog.addTrackToPlaylist(playlist, track1);

        RemoveTrackCommand cmd = new RemoveTrackCommand(track1, catalog);

        cmd.execute();
        assertFalse(catalog.getAllTracks().contains(track1));
        assertFalse(playlist.getTracks().contains(track1));

        cmd.undo();
        assertTrue(catalog.getAllTracks().contains(track1));
        assertTrue(playlist.getTracks().contains(track1));
    }


    /**
     * @brief Verifica execute() e undo() di {@link AddTrackToPlaylistCommand}.
     *
     * Controlla che l'esecuzione aggiunga la traccia alla playlist
     * e che l'undo la rimuova correttamente.
     */
    @Test
    void addTrackToPlaylistCommand_executeAndUndo() {
        AddTrackToPlaylistCommand cmd = new AddTrackToPlaylistCommand(track1, catalog, playlist);

        cmd.execute();
        assertTrue(cmd.wasExecuted());
        assertTrue(playlist.getTracks().contains(track1));

        cmd.undo();
        assertFalse(playlist.getTracks().contains(track1));
    }


    /**
     * @brief Verifica execute() e undo() di {@link RemoveTrackFromPlaylistCommand}.
     *
     * Controlla che l'esecuzione rimuova la traccia dalla playlist
     * e che l'undo la riaggiunga correttamente.
     */
    @Test
    void removeTrackFromPlaylistCommand_executeAndUndo() {
        catalog.addTrackToPlaylist(playlist, track2);
        RemoveTrackFromPlaylistCommand cmd = new RemoveTrackFromPlaylistCommand(playlist, track2, catalog);

        cmd.execute();
        assertTrue(cmd.wasExecuted());
        assertFalse(playlist.getTracks().contains(track2));

        cmd.undo();
        assertTrue(playlist.getTracks().contains(track2));
    }


    /**
     * @brief Verifica execute() e undo() di {@link MoveTrackInPlaylistCommand}.
     *
     * Controlla che l'esecuzione scambi correttamente la posizione di due
     * tracce all'interno della playlist e che l'undo ripristini l'ordine originale.
     */
    @Test
    void moveTrackInPlaylistCommand_executeAndUndo() {
        catalog.addTrackToPlaylist(playlist, track1);
        catalog.addTrackToPlaylist(playlist, track2);

        MoveTrackInPlaylistCommand cmd = new MoveTrackInPlaylistCommand(playlist, 0, 1, catalog);

        cmd.execute();
        assertEquals(track2, playlist.getTracks().get(0));
        assertEquals(track1, playlist.getTracks().get(1));

        cmd.undo();
        assertEquals(track1, playlist.getTracks().get(0));
        assertEquals(track2, playlist.getTracks().get(1));
    }


    /**
     * @brief Verifica execute() e undo() di {@link AddAutoGeneratorCommand}.
     *
     * Controlla che l'aggiunta di un generatore automatico di playlist
     * (basato su genere) e il relativo annullamento non generino eccezioni.
     */
    @Test
    void addAutoGeneratorCommand_executeAndUndo() {
        AutomaticPlaylistService service = AutomaticPlaylistService.getInstance();
        AutoPlaylistGenerator generator = new GenrePlaylistGenerator("Rock");

        AddAutoGeneratorCommand cmd = new AddAutoGeneratorCommand(service, generator, catalog);

        assertDoesNotThrow(cmd::execute);
        assertDoesNotThrow(cmd::undo);
    }

    /**
     * @brief Verifica execute() e undo() di {@link RemoveAutoGeneratorCommand}.
     *
     * Controlla che la rimozione di un generatore automatico personalizzato
     * precedentemente registrato avvenga correttamente e che l'undo
     * non generi eccezioni.
     */
    @Test
    void removeAutoGeneratorCommand_executeAndUndo() {
        AutomaticPlaylistService service = AutomaticPlaylistService.getInstance();
        AutoPlaylistGenerator generator = new GenrePlaylistGenerator("Rock");

        service.addCustomGenerator(generator);

        RemoveAutoGeneratorCommand cmd = new RemoveAutoGeneratorCommand(service, generator, catalog);

        cmd.execute();
        assertTrue(cmd.wasExecuted());

        assertDoesNotThrow(cmd::undo);
    }
}