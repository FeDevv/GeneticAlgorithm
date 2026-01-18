package org.agroplanner.inventory.dao;

import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.model.PlantVarietySheet;

import java.util.List;
import java.util.Set;

public interface PlantVarietyDAOContract {

    void initStorage();

    boolean save(PlantVarietySheet sheet);

    List<PlantVarietySheet> findByType(PlantType type);
    List<PlantVarietySheet> findAll();

    PlantVarietySheet findById(int id);
    List<PlantVarietySheet> findAllByIds(Set<Integer> ids);
}
