package com.example.gruppo04.observer;

/**
 * Interfaccia che deve essere implementata da qualsiasi componente (View o Controller)
 * che necessiti di reagire in tempo reale ai cambiamenti del catalogo musicale.
 * Rappresenta il ruolo di "Observer" nel rispettivo design pattern.
 */
public interface CatalogObserver {

    /**
     * Metodo di callback invocato dal Subject quando si verifica una mutazione del modello.
     *
     * @param event l'evento contenente tutti i dettagli del cambiamento strutturale
     */
    void onCatalogChanged(CatalogEvent event);
}
