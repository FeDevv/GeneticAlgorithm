package org.agroplanner.exportsystem.model.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.inventory.model.InventoryEntry;
import org.agroplanner.inventory.model.PlantInventory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of the Export Strategy targeting <strong>JSON (JavaScript Object Notation)</strong>.
 */
public class JsonExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ExportType.JSON.getExtension();
    }

    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        // 1. Configuration
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 2. Root Construction
        Map<String, Object> rootNode = new LinkedHashMap<>();

        // --- SECTION A: METADATA ---
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("domain_info", domain.toString());
        metadata.put("timestamp", System.currentTimeMillis());

        // Serialize Inventory Context
        List<Map<String, Object>> inventoryList = serializeInventory(inventory);
        metadata.put("inventory_request", inventoryList);

        // --- SECTION B: SOLUTION ---
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fitness", individual.getFitness());
        result.put("total_plants", individual.getDimension());

        // Serialize Points (Now with Variety Data!)
        List<Map<String, Object>> plantsList = serializeSolutionPoints(individual.getChromosomes());
        result.put("plants", plantsList);

        // 3. Assembly
        rootNode.put("metadata", metadata);
        rootNode.put("solution", result);

        // 4. Persistence
        mapper.writeValue(path.toFile(), rootNode);
    }

    // --- HELPERS ---

    /**
     * Converts Inventory objects into a clean JSON map.
     */
    private static List<Map<String, Object>> serializeInventory(PlantInventory inventory) {
        List<Map<String, Object>> inventoryList = new ArrayList<>();

        for (InventoryEntry entry : inventory.getEntries()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("plant_type", entry.getType().name());
            item.put("visual_label", entry.getType().getLabel());
            item.put("requested_qty", entry.getQuantity());
            item.put("radius_constraint", entry.getRadius());
            inventoryList.add(item);
        }
        return inventoryList;
    }

    /**
     * Converts Solution Points into a clean JSON map (DTO Pattern).
     * <p>Updated to include Variety ID and Name.</p>
     */
    private static List<Map<String, Object>> serializeSolutionPoints(List<Point> points) {
        List<Map<String, Object>> list = new ArrayList<>();

        for (Point p : points) {
            Map<String, Object> map = new LinkedHashMap<>();

            // NEW FIELDS: Variety Info (snake_case per standard JSON)
            map.put("variety_id", p.getVarietyId());
            map.put("variety_name", p.getVarietyName());

            // Geometric Data
            map.put("type", p.getType().name());
            map.put("x", p.getX());
            map.put("y", p.getY());
            map.put("radius", p.getRadius());

            list.add(map);
        }
        return list;
    }
}
