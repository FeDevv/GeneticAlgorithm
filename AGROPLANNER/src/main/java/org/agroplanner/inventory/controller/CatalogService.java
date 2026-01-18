package org.agroplanner.inventory.controller;

import org.agroplanner.inventory.dao.PlantVarietyDAOContract;
import org.agroplanner.inventory.model.PlantVarietySheet;

/**
 * Service for managing the Botanical Catalog.
 * <p>
 * This class encapsulates the business rules regarding the registration of new plant varieties,
 * ensuring data integrity before persistence.
 * </p>
 */
public class CatalogService {

    private final PlantVarietyDAOContract dao;

    /**
     * Initializes the service with the persistence provider.
     *
     * @param dao The Data Access Object for plant varieties.
     */
    public CatalogService(PlantVarietyDAOContract dao) {
        this.dao = dao;
    }

    /**
     * Registers a new plant variety into the system catalog.
     *
     * @param sheet The data sheet containing variety details.
     * @return {@code true} if the operation was successful.
     */
    public boolean registerNewVariety(PlantVarietySheet sheet) {
        if (sheet.getVarietyName() == null || sheet.getVarietyName().isEmpty()) {
            return false;
        }
        return dao.save(sheet);
    }
}
