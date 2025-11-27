package org.example.export;

import org.example.model.Individual;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe astratta nella quale definisco il comportamento comune a
 * tutti i tipi di export implementati.
 * */
public abstract class BaseExporter {

    // Cartella di default per l'esportazione. Qua dentro viene salvato il risultato
    private static final String EXPORT_FOLDER = "exports";

    /**
     * Metodo Template: Definisce lo scheletro dell'operazione.
     * Non può essere sovrascritto (final) perché la logica di base è fissa.
     */
    public final void export(Individual individual, String filename) {

        // 1. Aggiungiamo l'estensione specifica (definita dalla sottoclasse)
        String extension = getExtension();
        if (!filename.endsWith(extension)) {
            filename += extension;
        }

        // 2. Costruzione Path sicuro
        Path path = Paths.get(EXPORT_FOLDER, filename);

        try {
            // 3. Creazione Cartella (Logica comune a tutti gli export)
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
                System.out.println("[Export] Creata directory: " + path.getParent().toAbsolutePath());
            }

            // 4. Delega la scrittura specifica alla sottoclasse
            performExport(individual, path);

            System.out.println("[Export Success] File salvato in: " + path.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("[Export Error] Impossibile salvare il file: " + e.getMessage());
        }
    }

    // --- METODI ASTRATTI (Le sottoclassi DEVONO implementarli) ---

    // Ogni exporter deve dire che estensione usa (.csv, .json, .txt)
    protected abstract String getExtension();

    // Ogni exporter implementa qui la logica di scrittura specifica
    protected abstract void performExport(Individual individual, Path path) throws IOException;

}
