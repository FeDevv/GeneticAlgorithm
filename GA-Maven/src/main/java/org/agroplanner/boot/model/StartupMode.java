package org.agroplanner.boot.model;

/**
 * Enumerates the available execution profiles (presets) for the application.
 * <p>
 * This enum maps user-friendly IDs and labels to specific technical combinations
 * of UI paradigms (CLI vs JavaFX) and persistence strategies (Memory vs Disk).
 * </p>
 */
public enum StartupMode {

    // --- CLI MODES ---
    CLI_DEMO(1, "CLI Demo (No Save)", PersistenceType.MEMORY, false),
    CLI_FS(2,   "CLI + FileSystem",   PersistenceType.FILESYSTEM, false),
    CLI_DB(3,   "CLI + Database",     PersistenceType.DATABASE,   false),

    // --- JFX MODES ---
    JFX_DEMO(4, "JFX Demo (No Save)", PersistenceType.MEMORY, true),
    JFX_FS(5,   "JFX + FileSystem",   PersistenceType.FILESYSTEM, true),
    JFX_DB(6,   "JFX + Database",     PersistenceType.DATABASE,   true);

    private final int id;
    private final String label;
    private final PersistenceType persistenceType; // Solo il tipo (enum), NON la factory
    private final boolean guiActive;

    StartupMode(int id, String label, PersistenceType persistenceType, boolean guiActive) {
        this.id = id;
        this.label = label;
        this.persistenceType = persistenceType;
        this.guiActive = guiActive;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
    public PersistenceType getPersistenceType() { return persistenceType; }
    public boolean isGuiActive() { return guiActive; }

    /**
     * Resolves a startup mode from its unique numeric identifier.
     *
     * @param id The selected ID (typically input by the user).
     * @return The corresponding {@link StartupMode}, or {@code null} if no match is found.
     */
    public static StartupMode fromId(int id) {
        for (StartupMode mode : values()) {
            if (mode.id == id) return mode;
        }
        return null;
    }
}
