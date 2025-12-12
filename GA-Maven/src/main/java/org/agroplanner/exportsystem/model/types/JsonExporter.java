package org.agroplanner.exportsystem.model.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
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
        return ".json";
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
     * @param radius     The point dimension.
     * @param path       The target file path.
     * @throws IOException If serialization or writing fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

        // Jackson Configuration
        ObjectMapper mapper = new ObjectMapper();

        // UX Choice: Enable "Pretty Print".
        // Increases file size slightly but makes it readable for humans (indentation + newlines).
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Data Structure Construction (Root Object)
        // Implementation Choice: Use LinkedHashMap.
        // Unlike HashMap (which has undefined order), LinkedHashMap preserves the insertion order.
        // This ensures the JSON fields always appear in a predictable sequence (Metadata first, then Solution).
        Map<String, Object> rootNode = new LinkedHashMap<>();

        // --- Metadata Section ---
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("domain_description", domain.toString());
        metadata.put("target_distance", radius);

        // --- Result Section ---
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fitness", individual.getFitness());
        result.put("total_points", individual.getDimension());

        // Serialization Note:
        // Jackson automatically serializes the List<Point> by calling the public getters
        // (getX, getY, getRadius) on the Point objects. No manual loop is required.
        result.put("points", individual.getChromosomes());

        // Assembly
        rootNode.put("metadata", metadata);
        rootNode.put("solution", result);

        // File Output
        // 'writeValue' handles the entire stream lifecycle (Open -> Write -> Close).
        mapper.writeValue(path.toFile(), rootNode);
    }
}
