package org.agroplanner.persistence.controllers;

import org.agroplanner.access.model.User;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.gasystem.dao.SolutionDAOContract;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.LoadedSession;
import org.agroplanner.gasystem.model.SolutionMetadata;
import org.agroplanner.orchestrator.views.SystemViewContract;
import org.agroplanner.persistence.views.PersistenceViewContract;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the persistence workflow, bridging the gap between the Presentation Layer
 * and the Data Access Layer.
 * <p>
 * This controller enforces business rules regarding saving/loading (e.g., Demo Mode restrictions)
 * and delegates UI interactions to the {@link PersistenceViewContract}.
 * </p>
 */
public class PersistenceConsoleController {

    private final SolutionDAOContract solutionDAO;
    private final PersistenceViewContract view;

    /**
     * Initializes the controller with the required DAO and View dependencies.
     *
     * @param solutionDAO The Data Access Object for handling solution entities.
     * @param view        The abstraction of the user interface.
     */
    public PersistenceConsoleController(SolutionDAOContract solutionDAO, PersistenceViewContract view) {
        this.solutionDAO = solutionDAO;
        this.view = view;
    }

    /**
     * Executes the interactive workflow to persist a simulation session.
     * <p>
     * Validates user permissions (Demo Mode) before attempting to write data.
     * Errors during the write process are caught and relayed to the view.
     * </p>
     *
     * @param solution    The genetic solution to be saved.
     * @param currentUser The user attempting the save operation.
     * @param isDemoMode  Flag indicating if the application is running in restricted mode.
     * @param domainDef   The domain configuration context associated with the solution.
     */
    public void runSaveSession(Individual solution, User currentUser, boolean isDemoMode, DomainDefinition domainDef) {

        if (isDemoMode) {
            view.showGuestSaveRestricted();
            return;
        }

        if (view.askIfSaveSolution()) {
            String title = view.askSolutionTitle();

            try {
                // Pass Domain definition to DAO to ensure the context is saved with the solution
                boolean success = solutionDAO.saveSolution(solution, currentUser, title, domainDef);
                if (success) {
                    view.showSaveSuccess();
                }
            } catch (DataPersistenceException e) {
                view.showSaveError(e.getMessage());
            }
        }
    }

    /**
     * Executes the interactive workflow to retrieve a saved session.
     *
     * @param currentUser The user requesting the data load.
     * @return An {@link Optional} containing the reconstructed session if successful,
     * or empty if the operation was cancelled or failed.
     */
    public Optional<LoadedSession> runLoadSelection(User currentUser) {
        try {
            // 1. Fetch Metadata
            List<SolutionMetadata> saved = solutionDAO.findByUser(currentUser);

            if (saved.isEmpty()) {
                view.showNoSavedSolutions();
                return Optional.empty();
            }

            // 2. User Selection
            int selectedId = view.askSelection(saved);
            if (selectedId == -1) return Optional.empty();

            // 3. Fetch Full Data
            view.showLoadingMessage("Fetching data from storage...");
            return solutionDAO.loadSolution(selectedId);

        } catch (DataPersistenceException e) {
            view.showLoadError(e.getMessage());
            return Optional.empty();
        }
    }

}