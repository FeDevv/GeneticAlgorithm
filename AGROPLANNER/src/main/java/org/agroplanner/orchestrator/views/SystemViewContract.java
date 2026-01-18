package org.agroplanner.orchestrator.views;

/**
 * Defines the abstract contract for the <strong>Main Application View</strong>.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Scope:</strong> Unlike subsystem views (which handle specific tasks like Domain definition or Export),
 * this view manages the <strong>Global Lifecycle</strong> of the application.</li>
 * <li><strong>Responsibility:</strong> Handles "Meta-Interactions" such as:
 * <ul>
 * <li>Session State transitions (Welcome, New Session, Exit).</li>
 * <li>Global Error Reporting (Displaying exceptions caught by the main safety net).</li>
 * <li>High-level flow control (Restart vs Terminate).</li>
 * </ul>
 * </li>
 * </ul>
 */

import org.agroplanner.access.model.User;

/**
 * Defines the contract for the <strong>System Orchestrator View</strong>.
 * <p>
 * This view handles the "Meta-Lifecycle" of the application:
 * <ul>
 * <li>Global state transitions (Bootstrap -> Login -> Dashboard -> Exit).</li>
 * <li>Global Error Reporting.</li>
 * <li>Top-level menu navigation.</li>
 * </ul>
 * </p>
 */
public interface SystemViewContract {

    // --- BOOTSTRAP ---
    void showBootstrapInfo(String message);
    void showWelcomeMessage();

    // --- DASHBOARD ---
    void showNewSessionMessage();
    int askMainDashboardChoice(User user);
    boolean askForNewSession();

    // --- SYSTEM STATES ---
    void showExitMessage();
    void showLogoutMessage();
    void showDemoModeActive();
    void showDemoSessionEnded();
    void showUnknownCommand();

    // --- ERRORS & FEEDBACK ---
    void showSessionAborted(String reason);
    void showExportError(String message);
    void showDemoFeatureDisabled();

    // --- INTERACTION ---
    boolean askIfExportWanted();
    void waitForUserConfirmation();
}
