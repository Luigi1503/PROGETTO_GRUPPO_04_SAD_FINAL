package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.model.factory_method.AutoPlaylistGenerator;
import com.example.gruppo04.model.factory_method.AutomaticPlaylistService;

/**
 * @class RemoveAutoGeneratorCommand
 * @brief Comando per la rimozione di un generatore automatico di playlist.
 *
 * Implementa il pattern Command per rimuovere un {@link AutoPlaylistGenerator}
 * precedentemente registrato presso l'{@link AutomaticPlaylistService}, permettendo
 * l'esecuzione e l'eventuale annullamento dell'operazione in modo reversibile.
 */
public class RemoveAutoGeneratorCommand implements Command {

    private final AutomaticPlaylistService service;
    private final AutoPlaylistGenerator generator;
    private final MusicCatalog catalog;
    private boolean executed;

    /**
     * @brief Costruisce il comando per la rimozione di un generatore automatico.
     * @param service Il servizio responsabile della gestione dei generatori automatici.
     * @param generator Il generatore di playlist automatiche da rimuovere.
     * @param catalog Il catalogo musicale da aggiornare e notificare.
     */
    public RemoveAutoGeneratorCommand(
            AutomaticPlaylistService service,
            AutoPlaylistGenerator generator,
            MusicCatalog catalog
    ) {
        this.service = service;
        this.generator = generator;
        this.catalog = catalog;
    }

    /**
     * @brief Esegue il comando rimuovendo il generatore identificato dal nome del criterio.
     *
     * Imposta il flag executed in base all'esito della rimozione; se la
     * rimozione ha successo, forza l'aggiornamento delle playlist generate
     * in base al catalogo corrente.
     */
    public void execute() {
        executed = service.removeGenerator(generator.getCriterionName());
        if (executed) {
            service.refresh(catalog);
        }
    }

    /**
     * @brief Annulla l'esecuzione del comando ripristinando il generatore rimosso.
     *
     * Non ha effetto se il comando non è stato eseguito con successo.
     * In caso contrario, riaggiunge il generatore al servizio e forza
     * l'aggiornamento delle playlist generate.
     */
    public void undo() {
        if (!executed) {
            return;
        }
        service.addCustomGenerator(generator);
        service.refresh(catalog);
    }

    /**
     * @brief Indica se il comando è stato eseguito con successo.
     * @return true se la rimozione del generatore è andata a buon fine, false altrimenti.
     */
    @Override
    public boolean wasExecuted() {
        return executed;
    }

    /**
     * @brief Notifica al catalogo che il contenuto delle playlist è cambiato.
     *
     * La notifica viene inviata solo se il comando è stato eseguito con successo.
     */
    @Override
    public void notifyCatalogChanged() {
        if (executed) {
            catalog.notifyPlaylistContentChanged(null);
        }
    }
}