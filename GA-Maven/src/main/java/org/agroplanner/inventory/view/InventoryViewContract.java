package org.agroplanner.inventory.view;

import org.agroplanner.inventory.model.PlantType;

public interface InventoryViewContract {

    void showWizardStart();

    /** Mostra l'elenco delle piante disponibili con ID ed Emoji */
    void showAvailablePlants(PlantType[] types);

    /** Chiede all'utente di selezionare una pianta (ritorna l'Enum) */
    PlantType askForPlantSelection(PlantType[] types);

    /** Chiede la quantità (validando > 0) */
    int askForQuantity(PlantType selectedType);

    /** Chiede il raggio (validando che sia > 0 e <= maxAllowed) */
    double askForRadius(PlantType selectedType, double maxAllowedRadius);

    /** Chiede se continuare ad aggiungere elementi */
    boolean askIfAddMore();

    /** Mostra lo stato attuale del carrello */
    void showCurrentStatus(int totalItems, double maxCurrentRadius);

    void showErrorMessage(String message);
}
