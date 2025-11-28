package org.example;


import org.example.controllers.AppController;

public class Main {

    @SuppressWarnings("java:S106")
    public static void main(String[] args) {

        // Crea l'Overseer
        AppController app = new AppController();

        // Lancia l'applicazione
        app.run();

    }
}


/*
* OBIETTIVI
* 1. rendere le factory un singleton *
* 2. ricontrollare i commenti
* 3. rimuovi i code smell
* */

// * aggiungere quindi l'attributo di tipo "singletonClass" e un check (if) per ritornare sempre lo stessa istanza.

/**
 * singletonClass singleton = null
 * ...
 * if (singleton == null) {
 *     return new singletonClass;
 * } else {
 *     return singleton;
 * }
 * */

