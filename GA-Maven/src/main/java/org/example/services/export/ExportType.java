package org.example.services.export;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum che definisce i tipi di formati di esportazione supportati.
 * Mantiene la coerenza stilistica con DomainType.
 */
public enum ExportType {
    
    CSV(1, "CSV (Comma Separated Values)"),
    EXCEL(2, "Excel (Excel file)"),
    TXT(3, "TXT (Plain text report)"),
    JSON(4, "Json (JavaScript Object Notation)"),
    PDF(5, "PDF (Portable Document Format)");

    // ------------------- ATTRIBUTI -------------------

    private final int menuId;
    private final String displayName;

    // ------------------- COSTRUTTORE -------------------

    ExportType(int menuId, String displayName) {
        this.menuId = menuId;
        this.displayName = displayName;
    }

    // ------------------- GETTER -------------------

    public int getMenuId() {
        return menuId;
    }

    public String getDisplayName() {
        return displayName;
    }

    // ------------------- UTILS -------------------

    /**
     * Cerca e restituisce un ExportType basato sull'ID del menu.
     * Identico alla logica usata in DomainType.
     */
    public static Optional<ExportType> fromMenuId(int id) {
        return Arrays.stream(ExportType.values())
                .filter(type -> type.menuId == id)
                .findFirst();
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
