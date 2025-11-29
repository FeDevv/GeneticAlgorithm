package org.example.services.export;

import org.example.services.export.types.*;

/**
 * Factory per la creazione delle istanze di esportazione.
 * Centralizza la logica di istanziazione nascondendo le classi concrete al client.
 */
public class ExporterFactory {

    // 1. Costruttore Privato
    // Fondamentale: impedisce a chiunque di fare 'new ExporterFactory()'
    private ExporterFactory() {
        // Qui potresti mettere log di inizializzazione se servissero
    }

    // 2. Inner Class Statica (Lazy Holder)
    // Questa classe non viene caricata in memoria finché qualcuno non chiama getInstance().
    // La JVM garantisce che il caricamento delle classi statiche sia Thread-Safe.
    private static class FactoryHolder {
        private static final ExporterFactory INSTANCE = new ExporterFactory();
    }

    // 3. Global Access Point
    public static ExporterFactory getInstance() {
        return FactoryHolder.INSTANCE;
    }

    /**
     * Metodo di business (la logica Factory vera e propria).
     * Non cambia nulla rispetto a prima.
     */
    public BaseExporter createExporter(ExportType type) {
        if (type == null) {
            throw new IllegalArgumentException("ExportType cannot be null.");
        }

        return switch (type) {
            case CSV -> new CSVExporter();
            case EXCEL -> new ExcelExporter();
            case TXT -> new TxtExporter();
            case JSON -> new JsonExporter();
            case PDF -> new PdfExporter();
        };
    }
}
