package com.example.gruppo04.observer;

/**
 * Rappresenta i tipi di eventi di dominio che possono verificarsi all'interno del catalogo.
 * Permette agli observer di filtrare le notifiche ricevute in base alla loro granularità.
 */
public enum CatalogEventType {
    /** Rilasciato quando una nuova traccia viene inserita con successo nel catalogo. */
    TRACK_ADDED,
    /** Rilasciato quando una traccia viene definitivamente rimossa dal catalogo (e dalle playlist). */
    TRACK_REMOVED,
    /** Rilasciato quando le proprietà di una traccia esistente vengono modificate. */
    TRACK_UPDATED,
    /** Rilasciato quando una nuova playlist viene creata all'interno del catalogo. */
    PLAYLIST_ADDED,
    /** Rilasciato quando una playlist viene eliminata dal catalogo. */
    PLAYLIST_REMOVED,
    /** Rilasciato quando il nome di una playlist esistente viene modificato con successo. */
    PLAYLIST_RENAMED,
    /** Rilasciato quando vengono aggiunte o rimosse tracce da una specifica playlist. */
    PLAYLIST_CONTENT_CHANGED,
    /** Rilasciato quando l'ordine interno delle tracce all'interno di una playlist viene modificato. */
    PLAYLIST_REORDERED,
    /** Rilasciato quando una nuova traccia viene aggiunta alla playlist */
    PLAYLIST_TRACK_ADDED,
    /** Rilasciato quando una traccia viene rimossa dalla playlist */
    PLAYLIST_TRACK_REMOVED,
    /** Rilasciato quando la strategia di riproduzione viene modificata **/
    STRATEGY_CHANGED,
    /**
     * Rilasciato quando viene avviata una nuova riproduzione tramite {@code PlaybackController.play()}.
     * Il target dell'evento è un {@code PlaybackStartedPayload} contenente la traccia corrente
     * e un flag che indica se la sorgente è una playlist (true) o una traccia singola (false).
     */
    PLAYBACK_STARTED,

    /** Rilasciato quando la traccia in riproduzione cambia (skip, fine traccia, previous). */
    TRACK_CHANGED,

    /** Rilasciato quando la sorgente in riproduzione cambia (skip, fine traccia, previous). */
    SOURCE_CHANGED,

    /** Rilasciato quando la riproduzione viene fermata del tutto (stop/fine coda). */
    PLAYBACK_STOPPED,
}
