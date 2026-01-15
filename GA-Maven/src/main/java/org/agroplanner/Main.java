package org.agroplanner;

import org.agroplanner.orchestrator.controllers.SystemOrchestrator;

public class Main {

    @SuppressWarnings("java:S106")
    public static void main(String[] args) {

        new SystemOrchestrator().run();

    }

}

// remove unused methods in credentialDTO
// check inventoryconsolecontroller --> runinventorywizard if error occurs
// check filedomaindao --> init if error occurs
// check filesolutiondao --> loadSolution