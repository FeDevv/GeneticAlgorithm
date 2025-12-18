package org.agroplanner.inventory.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Identificatore univoco delle specie disponibili nel sistema.
 * <p>Nota: Non contiene dati fisici (raggio, ecc.) in quanto questi
 * vengono iniettati a runtime tramite configurazione esterna (DB/Form).</p>
 */
public enum PlantType {
    TOMATO(1, "TOMATO", "🍅"),
    CORN(2, "CORN", "🌽"),
    POTATO(3, "POTATO", "🥔"),
    CARROT(4, "CARROT", "🥕"),
    WHEAT(5, "WHEAT", "🌾"),
    ZUCCHINI(6, "ZUCCHINI", "🥒"),
    PUMPKIN(7, "PUMPKIN", "🎃");

    private final int id;
    private final String name;
    private final String label;

    PlantType(int id, String name, String label) {
        this.id = id;
        this.name = name;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Cerca una pianta per ID usando gli Stream (Stile Funzionale).
     * Identico all'approccio usato in DomainType.
     */
    public static Optional<PlantType> getById(int id) {
        return Arrays.stream(values())
                .filter(type -> type.id == id)
                .findFirst();
    }
}
