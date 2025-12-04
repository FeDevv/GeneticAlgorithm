package org.agroplanner.exportsystem.views;

import org.agroplanner.exportsystem.model.ExportType;

import java.util.List;
import java.util.Optional;

public interface ExportViewContract {

    /**
     * Chiede all'utente il formato di esportazione.
     * @return Il tipo scelto, o Empty se l'utente vuole saltare l'export.
     */
    Optional<ExportType> askForExportType(List<ExportType> availableTypes);

    /**
     * Chiede il nome del file (senza estensione).
     */
    String askForFilename();

    /**
     * Mostra il successo dell'operazione.
     * @param filePath Il percorso completo del file salvato.
     */
    void showSuccessMessage(String filePath);

    /**
     * Mostra un errore.
     */
    void showErrorMessage(String message);
}
