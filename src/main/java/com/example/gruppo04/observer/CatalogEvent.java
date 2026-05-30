package com.example.gruppo04.observer;

/**
 * Incapsula le informazioni relative a un cambiamento di stato nel catalogo musicale.
 * Gli eventi sono fortemente tipizzati per evitare cast insicuri a runtime.
 */
public class CatalogEvent {
    private final CatalogEventType type;
    private final Object source;
    private final Object target;

    /**
     * Costruisce un nuovo evento di catalogo.
     *
     * @param type   il tipo di evento di dominio che si è verificato
     * @param source la sorgente che ha generato l'evento (tipicamente l'istanza di MusicCatalog)
     * @param target l'oggetto specifico del dominio che è stato modificato (es. la traccia o la playlist)
     */
    public CatalogEvent(CatalogEventType type, Object source, Object target) {
        this.type = type;
        this.source = source;
        this.target = target;
    }

    /**
     * Restituisce la tipologia specifica dell'evento di dominio.
     *
     * @return il tipo di evento
     */
    public CatalogEventType getType() { return type; }

    /**
     * Restituisce l'oggetto sorgente che ha originato la notifica.
     *
     * @return il soggetto (MusicCatalog) che ha subito la mutazione
     */
    public Object getSource() { return source; }

    /**
     * Restituisce l'entità di dominio specifica colpita dal cambiamento.
     *
     * @return l'oggetto modificato, da sottoporre a cast sicuro in base al {@link CatalogEventType}
     */
    public Object getTarget() { return target; }
}
