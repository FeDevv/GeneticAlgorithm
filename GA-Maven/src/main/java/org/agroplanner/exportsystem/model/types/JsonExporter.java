package org.agroplanner.exportsystem.model.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;
import org.agroplanner.inventory.model.InventoryEntry;
import org.agroplanner.inventory.model.PlantInventory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p><strong>Concrete Exporter: JSON (JavaScript Object Notation).</strong></p>
 *
 * <p>This strategy exports the solution in a structured, hierarchical format ideal for machine processing,
 * web integration, or storage in NoSQL databases.</p>
 *
 * <p><strong>Technology Stack:</strong> Uses the <strong>Jackson</strong> library for high-performance POJO serialization.</p>
 */
public class JsonExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ExportType.JSON.getExtension();
    }

    /**
     * Serializes the solution data into a JSON file.
     *
     * <p><strong>Output Structure:</strong>
     * <pre>
     * {
     * "metadata": { "domain_description": "...", "target_distance": ... },
     * "solution": {
     * "fitness": ...,
     * "points": [ { "x": 1.0, "y": 2.0, "radius": ... }, ... ]
     * }
     * }
     * </pre>
     * </p>
     *
     * @param individual The solution to serialize.
     * @param domain     The context metadata.
     * @param path       The target file path.
     * @throws IOException If serialization or writing fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        // Jackson Configuration
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Root Object (LinkedHashMap to preserve order: Metadata -> Solution)
        Map<String, Object> rootNode = new LinkedHashMap<>();

        // --- 1. METADATA SECTION ---
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("domain_info", domain.toString());

        // Convert Inventory Map to a clean List of Objects for JSON
        List<Map<String, Object>> inventoryList = new ArrayList<>();

        // Iteriamo sulla lista degli inserimenti (InventoryEntry)
        for (InventoryEntry entry : inventory.getEntries()) {
            Map<String, Object> item = new LinkedHashMap<>();

            // Recuperiamo i dati dall'oggetto 'entry' invece che dalla coppia key-value
            item.put("plant_type", entry.getType().name());       // "TOMATO"
            item.put("visual_label", entry.getType().getLabel()); // "🍅"
            item.put("requested_qty", entry.getQuantity());
            item.put("radius_constraint", entry.getRadius());

            inventoryList.add(item);
        }

        metadata.put("inventory_request", inventoryList);

        // --- 2. SOLUTION SECTION ---
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fitness", individual.getFitness());
        result.put("total_plants", individual.getDimension());

        // Automatic Serialization:
        // Jackson calls .getX(), .getY(), .getType(), .getRadius() on each Point.
        // Since we updated the Point class, the 'type' and 'radius' fields appear automatically here.
        result.put("plants", individual.getChromosomes());

        // Assembly
        rootNode.put("metadata", metadata);
        rootNode.put("solution", result);

        // File Output
        mapper.writeValue(path.toFile(), rootNode);
    }
}
