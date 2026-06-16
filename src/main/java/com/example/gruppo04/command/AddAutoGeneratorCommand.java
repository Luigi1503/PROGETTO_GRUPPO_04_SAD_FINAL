package com.example.gruppo04.command;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.model.factory_method.AutoPlaylistGenerator;
import com.example.gruppo04.model.factory_method.AutomaticPlaylistService;

public class AddAutoGeneratorCommand implements Command {

    private final AutomaticPlaylistService service;
    private final AutoPlaylistGenerator generator;
    private final MusicCatalog catalog;

    public AddAutoGeneratorCommand(
            AutomaticPlaylistService service,
            AutoPlaylistGenerator generator,
            MusicCatalog catalog
    ) {
        this.service = service;
        this.generator = generator;
        this.catalog = catalog;
    }

    @Override
    public void execute() {
        service.addCustomGenerator(generator);
        service.refresh(catalog);
    }

    @Override
    public void undo() {
        service.removeGenerator(generator.getCriterionName());
        service.refresh(catalog);
    }

    @Override
    public void notifyCatalogChanged() {
        catalog.notifyPlaylistContentChanged(null);
    }
}
