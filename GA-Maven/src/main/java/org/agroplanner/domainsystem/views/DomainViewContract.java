package org.agroplanner.domainsystem.views;

import org.agroplanner.domainsystem.model.DomainType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DomainViewContract {

    /**
     * Chiede all'utente di selezionare un tipo di dominio dalla lista.
     * @param types La lista dei tipi disponibili.
     * @return Il tipo selezionato, oppure Empty se l'utente vuole annullare/uscire.
     */
    Optional<DomainType> askForDomainType(List<DomainType> types);

    /**
     * Chiede i parametri specifici richiesti dal tipo di dominio selezionato.
     * @param type Il tipo di dominio per cui chiedere i parametri.
     * @return Una mappa <NomeParametro, Valore>.
     */
    Map<String, Double> askForParameters(DomainType type);

    /**
     * Mostra un messaggio di successo generico o specifico.
     */
    void showSuccessMessage();

    /**
     * Mostra un messaggio di errore.
     * @param message Il dettaglio dell'errore.
     */
    void showErrorMessage(String message);
}
