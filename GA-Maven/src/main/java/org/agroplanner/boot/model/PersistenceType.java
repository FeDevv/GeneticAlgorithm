package org.agroplanner.boot.model;

/**
 * Enumerates the supported data storage engines.
 * <p>
 * This enum serves as a configuration flag to decouple the abstract concept of storage
 * from the concrete Factory implementation found in the persistence layer.
 * </p>
 */
public enum PersistenceType {
    MEMORY("Volatile (RAM)", "❌ No Save"),
    DATABASE("SQL Database (H2)", "💾 .db file"),
    FILESYSTEM("File System (JSON)", "📂 .json files");

    private final String label;
    private final String icon;

    PersistenceType(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public String getLabel() { return label; }
    public String getIcon() { return icon; }
}
