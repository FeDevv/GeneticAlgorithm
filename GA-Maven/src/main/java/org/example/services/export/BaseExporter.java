package org.example.services.export;

import org.example.model.Individual;
import org.example.model.domains.Domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class BaseExporter {

    private static final String EXPORT_FOLDER = "exports";

    /**
     * Metodo Template.
     * NON stampa nulla. Esegue il lavoro e restituisce il percorso del file creato.
     * Se fallisce, lancia un'eccezione che verrà catturata dal Controller.
     *
     * @return Il percorso assoluto del file salvato come String.
     * @throws IOException Se c'è un errore di scrittura o creazione cartelle.
     */
    public final String export(Individual individual, Domain domain, double radius, String filename) throws IOException {

        // 1. Gestione Estensione
        String extension = getExtension();
        if (!filename.toLowerCase().endsWith(extension)) {
            filename += extension;
        }

        // 2. Costruzione Path
        Path path = Paths.get(EXPORT_FOLDER, filename);

        // 3. Creazione Cartella (Silenziosa)
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // 4. Delega scrittura
        performExport(individual, domain, radius, path);

        // 5. Ritorna il path assoluto per informare il chiamante (Controller)
        return path.toAbsolutePath().toString();
    }

    // --- METODI ASTRATTI ---
    protected abstract String getExtension();
    protected abstract void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException;
}
