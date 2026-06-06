package com.example.gruppo04.persistence;

import com.example.gruppo04.interfaces.MusicCatalog;
import com.example.gruppo04.observer.CatalogEvent;
import com.example.gruppo04.observer.CatalogObserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @brief Observer che salva automaticamente il catalogo ad ogni modifica.
 * @details Si registra come ConcreteObserver su {@link MusicCatalog} e
 * delega il salvataggio a {@link PersistenceManager} ad ogni notifica
 * di modifica del catalogo, garantendo la persistenza dello stato
 * tra gli accessi all'applicazione.
 * Integra il pattern Observer già esistente con il pattern Singleton
 * di {@link PersistenceManager}.
 */
public class AutoSaveObserver implements CatalogObserver {

    /** @brief Istanza del gestore della persistenza. */
    private final PersistenceManager pm = PersistenceManager.getInstance();

    /** @brief Catalogo musicale da salvare ad ogni modifica. */
    private final MusicCatalog catalog;

    /** @brief Logger per la gestione degli errori di salvataggio. */
    private static final Logger logger =
            Logger.getLogger(AutoSaveObserver.class.getName());

    /**
     * @brief Costruisce un {@code AutoSaveObserver} per il catalogo fornito.
     *
     * @param catalog il catalogo musicale da osservare e salvare; non deve essere null
     */
    public AutoSaveObserver(MusicCatalog catalog) {
        this.catalog = catalog;
    }

    /**
     * @brief Chiamato dal catalogo quando si verifica un cambiamento.
     * @details Salva automaticamente lo stato corrente del catalogo
     * su file ad ogni notifica di modifica, indipendentemente dal tipo
     * di evento ricevuto.
     *
     * @param event l'evento contenente i dettagli del cambiamento
     */
    @Override
    public void onCatalogChanged(CatalogEvent event) {
        try {
            pm.save(catalog);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errore nel salvataggio automatico del catalogo", e);
        }
    }
}