package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.model.factory_method.AutoPlaylistGenerator;
import com.example.gruppo04.model.factory_method.AutomaticPlaylistService;

/**
 * @class AddAutoGeneratorCommand
 * @brief Comando per l'aggiunta di un generatore automatico di playlist.
 *
 * Implementa il pattern Command per registrare un nuovo {@link AutoPlaylistGenerator}
 * presso l'{@link AutomaticPlaylistService}, permettendo l'esecuzione e
 * l'annullamento dell'operazione in modo reversibile.
 */
public class AddAutoGeneratorCommand implements Command {

    private final AutomaticPlaylistService service;
    private final AutoPlaylistGenerator generator;
    private final MusicCatalog catalog;

    /**
     * @brief Costruisce il comando per l'aggiunta di un generatore automatico.
     * @param service Il servizio responsabile della gestione dei generatori automatici.
     * @param generator Il generatore di playlist automatiche da aggiungere.
     * @param catalog Il catalogo musicale da aggiornare e notificare.
     */
    public AddAutoGeneratorCommand(
            AutomaticPlaylistService service,
            AutoPlaylistGenerator generator,
            MusicCatalog catalog
    ) {
        this.service = service;
        this.generator = generator;
        this.catalog = catalog;
    }

    /**
     * @brief Esegue il comando registrando il generatore personalizzato nel servizio.
     *
     * Aggiunge il generatore al servizio di playlist automatiche e forza
     * l'aggiornamento delle playlist generate in base al catalogo corrente.
     */
    @Override
    public void execute() {
        service.addCustomGenerator(generator);
        service.refresh(catalog);
    }

    /**
     * @brief Annulla l'esecuzione del comando rimuovendo il generatore registrato.
     *
     * Rimuove il generatore dal servizio identificandolo tramite il nome del
     * criterio e forza l'aggiornamento delle playlist generate.
     */
    @Override
    public void undo() {
        service.removeGenerator(generator.getCriterionName());
        service.refresh(catalog);
    }

    /**
     * @brief Notifica al catalogo che il contenuto delle playlist è cambiato.
     */
    @Override
    public void notifyCatalogChanged() {
        catalog.notifyPlaylistContentChanged(null);
    }
}