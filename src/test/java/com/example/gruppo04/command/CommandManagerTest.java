package com.example.gruppo04.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class CommandManagerTest
 * @brief Unit test per {@link CommandManager} — Task T45.
 *
 * Copre:
 * - Esecuzione dei comandi e inserimento nello stack
 * - Verifica di canUndo()
 * - Sequenze di undo multiple (politica LIFO)
 * - Gestione dei comandi falliti (wasExecuted() = false)
 */
class CommandManagerTest {

    /**
     * @class StubCommand
     * @brief Implementazione fittizia di {@link Command} usata per i test.
     *
     * Permette di simulare sia l'esecuzione riuscita che quella fallita,
     * tracciando lo stato interno (eseguito, annullato, catalogo notificato)
     * per le verifiche nei test.
     */
    private static class StubCommand implements Command {
        boolean executed = false;
        boolean undone = false;
        boolean catalogNotified = false;
        boolean simulateSuccess = true;

        /** @brief Simula l'esecuzione del comando impostando il flag executed. */
        @Override public void execute() { executed = true; }

        /** @brief Simula l'annullamento del comando impostando il flag undone. */
        @Override public void undo() { undone = true; }

        /** @brief Restituisce l'esito simulato dell'esecuzione. */
        @Override public boolean wasExecuted() { return simulateSuccess; }

        /** @brief Simula la notifica al catalogo impostando il flag catalogNotified. */
        @Override public void notifyCatalogChanged() { catalogNotified = true; }
    }

    private CommandManager manager;
    private boolean listenerCalled;
    private boolean lastCanUndoState;

    /**
     * @brief Inizializza il fixture di test prima di ogni esecuzione.
     *
     * Crea una nuova istanza di {@link CommandManager} e registra un listener
     * su canUndo per tracciare le notifiche di cambiamento di stato.
     */
    @BeforeEach
    void setUp() {
        manager = new CommandManager();
        listenerCalled = false;
        manager.addCanUndoListener(canUndo -> {
            listenerCalled = true;
            lastCanUndoState = canUndo;
        });
    }

    /**
     * @brief Verifica che l'esecuzione riuscita di un comando lo inserisca nello stack.
     *
     * Controlla che il comando venga eseguito, che il catalogo venga notificato,
     * che canUndo() diventi true e che il listener venga avvisato con stato true.
     */
    @Test
    void executeCommand_success_pushesToStack() {
        StubCommand cmd = new StubCommand();
        cmd.simulateSuccess = true;
        manager.executeCommand(cmd);
        assertTrue(cmd.executed, "Il comando deve essere eseguito");
        assertTrue(cmd.catalogNotified, "Il catalogo deve essere notificato");
        assertTrue(manager.canUndo(), "Lo stack non deve essere vuoto");
        assertTrue(listenerCalled, "Il listener deve essere avvisato");
        assertTrue(lastCanUndoState, "Lo stato del listener deve essere true");
    }

    /**
     * @brief Verifica che un'esecuzione fallita non venga inserita nello stack.
     *
     * Controlla che il comando venga comunque eseguito, ma che il catalogo
     * non venga notificato e che lo stack rimanga vuoto (canUndo() = false).
     */
    @Test
    void executeCommand_failedExecution_doesNotPushToStack() {
        StubCommand cmd = new StubCommand();
        cmd.simulateSuccess = false;
        manager.executeCommand(cmd);
        assertTrue(cmd.executed);
        assertFalse(cmd.catalogNotified, "Se fallisce non notifica il catalogo");
        assertFalse(manager.canUndo(), "Lo stack deve restare vuoto");
    }

    /**
     * @brief Verifica che undo() estragga il comando dallo stack e invochi il suo undo().
     *
     * Controlla che dopo l'undo il metodo undo() del comando sia stato chiamato,
     * che lo stack risulti vuoto e che il listener venga notificato con stato false.
     */
    @Test
    void undo_popsFromStackAndCallsUndo() {
        StubCommand cmd = new StubCommand();
        manager.executeCommand(cmd);
        listenerCalled = false;
        manager.undo();
        assertTrue(cmd.undone, "Il metodo undo() del comando deve essere invocato");
        assertFalse(manager.canUndo(), "Lo stack ora deve essere vuoto");
        assertTrue(listenerCalled);
        assertFalse(lastCanUndoState, "Il listener riceve false perché lo stack è vuoto");
    }

    /**
     * @brief Verifica che sequenze multiple di undo rispettino la politica LIFO.
     *
     * Esegue due comandi in sequenza e controlla che il primo undo annulli
     * il secondo comando (l'ultimo inserito) e che il secondo undo annulli
     * infine il primo comando, lasciando lo stack vuoto.
     */
    @Test
    void undo_multipleSequences_respectsLIFO() {
        StubCommand cmd1 = new StubCommand();
        StubCommand cmd2 = new StubCommand();
        manager.executeCommand(cmd1);
        manager.executeCommand(cmd2);
        assertTrue(manager.canUndo());
        manager.undo();
        assertTrue(cmd2.undone, "Il secondo comando (ultimo inserito) deve essere annullato per primo");
        assertFalse(cmd1.undone, "Il primo comando non deve essere ancora annullato");
        assertTrue(manager.canUndo());
        manager.undo();
        assertTrue(cmd1.undone, "Ora anche il primo comando deve essere annullato");
        assertFalse(manager.canUndo());
    }
}