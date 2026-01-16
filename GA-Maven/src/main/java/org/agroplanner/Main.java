package org.agroplanner;

import org.agroplanner.orchestrator.controllers.SystemOrchestrator;

public class Main {

    @SuppressWarnings("java:S106")
    public static void main(String[] args) {

        new SystemOrchestrator().run();

    }

}


