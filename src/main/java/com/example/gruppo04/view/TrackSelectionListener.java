package com.example.gruppo04.view;

import com.example.gruppo04.interfaces.Track;

/**
 * Interfaccia funzionale invocata quando l'utente seleziona
 * una traccia nel {@link TrackSelectionViewController}.
 * Chi apre il pannello decide cosa fare con la traccia selezionata,
 * rendendo il pannello riusabile in contesti diversi.
 */
@FunctionalInterface
public interface TrackSelectionListener {

    /**
     * Chiamato quando l'utente conferma la selezione di una traccia.
     *
     * @param track la traccia selezionata dall'utente
     */
    void onTrackSelected(Track track);
}