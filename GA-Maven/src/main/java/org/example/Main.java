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
* 1. RISCRIVERE I COMMENTI
* 2. SUDDIVIDI CONTROLLER GRAFICO E CONTROLLER LOGICO - crea proprio due classi distinte,
*    i controller grafici seguono un'interfaccia. il controller logico chiama il controller
*    grafico che è stato istanziato dalla factory apposita.
* */