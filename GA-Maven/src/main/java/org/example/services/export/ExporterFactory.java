package org.example.services.export;

import org.example.services.export.types.CSVExporter;
import org.example.services.export.types.ExcelExporter;
import org.example.services.export.types.JsonExporter;
import org.example.services.export.types.TxtExporter;

/**
 * Factory per la creazione delle istanze di esportazione.
 * Centralizza la logica di istanziazione nascondendo le classi concrete al client.
 */
public class ExporterFactory {

    /**
     * Crea un'istanza concreta di BaseExporter basata sul tipo richiesto.
     *
     * @param type Il tipo di export scelto dall'enum.
     * @return L'istanza concreta che estende BaseExporter.
     * @throws IllegalArgumentException se il tipo è null.
     */
    public BaseExporter createExporter(ExportType type) {

        // 1. Validazione preventiva (stile difensivo come nella tua DomainFactory)
        if (type == null) {
            throw new IllegalArgumentException("ExportType cannot be null.");
        }

        // 2. Switch expression per l'istanziazione
        return switch (type) {
            case CSV -> new CSVExporter();
            case EXCEL -> new ExcelExporter();
            case TXT -> new TxtExporter();
            case JSON -> new JsonExporter();


            // Non serve default, l'enum copre tutti i casi possibili noti.
        };
    }
}
