package org.agroplanner.persistence.implementations.inventory;

import org.agroplanner.access.model.User;
import org.agroplanner.inventory.dao.PlantVarietyDAOContract;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.model.PlantVarietySheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Volatile implementation of the Plant Inventory.
 * <p>
 * Stores data in an in-memory List. Pre-populated with dummy data to facilitate
 * demonstration and testing without external dependencies.
 * </p>
 */
public class MemoryPlantVarietyDAO implements PlantVarietyDAOContract {

    private final List<PlantVarietySheet> memoryDb = new ArrayList<>();

    @Override
    public void initStorage() {
        memoryDb.clear();
        // Seed with Demo Data
        User system = new User();
        system.setFirstName("System"); system.setLastName("Demo");

        save(new PlantVarietySheet(PlantType.TOMATO, "San Marzano Demo", 0.45, system));
        save(new PlantVarietySheet(PlantType.TOMATO, "Cherry Bomb", 0.30, system));
        save(new PlantVarietySheet(PlantType.CORN, "Sweet Golden", 0.25, system));
        save(new PlantVarietySheet(PlantType.POTATO, "Russet Alpha", 0.40, system));
    }

    @Override
    public boolean save(PlantVarietySheet sheet) {
        sheet.setId(memoryDb.size() + 1);
        memoryDb.add(sheet);
        return true;
    }

    @Override
    public List<PlantVarietySheet> findByType(PlantType type) {
        return memoryDb.stream()
                .filter(p -> p.getType() == type)
                .toList();
    }

    @Override
    public List<PlantVarietySheet> findAll() {
        return new ArrayList<>(memoryDb);
    }

    @Override
    public PlantVarietySheet findById(int id) {
        return null;
    }

    @Override
    public List<PlantVarietySheet> findAllByIds(Set<Integer> ids) {
        return memoryDb.stream()
                .filter(p -> ids.contains(p.getId()))
                .toList();
    }
}
