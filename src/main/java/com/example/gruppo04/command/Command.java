package com.example.gruppo04.command;

/**
 * Rappresenta un'operazione eseguibile e reversibile nel Music Playlist Manager.
 * <p>
 * Le implementazioni di questa interfaccia incapsulano una singola azione
 * dell'utente (es. aggiunta o rimozione di una traccia o playlist) insieme
 * alla sua operazione inversa, abilitando il supporto all'undo
 * </p>
 */
public interface Command {

    /**
     * Esegue il comando, applicando la modifica prevista al Model.
     */
    void execute();

    /**
     * Annulla l'effetto di {@link #execute()}, ripristinando il Model
     * allo stato precedente all'esecuzione del comando.
     */
    void undo();
}