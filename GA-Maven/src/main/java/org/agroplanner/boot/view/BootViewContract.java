package org.agroplanner.boot.view;

import org.agroplanner.boot.model.StartupMode;

/**
 * Defines the contract for the boot interaction layer.
 * <p>
 * This interface adheres to the Dependency Inversion Principle, allowing the boot logic
 * to remain agnostic of the specific I/O implementation (Console vs GUI Window).
 * </p>
 */
public interface BootViewContract {
    /**
     * Displays the application welcome banner or splash screen.
     */
    void showWelcomeBanner();

    /**
     * Prompts the user to select a startup configuration from the available options.
     *
     * @param modes The list of available startup profiles.
     * @return The {@link StartupMode} selected by the user.
     */
    StartupMode askForStartupMode(StartupMode[] modes);
}
