package com.example.gruppo04.model.strategy;

import com.example.gruppo04.interfaces.PlayableSource;
import com.example.gruppo04.interfaces.Track;

import java.util.List;

/**
 * @brief Interfaccia per la strategia di riproduzione dei brani musicali.
 *
 * Definisce il contratto per la selezione della sorgente successiva
 * all'interno di una lista di sorgenti riproducibili, in base alla
 * strategia scelta.
 */
public interface PlaybackStrategy {

    /**
     * @brief Seleziona la sorgente successiva nella lista.
     *
     * @param sources la lista di {@link PlayableSource} disponibili.
     * @param currentIndex l'indice della sorgente corrente nella lista.
     * @return la sorgente successiva selezionata, o {@code null} se non esiste.
     */
    PlayableSource nextSource(List<PlayableSource> sources, int currentIndex);

    /**
     * @brief Seleziona la traccia successiva all'interno di una sorgente.
     *
     * <p>La logica varia in base alla strategia:</p>
     * <ul>
     *   <li><b>Sequential</b>: traccia successiva in ordine; {@code null} se è l'ultima.</li>
     *   <li><b>Loop</b>: traccia successiva in ordine; torna alla prima se è l'ultima.</li>
     *   <li><b>Shuffle</b>: traccia casuale tra quelle disponibili, diversa dalla corrente.</li>
     * </ul>
     *
     * @param tracks       la lista di {@link Track} della sorgente corrente.
     * @param currentIndex l'indice della traccia corrente nella lista.
     * @return la traccia successiva selezionata, o {@code null} se la sorgente è terminata
     *         e si deve passare alla sorgente successiva.
     */
    Track nextTrack(List<Track> tracks, int currentIndex);

    /**
     * @brief Seleziona la sorgente precedente nella lista.
     *
     * <p>È l'operazione simmetrica a {@link #nextSource(List, int)} e ne
     * rispecchia la logica per ciascuna strategia:</p>
     * <ul>
     *   <li><b>Sequential</b>: sorgente precedente in ordine; {@code null} se è la prima.</li>
     *   <li><b>Shuffle</b>: una sorgente casuale ("playlist casuale dal catalogo").</li>
     *   <li><b>Loop</b>: resta sulla sorgente corrente (la playlist riparte da capo).</li>
     * </ul>
     *
     * @param sources la lista di {@link PlayableSource} disponibili.
     * @param currentIndex l'indice della sorgente corrente nella lista.
     * @return la sorgente precedente selezionata, o {@code null} se non esiste.
     */
    PlayableSource previousSource(List<PlayableSource> sources, int currentIndex);

    /**
     * @brief Seleziona la traccia precedente all'interno di una sorgente.
     *
     * <p>È l'operazione simmetrica a {@link #nextTrack(List, int)} e ne
     * rispecchia la logica per ciascuna strategia:</p>
     * <ul>
     *   <li><b>Sequential</b>: traccia precedente in ordine; {@code null} se è la prima
     *       (segnale per passare alla sorgente precedente).</li>
     *   <li><b>Loop</b>: traccia precedente in ordine in modo ciclico; dalla prima
     *       torna all'ultima (con un'unica traccia resta sulla stessa).</li>
     *   <li><b>Shuffle</b>: traccia precedentemente riprodotta (dallo storico);
     *       se non esiste, la traccia corrente (così da riavviarla da capo).</li>
     * </ul>
     *
     * @param tracks       la lista di {@link Track} della sorgente corrente.
     * @param currentIndex l'indice della traccia corrente nella lista.
     * @return la traccia precedente selezionata, o {@code null} se si è all'inizio
     *         della sorgente e si deve passare alla sorgente precedente.
     */
    Track previousTrack(List<Track> tracks, int currentIndex);

    /**
     * @brief Indica se la navigazione "indietro" a livello di traccia (⏮) deve
     * basarsi sullo storico di riproduzione effettivo anziché su {@link #previousTrack}.
     *
     * <p>Per le strategie deterministiche (Sequential, Loop) il brano precedente
     * è ricavabile dall'ordine, quindi il valore è {@code false}. Per lo Shuffle
     * il "brano precedentemente riprodotto" può trovarsi in un'altra sorgente
     * (es. nel catalogo, dove ogni traccia è una sorgente a sé): solo lo storico
     * reale, mantenuto dal client, può individuarlo, quindi il valore è {@code true}.</p>
     *
     * @return {@code true} se ⏮ deve usare lo storico di riproduzione del client.
     */
    default boolean usesHistoryForPrevious() {
        return false;
    }
}
