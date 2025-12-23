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
 * Concrete implementation of the Export Strategy targeting <strong>JSON (JavaScript Object Notation)</strong>.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Integration Ready:</strong> Generates a structured, hierarchical payload ideal for interoperability
 * with Web Frontends (React/Angular), REST APIs, or NoSQL databases (MongoDB).</li>
 * <li><strong>Library Leverage:</strong> Utilizes <strong>Jackson</strong> for high-performance POJO serialization,
 * eliminating the need for manual string concatenation and escaping logic.</li>
 * <li><strong>Deterministic Ordering:</strong> Uses {@link LinkedHashMap} to construct the JSON tree.
 * While JSON parsers do not mandate order, enforcing "Metadata First, Data Second" improves human readability.</li>
 * </ul>
 */
public class JsonExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ExportType.JSON.getExtension();
    }

    /**
     * Serializes the solution context and data into a JSON file.
     * <p><strong>Output Schema:</strong></p>
     * <pre>
     * {
     * "metadata": {
     * "domain_info": "...",
     * "inventory_request": [ ... ]
     * },
     * "solution": {
     * "fitness": 0.95,
     * "total_plants": 150,
     * "plants": [ { "x": 10.5, "y": 20.1, ... }, ... ]
     * }
     * }
     * </pre>
     *
     * @param individual The solution genotype.
     * @param domain     The geometric context.
     * @param inventory  The biological context.
     * @param path       The target file path.
     * @throws IOException If the disk write operation fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        // RESOURCE MANAGEMENT NOTE:
        // Unlike manual writers (CSV/Txt), we do not need an explicit try-with-resources block here.
        // Jackson's 'mapper.writeValue(File, ...)' automatically handles the entire I/O lifecycle:
        // it opens the stream, streams the content, and strictly closes the resource upon completion or failure.

        // 1. Configuration
        ObjectMapper mapper = new ObjectMapper();
        // Enable pretty-printing for human readability
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 2. Root Construction (LinkedHashMap preserves insertion order)
        Map<String, Object> rootNode = new LinkedHashMap<>();

        // --- SECTION A: METADATA ---
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("domain_info", domain.toString());
        metadata.put("timestamp", System.currentTimeMillis());

        // Transform Inventory domain objects into JSON-friendly Maps
        List<Map<String, Object>> inventoryList = serializeInventory(inventory);
        metadata.put("inventory_request", inventoryList);

        // --- SECTION B: SOLUTION ---
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fitness", individual.getFitness());
        result.put("total_plants", individual.getDimension());

        // Automatic POJO Serialization:
        // Jackson uses reflection to call .getX(), .getY(), .getType(), .getRadius() on each Point.
        // The Point class structure is automatically mapped to JSON Array of Objects.
        result.put("plants", individual.getChromosomes());

        // 3. Assembly
        rootNode.put("metadata", metadata);
        rootNode.put("solution", result);

        // 4. Persistence
        mapper.writeValue(path.toFile(), rootNode);
    }

    /**
     * Helper method to convert the Inventory Domain Model into a serializable Map structure.
     * <p>Acts as a lightweight DTO (Data Transfer Object) mapper.</p>
     */
    private static List<Map<String, Object>> serializeInventory(PlantInventory inventory) {
        List<Map<String, Object>> inventoryList = new ArrayList<>();

        for (InventoryEntry entry : inventory.getEntries()) {
            Map<String, Object> item = new LinkedHashMap<>();

            item.put("plant_type", entry.getType().name());       // "TOMATO"
            item.put("visual_label", entry.getType().getLabel()); // "🍅"
            item.put("requested_qty", entry.getQuantity());
            item.put("radius_constraint", entry.getRadius());

            inventoryList.add(item);
        }
        return inventoryList;
    }
}
