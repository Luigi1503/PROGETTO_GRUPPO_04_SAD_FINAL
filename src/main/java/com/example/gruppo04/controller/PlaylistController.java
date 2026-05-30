package com.example.gruppo04.controller;

import com.example.gruppo04.model.MusicCatalog;
import com.example.gruppo04.model.Playlist;
import com.example.gruppo04.model.PlaylistImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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

    /**
     * @param catalog il catalogo a cui delegare le operazioni; iniettato dal
     *                composition root (la MainWindow)
     */
    public PlaylistController(MusicCatalog catalog) {
        this.catalog = catalog;
    }

    /**
     * Richiede la creazione di una nuova playlist, delegando a
     * {@link MusicCatalog#createPlaylist(String)} (che esegue controlli e notifica).
     *
     * @param nome il nome della playlist, così come inserito nella View
     * @return {@code true} se creata, {@code false} se il nome è già in uso;
     *         valore con cui la View mostra il riscontro
     */
    public boolean createPlaylist(String nome) {
        return catalog.createPlaylist(nome);
    }

    /**
     * Richiede la rinomina di una playlist esistente, delegando a
     * {@link MusicCatalog#renamePlaylist(Playlist, String)}.
     *
     * @param playlist  la playlist selezionata nella View
     * @param nuovoNome il nuovo nome inserito dall'utente
     * @return {@code true} se rinominata, {@code false} se il nome è già in uso
     *         o la playlist non è nel catalogo
     */
    public boolean renamePlaylist(Playlist playlist, String nuovoNome) {
        return catalog.renamePlaylist(playlist, nuovoNome);
    }

    /**
     * Richiede la rimozione di una playlist, delegando a
     * {@link MusicCatalog#deletePlaylist(Playlist)}.
     *
     * @param playlist la playlist selezionata nella View
     * @return {@code true} se rimossa, {@code false} se non presente
     */
    public boolean deletePlaylist(Playlist playlist) {
        return catalog.deletePlaylist(playlist);
    }
}
