package com.example.gruppo04.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Gestisce l'esecuzione dei comandi e lo storico per l'undo.
 * <p>
 * È una classe di dominio: <b>non dipende da JavaFX</b> né da alcun toolkit UI.
 * Per segnalare alla View i cambi di disponibilità dell'undo espone un listener
 * neutro ({@link CanUndoListener}), che la View adatta al proprio meccanismo
 * (es. abilitare/disabilitare un bottone). Questo rispetta il layering
 * (dominio → nessuna dipendenza verso la UI) e rende la classe testabile
 * fuori dal thread JavaFX.
 * </p>
 */
public class CommandManager {

    /**
     * Listener neutro notificato quando cambia la disponibilità dell'operazione
     * di undo. La View vi si aggancia per aggiornare i propri controlli.
     */
    @FunctionalInterface
    public interface CanUndoListener {
        /**
         * @param canUndo {@code true} se è presente almeno un comando annullabile
         */
        void onCanUndoChanged(boolean canUndo);
    }

    private final Stack<Command> stack = new Stack<>();
    private final List<CanUndoListener> listeners = new ArrayList<>();

    /**
     * Esegue il comando e, se ha avuto effetto, lo registra per l'undo.
     * I comandi senza effetto (vedi {@link Command#wasExecuted()}) non vengono
     * impilati, così l'undo non si abilita per operazioni andate a vuoto.
     *
     * @param cmd il comando da eseguire
     */
    public void executeCommand(Command cmd) {
        if (cmd != null) {
            cmd.execute();
            if (cmd.wasExecuted()) {
                stack.push(cmd);
                fireCanUndoChanged();
            }
        }
    }

    /**
     * Annulla l'ultimo comando registrato, se presente.
     */
    public void undo() {
        if (!stack.isEmpty()) {
            Command command = stack.pop();
            command.undo();
            fireCanUndoChanged();
        }
    }

    /**
     * @return {@code true} se è presente almeno un comando annullabile
     */
    public boolean canUndo() {
        return !stack.isEmpty();
    }

    /**
     * Registra un listener per i cambi di disponibilità dell'undo.
     *
     * @param listener l'ascoltatore da aggiungere; ignorato se {@code null} o già presente
     */
    public void addCanUndoListener(CanUndoListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Rimuove un listener precedentemente registrato.
     *
     * @param listener l'ascoltatore da rimuovere
     */
    public void removeCanUndoListener(CanUndoListener listener) {
        listeners.remove(listener);
    }

    private void fireCanUndoChanged() {
        boolean canUndo = canUndo();
        for (CanUndoListener l : new ArrayList<>(listeners)) {
            l.onCanUndoChanged(canUndo);
        }
    }
}