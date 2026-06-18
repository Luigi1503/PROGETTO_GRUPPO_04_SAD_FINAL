package com.example.gruppo04;

/**
 * @brief Launcher dell'applicazione, separato da {@link MainApplication}.
 * @details Necessario per l'esecuzione tramite {@code java -jar} su jar
 * contenenti tutte le dipendenze (shaded jar).
 */
public class Launcher {

    /**
     * @brief Punto di ingresso dell'applicazione.
     * @param args argomenti da linea di comando, passati inalterati a {@link MainApplication}
     */
    public static void main(String[] args) {
        MainApplication.main(args);
    }
}