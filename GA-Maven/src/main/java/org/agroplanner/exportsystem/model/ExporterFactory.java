package org.agroplanner.exportsystem.model;

import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.exportsystem.model.types.*;

/**
 * Factory component responsible for the instantiation of concrete Export Strategies.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Factory Method. It encapsulates the decision logic of <em>which</em> class to instantiate,
 * decoupling the Controller (Client) from the specific implementations ({@link CSVExporter}, {@link ExcelExporter}, etc.).</li>
 * <li><strong>Singleton Strategy:</strong> Implements the <em>Initialization-on-demand holder idiom</em>.
 * This ensures the instance is created lazily (only when needed) and is inherently thread-safe without the performance overhead of synchronization.</li>
 * <li><strong>Open/Closed Principle (Partial):</strong> While adding a new format requires modifying this switch statement,
 * the client code remains unchanged, isolating the modification impact to this single factory class.</li>
 * </ul>
 */
public class ExporterFactory {

    // ------------------- SINGLETON PATTERN -------------------

    /**
     * Private constructor to strictly prevent direct instantiation.
     */
    private ExporterFactory() {

    }

    /**
     * <strong>Lazy Holder:</strong>
     * The JVM loads this static inner class only when {@link #getInstance()} is invoked for the first time.
     * This provides a lock-free, thread-safe singleton initialization.
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
     * Dispatches the request to the appropriate concrete Exporter implementation.
     *
     * <p><strong>Safety Net (Configuration Drift):</strong></p>
     * The {@code switch} statement maps the metadata enum to the actual strategy class.
     * The {@code default} branch acts as a safeguard: if a developer adds a new constant to {@link ExportType}
     * but forgets to update this factory, the system will fail fast with a descriptive exception rather than
     * returning null or behaving unpredictably.
     *
     * @param type The metadata enum describing the desired output format.
     * @return A new instance of a class extending {@link BaseExporter}.
     * @throws InvalidInputException If {@code type} is null or if the requested format is not yet mapped to an implementation.
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

            // Edge Case: Enum exists but Factory implementation is missing.
            default -> throw new InvalidInputException("Unsupported export type: " + type);
        };
    }
}
