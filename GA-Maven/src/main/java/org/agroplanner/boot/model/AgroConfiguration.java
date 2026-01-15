package org.agroplanner.boot.model;

/**
 * Immutable Data Transfer Object (DTO) encapsulating system-wide settings.
 * <p>
 * This class acts as the "handover contract" between the Bootloader phase
 * and the Main Orchestrator, carrying the finalized runtime configuration.
 * </p>
 */
public class AgroConfiguration {
    private final PersistenceType persistenceType;
    private final boolean guiActive;

    /**
     * Constructs a new configuration object.
     *
     * @param persistenceType The selected storage strategy.
     * @param guiActive       {@code true} if the graphical user interface is enabled, {@code false} for CLI.
     */
    public AgroConfiguration(PersistenceType persistenceType, boolean guiActive) {
        this.persistenceType = persistenceType;
        this.guiActive = guiActive;
    }

    public PersistenceType getPersistenceType() {
        return persistenceType;
    }

    public boolean isGuiActive() {
        return guiActive;
    }
}
