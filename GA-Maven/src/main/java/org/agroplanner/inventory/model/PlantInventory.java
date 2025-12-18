package org.agroplanner.inventory.model;

import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.*;

public class PlantInventory {

    // USIAMO UNA LISTA: Accetta duplicati (es. due inserimenti di Pomodori diversi)
    private final List<InventoryEntry> entries;

    public PlantInventory() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(PlantType type, int quantity, double radius) {
        // Validazione essenziale
        if (type == null) throw new InvalidInputException("PlantType cannot be null.");
        if (quantity <= 0) throw new InvalidInputException("Quantity must be positive.");
        if (radius <= 0) throw new InvalidInputException("Radius must be positive.");

        // Semplice: aggiungo una riga alla lista
        entries.add(new InventoryEntry(type, quantity, radius));
    }

    // Restituisce la lista per chi deve leggerla (Export, Algoritmo, View)
    public List<InventoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    // Utility: conta quante piante totali ci sono (somma delle quantità)
    public int getTotalPopulationSize() {
        return entries.stream().mapToInt(InventoryEntry::getQuantity).sum();
    }

    // Utility: trova il raggio massimo (per validazioni)
    public double getMaxRadius() {
        return entries.stream()
                .mapToDouble(InventoryEntry::getRadius)
                .max()
                .orElse(0.0);
    }
}
