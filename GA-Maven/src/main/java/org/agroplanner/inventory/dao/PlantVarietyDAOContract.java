package org.agroplanner.inventory.dao;

import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.model.PlantVarietySheet;

import java.util.List;
import java.util.Set;

public interface PlantVarietyDAOContract {
    // Inizializza lo storage (Crea tabella SQL o crea File se non esiste)
    void initStorage();

    // Salva una nuova scheda
    boolean save(PlantVarietySheet sheet);

    // Metodi di ricerca
    List<PlantVarietySheet> findByType(PlantType type);
    List<PlantVarietySheet> findAll();

    PlantVarietySheet findById(int id);
    List<PlantVarietySheet> findAllByIds(Set<Integer> ids);
}
