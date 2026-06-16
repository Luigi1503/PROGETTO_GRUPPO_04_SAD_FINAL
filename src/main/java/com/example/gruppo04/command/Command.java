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

    /**
     * Indica se l'ultima {@link #execute()} ha effettivamente prodotto una
     * modifica al Model. I comandi che possono fallire silenziosamente (es. nome
     * playlist duplicato, traccia già presente) sovrascrivono questo metodo: in
     * caso di esito negativo il comando non viene registrato per l'undo.
     *
     * @return {@code true} se il comando ha avuto effetto (default), {@code false} altrimenti
     */
    default boolean wasExecuted() {
        return true;
    }
}