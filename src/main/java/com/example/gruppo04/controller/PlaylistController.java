package com.example.gruppo04.controller;

import com.example.gruppo04.command.*;
import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.interfaces.Playlist;
import com.example.gruppo04.interfaces.Track;

/**
 * Controller (MVC) per la gestione delle playlist.
 * Fa da tramite tra la View e il {@link MusicCatalog}: riceve le richieste
 * dell'utente, le delega al catalogo e restituisce l'esito, così la View può
 * dare un riscontro (es. un banner successo/errore).
 * Non possiede stato di dominio e non aggiorna l'interfaccia: il refresh dei
 * pannelli avviene tramite gli eventi Observer emessi dal catalogo.
 */
public class PlaylistController {
    private final MusicCatalog catalog;

    private final CommandManager managerPlaylist;       // Per creare/eliminare playlist
    private final CommandManager managerTrackPlaylist; // Per aggiungere/togliere brani dalla playlist

    /**
     * @param catalog il catalogo a cui delegare le operazioni; iniettato dal
     *                composition root (la MainWindow)
     */
    public PlaylistController(MusicCatalog catalog) {
        this.catalog = catalog;
        this.managerPlaylist = new CommandManager();
        this.managerTrackPlaylist = new CommandManager();
    }

    /**
     * Richiede la creazione di una nuova playlist, delegando a
     * {@link MusicCatalog#createPlaylist(String)} (che esegue controlli e notifica).
     *
     * @param name il nome della playlist, così come inserito nella View
     * @return {@code true} se creata, {@code false} se il nome è già in uso;
     *         valore con cui la View mostra il riscontro
     */
    public boolean createPlaylist(String name) {
        AddPlaylistCommand cmd = new AddPlaylistCommand(catalog, name);
        managerPlaylist.executeCommand(cmd);
        return cmd.wasExecuted();
    }

    /**
     * Richiede la rinomina di una playlist esistente, delegando a
     * {@link MusicCatalog#renamePlaylist(Playlist, String)}.
     *
     * @param playlist  la playlist selezionata nella View
     * @param newName il nuovo nome inserito dall'utente
     * @return {@code true} se rinominata, {@code false} se il nome è già in uso
     *         o la playlist non è nel catalogo
     */
    public boolean renamePlaylist(Playlist playlist, String newName) {
        return catalog.renamePlaylist(playlist, newName);
    }

    /**
     * Richiede la rimozione di una playlist, delegando a
     * {@link MusicCatalog#deletePlaylist(Playlist)}.
     *
     * @param playlist la playlist selezionata nella View
     * @return {@code true} se rimossa, {@code false} se non presente
     */
    public boolean deletePlaylist(Playlist playlist) {
        RemovePlaylistCommand cmd = new RemovePlaylistCommand(playlist, catalog);
        managerPlaylist.executeCommand(cmd);
        return cmd.wasExecuted();
    }

    /**
     * Aggiunge una traccia ad una playlist esistente, delegando
     * {@link MusicCatalog#addTrackToPlaylist(Playlist, Track)}.
     *
     * @param playlist la playlist selezionata nella View
     * @param track    la traccia da aggiungere
     * @return {@code true} se aggiunta, {@code false} se la traccia è già
     *         presente nella playlist
     */
    public boolean addTrackToPlaylist(Playlist playlist, Track track) {
        AddTrackToPlaylistCommand cmd = new AddTrackToPlaylistCommand(track, catalog, playlist);
        managerTrackPlaylist.executeCommand(cmd);
        return cmd.wasExecuted();
    }

    /**
     * Rimuove una traccia da una playlist esistente, delegando
     * {@link MusicCatalog#removeTrackFromPlaylist(Playlist, Track)}.
     *
     * @param playlist la playlist selezionata nella View
     * @param track    la traccia da rimuovere
     * @return {@code true} se rimossa, {@code false} se la traccia non è
     *         presente nella playlist
     */
    public boolean removeTrackFromPlaylist(Playlist playlist, Track track) {
        RemoveTrackFromPlaylistCommand cmd = new RemoveTrackFromPlaylistCommand(playlist, track, catalog);
        managerTrackPlaylist.executeCommand(cmd);
        return cmd.wasExecuted();
    }

    /**
     * @brief Restituisce il command manager dedicato alla gestione delle playlist.
     * @return L'istanza di {@link CommandManager} per le operazioni sulle playlist.
     */
    public CommandManager getManagerPlaylist() {
        return this.managerPlaylist;
    }

    /**
     * @brief Restituisce il command manager dedicato alla gestione delle tracce nelle playlist.
     * @return L'istanza di {@link CommandManager} per le operazioni sulle tracce all'interno delle playlist.
     */
    public CommandManager getManagerTrackPlaylist() {
        return this.managerTrackPlaylist;
    }

    /**
     * Sposta una traccia all'interno di una playlist, delegando a
     * {@link MusicCatalog#moveTrackInPlaylist(Playlist, int, int)}.
     * L'operazione è registrata nel CommandManager e supporta l'undo.
     *
     * @param playlist la playlist selezionata nella View
     * @param from     indice di partenza della traccia
     * @param to       indice di destinazione della traccia
     */
    public void moveTrackInPlaylist(Playlist playlist, int from, int to) {
        Command cmd = new MoveTrackInPlaylistCommand(playlist, from, to, catalog);
        managerTrackPlaylist.executeCommand(cmd);
    }
}
