package org.agroplanner.domainsystem.controllers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainFactory;
import org.agroplanner.domainsystem.model.DomainType;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DomainService {

    private final DomainFactory factory;

    // Singleton o Dependency Injection
    public DomainService() {
        this.factory = DomainFactory.getInstance();
    }

    /**
     * Restituisce la lista dei tipi di dominio supportati dal sistema.
     */
    public List<DomainType> getAvailableDomainTypes() {
        return Arrays.asList(DomainType.values());
    }

    /**
     * Tenta di creare un dominio dati i parametri.
     * @throws IllegalArgumentException se i parametri non rispettano la business logic interna del Dominio.
     */
    public Domain createDomain(DomainType type, Map<String, Double> params) {
        return factory.createDomain(type, params);
    }

    public double calculateMaxValidRadius(Domain domain) {
        Rectangle2D boundingBox = domain.getBoundingBox();
        double boxWidth = boundingBox.getWidth();
        double boxHeight = boundingBox.getHeight();
        // Il raggio non può essere più grande di metà del lato più corto
        return Math.min(boxWidth, boxHeight) / 2.0;
    }
}
