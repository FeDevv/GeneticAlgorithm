package org.agroplanner.persistence.views;

import org.agroplanner.gasystem.model.SolutionMetadata;

import java.util.List;

/**
 * Defines the contract for the Persistence User Interface.
 * <p>
 * This interface abstracts the interaction mechanism (Console, GUI) for saving and loading operations,
 * allowing the controller to remain agnostic of the specific UI implementation.
 * </p>
 */
public interface PersistenceViewContract {

    void showGuestSaveRestricted();
    boolean askIfSaveSolution();
    String askSolutionTitle();
    void showSaveSuccess();
    void showSaveError(String error);
    void showNoSavedSolutions();
    int askSelection(List<SolutionMetadata> list);
    void showLoadError(String msg);
    void showLoadingMessage(String msg);
}
