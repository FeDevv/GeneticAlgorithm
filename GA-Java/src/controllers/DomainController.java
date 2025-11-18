package controllers;

import model.domains.Domain;
import model.domains.DomainFactory;
import model.domains.DomainType;
import view.DomainConsoleView;

import java.util.*;

/**
 * Il Controller del sistema. È responsabile dell'orchestrare il flusso di creazione di un dominio:
 * <p>
 * 1. Richiedere la selezione del tipo di dominio e i parametri all'utente (delegando alla {@code DomainConsoleView}).
 * 2. Gestire il ciclo di selezione/creazione fino al successo o all'uscita.
 * 3. Invocare la {@code DomainFactory} per la creazione effettiva.
 * <p>
 * Questo controller è disaccoppiato dalla logica di I/O grazie all'interfaccia {@code DomainConsoleView}.
 */
public class DomainController {

    // Dipendenza dalla Factory: il Controller usa la Factory per creare gli oggetti Domain.
    private final DomainFactory factory = new DomainFactory();

    // Dipendenza dalla View: Il Controller usa la View per tutte le interazioni con l'utente (I/O).
    private final DomainConsoleView view;

    /**
     * Costruttore che inietta la dipendenza della View.
     * @param view L'istanza della View (es. {@code DomainConsoleView}) che gestirà l'I/O.
     */
    public DomainController(DomainConsoleView view) {
        this.view = view;
    }

    /**
     * Metodo principale per orchestrare il flusso di creazione di un dominio.
     * <p>
     * Questo metodo esegue un ciclo infinito per visualizzare il menu, gestire le scelte
     * non valide e riprovare la creazione in caso di errore di validazione dei parametri.
     *
     * @return Un {@code Optional} contenente l'istanza di {@code Domain} se la creazione ha successo,
     * o un {@code Optional.empty()} se l'utente sceglie di uscire (opzione 0).
     */
    public Optional<Domain> createDomain() {
        while (true) {
            // 1. Visualizzazione del menu (delegato alla View)
            view.showMenu(Arrays.asList(DomainType.values()));

            // 2. Lettura della scelta (delegata alla View)
            int choice = view.readMenuChoice();

            if (choice == 0)
                return Optional.empty(); // Uscita richiesta dall'utente

            // 3. Mappatura della scelta all'Enum
            Optional<DomainType> selectedType = DomainType.fromMenuId(choice);

            if (selectedType.isEmpty()) {
                // Scelta numerica non valida (non mappata a nessun DomainType)
                view.showError("Invalid choice.");
                continue; // Ricomincia il ciclo (mostra di nuovo il menu)
            }

            DomainType type = selectedType.get();

            // 4. Raccolta dei parametri richiesti (delegato alla View)
            Map<String, Double> params = view.readParameters(type.getRequiredParameters(), type.getDisplayName());

            // 5. Creazione del dominio (Delegata alla Factory)
            try {
                Domain domain = factory.createDomain(type, params);

                // Messaggio di successo (delegato alla View)
                view.showSuccess();

                return Optional.of(domain); // Creazione riuscita, esce dal ciclo e restituisce il risultato
            } catch (IllegalArgumentException e) {
                // Errore di validazione (es. raggio <= 0) sollevato dalla Factory/Domain.
                // Messaggio di errore (delegato alla View) e riprova (il ciclo 'while' continua).
                view.showError(e.getMessage());
            }
        }
    }
}

