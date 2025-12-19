package org.agroplanner.exportsystem.model;

import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.exportsystem.model.types.*;

/**
 * <p><strong>Factory for Export Strategy Instantiation.</strong></p>
 *
 * <p>This class implements the <strong>Factory Method Pattern</strong> to centralize the creation logic
 * of concrete {@link BaseExporter} implementations. It serves two main architectural purposes:</p>
 * <ul>
 * <li><strong>Decoupling:</strong> The client (Controller) relies only on the abstraction (BaseExporter) and the metadata (ExportType), never on concrete classes.</li>
 * <li><strong>Validation:</strong> It acts as a gatekeeper, ensuring that requested export types are actually supported/implemented.</li>
 * </ul>
 *
 * <p><strong>Pattern:</strong> Singleton (Initialization-on-demand holder idiom).</p>
 */
public class ExporterFactory {

    // ------------------- SINGLETON PATTERN -------------------

    /** Private constructor to prevent direct instantiation. */
    private ExporterFactory() {

    }

    /**
     * Static Inner Class (Lazy Holder).
     * Loaded only when {@code getInstance()} is called, ensuring thread safety without synchronized blocks.
     */
    private static class FactoryHolder {
        private static final ExporterFactory INSTANCE = new ExporterFactory();
    }

    /**
     * Retrieves the global singleton instance of the factory.
     * @return The singleton instance.
     */
    public static ExporterFactory getInstance() {
        return FactoryHolder.INSTANCE;
    }

    // ------------------- FACTORY METHOD -------------------

    /**
     * Creates and returns a concrete Exporter instance based on the requested type.
     *
     * @param type The metadata enum describing the desired output format.
     * @return A new instance of a class extending {@link BaseExporter}.
     * @throws InvalidInputException If the type is null or if the factory doesn't know how to handle the requested type
     * (Defensive Programming against unimplemented Enums).
     */
    public BaseExporter createExporter(ExportType type) {
        // Deep Protection: Null Safety
        if (type == null) {
            throw new InvalidInputException("ExportType cannot be null.");
        }

        // Dispatch Logic
        return switch (type) {
            case CSV -> new CSVExporter();
            case EXCEL -> new ExcelExporter();
            case TXT -> new TxtExporter();
            case JSON -> new JsonExporter();
            case PDF -> new PdfExporter();

            // Defensive Programming (Safety Net)
            // Covers the edge case where a developer adds a new constant to ExportType
            // but forgets to implement the corresponding logic here.
            default -> throw new InvalidInputException("Unsupported export type: " + type);
        };
    }
}
