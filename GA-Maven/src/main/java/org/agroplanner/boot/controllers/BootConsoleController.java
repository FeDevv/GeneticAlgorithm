package org.agroplanner.boot.controllers;

import org.agroplanner.boot.model.AgroConfiguration;
import org.agroplanner.boot.model.StartupMode;
import org.agroplanner.boot.view.BootViewContract;

/**
 * Orchestrates the initial system startup sequence.
 * <p>
 * This controller mediates between the user (via the View) and the system configuration model,
 * acting as a wizard to gather necessary launch parameters before handing over control to the main orchestrator.
 * </p>
 */
public class BootConsoleController {

    private final BootViewContract view;

    /**
     * Initializes the boot controller.
     *
     * @param view The abstraction of the startup user interface.
     */
    public BootConsoleController(BootViewContract view) {
        this.view = view;
    }

    /**
     * Executes the interactive boot workflow.
     * <p>
     * This method displays the welcome banner, prompts the user to select a {@link StartupMode},
     * and maps the selection into a system-agnostic configuration object.
     * </p>
     *
     * @return The final {@link AgroConfiguration} DTO required to initialize the main application.
     */
    public AgroConfiguration runBootSequence() {
        view.showWelcomeBanner();

        // 1. User Interaction
        StartupMode selectedMode = view.askForStartupMode(StartupMode.values());

        // 2. Configuration Assembly
        return new AgroConfiguration(
                selectedMode.getPersistenceType(),
                selectedMode.isGuiActive()
        );
    }
}
