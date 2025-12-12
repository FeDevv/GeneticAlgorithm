package org.agroplanner;

import org.agroplanner.optimizer.controllers.OptimizerConsoleController;

public class Main {

    @SuppressWarnings("java:S106")
    public static void main(String[] args) {

        new OptimizerConsoleController().run();

    }
}


/*
* OBIETTIVI
* 1. RICONTROLLARE I COMMENTI
* 2. CONTROLLARE CODE SMELLS
* 3. SE inserisci durante l'esport lo stesso nome e la stessa estensione, sovrascrive il vecchio file [magari aggiungere un controllo sui file gia inseriti in cartella]
* 4. fare un clear della console quando si cambia view/controller
* 5. aggiungere ed automatizzare il supporto multi-pianta (multi-punto)
* 6. rendere gli errori nelle CLI dei system.err, aggiungendo una piccolissima sleep (50 ms) per la sincronizzazione
* */