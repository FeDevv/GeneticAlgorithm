package org.agroplanner.exportsystem.model;

import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.exportsystem.model.types.*;

/**
 * Factory per la creazione delle istanze di esportazione.
 * Centralizza la logica di istanziazione nascondendo le classi concrete al client.
 */
public class ExporterFactory {

    private ExporterFactory() {}

    private static class FactoryHolder {
        private static final ExporterFactory INSTANCE = new ExporterFactory();
    }

    public static ExporterFactory getInstance() {
        return FactoryHolder.INSTANCE;
    }

    public BaseExporter createExporter(ExportType type) {
        // 1. Controllo Nullità -> InvalidInputException
        if (type == null) {
            throw new InvalidInputException("ExportType cannot be null.");
        }

        return switch (type) {
            case CSV -> new CSVExporter();
            case EXCEL -> new ExcelExporter();
            case TXT -> new TxtExporter();
            case JSON -> new JsonExporter();
            case PDF -> new PdfExporter();

            // 2. Controllo Deep Protection su nuovi tipi non ancora implementati
            // Se aggiungi un enum ma non aggiorni la factory, finisci qui.
            default -> throw new InvalidInputException("Unsupported export type: " + type);
        };
    }
}
