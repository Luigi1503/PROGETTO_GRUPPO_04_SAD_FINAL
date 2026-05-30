package com.example.gruppo04.view;

import com.example.gruppo04.model.Track;

/**
 * Interfaccia funzionale callback invocata quando l'utente seleziona
 * una traccia nel {@link TrackSelectionViewController}.
 * Chi apre il dialog decide cosa fare con la traccia selezionata,
 * rendendo il dialog riusabile in contesti diversi.
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