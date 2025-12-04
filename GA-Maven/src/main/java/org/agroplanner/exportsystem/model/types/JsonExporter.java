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

public class JsonExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ".json";
    }

    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

        // 1. Configurazione Jackson
        ObjectMapper mapper = new ObjectMapper();
        // Attiva la formattazione "Pretty Print" (indentazione e a capo) per renderlo leggibile
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 2. Costruzione della struttura dati (Root Object)
        // Usiamo LinkedHashMap per mantenere l'ordine di inserimento nel JSON
        Map<String, Object> rootNode = new LinkedHashMap<>();

        // Sezione Metadata
        Map<String, Object> metadata = new LinkedHashMap<>();
        // Mettiamo il toString del dominio come descrizione.
        // Se volessimo l'oggetto completo, dovremmo assicurarci che Domain abbia tutti i getter.
        metadata.put("domain_description", domain.toString());
        metadata.put("target_distance", radius);

        // Sezione Result
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fitness", individual.getFitness());
        result.put("total_points", individual.getDimension());
        // Jackson sa come serializzare automaticamente List<Point> se Point ha i getter (getX, getY)
        result.put("points", individual.getChromosomes());

        // Assembliamo il tutto
        rootNode.put("metadata", metadata);
        rootNode.put("solution", result);

        // 3. Scrittura su file
        // writeValue fa tutto: apre stream, scrive, chiude.
        mapper.writeValue(path.toFile(), rootNode);
    }
}
