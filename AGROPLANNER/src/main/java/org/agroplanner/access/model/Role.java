package org.agroplanner.access.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumerates the authorization levels (roles) within the system.
 * <p>
 * Used to control access to specific application modules (Inventory, Analysis, Administration).
 * </p>
 */
public enum Role {

    // --- SPECIAL ROLES ---
    GUEST(0, "Guest", false),

    // --- STANDARD ROLES ---
    USER(1, "Standard User", true),
    AGRONOMIST(2, "Agronomist", true),
    ADMINISTRATOR(99, "system administrator", false);

    private final int id;
    private final String label;
    private final boolean publicSelectable;

    Role(int id, String label, boolean publicSelectable) {
        this.id = id;
        this.label = label;
        this.publicSelectable = publicSelectable;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
    public boolean isPublicSelectable() { return publicSelectable; }

    /**
     * Resolves a Role from its numeric identifier.
     * @param id The ID to lookup.
     * @return An {@link Optional} containing the Role if found.
     */
    public static Optional<Role> fromId(int id) {
        return Arrays.stream(values())
                .filter(role -> role.id == id)
                .findFirst();
    }
}
