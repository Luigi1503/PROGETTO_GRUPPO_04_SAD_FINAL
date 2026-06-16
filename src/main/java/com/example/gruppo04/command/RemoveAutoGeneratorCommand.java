package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.model.factory_method.AutoPlaylistGenerator;
import com.example.gruppo04.model.factory_method.AutomaticPlaylistService;

public class RemoveAutoGeneratorCommand implements Command {

    private final AutomaticPlaylistService service;
    private final AutoPlaylistGenerator generator;
    private final MusicCatalog catalog;
    private boolean executed;

    public RemoveAutoGeneratorCommand(
            AutomaticPlaylistService service,
            AutoPlaylistGenerator generator,
            MusicCatalog catalog
    ) {
        this.service = service;
        this.generator = generator;
        this.catalog = catalog;
    }

    public void execute() {
        executed = service.removeGenerator(generator.getCriterionName());
        if (executed) {
            service.refresh(catalog);
        }
    }

    public void undo() {
        if (!executed) {
            return;
        }
        service.addCustomGenerator(generator);
        service.refresh(catalog);
    }

    @Override
    public boolean wasExecuted() {
        return executed;
    }

    @Override
    public void notifyCatalogChanged() {
        if (executed) {
            catalog.notifyPlaylistContentChanged(null);
        }
    }
}
