package model.domains;

import model.Individual;

import java.awt.geom.Rectangle2D;

public interface Domain {
    //controlla se un punto è all'interno del dominio
    boolean isPointOutside(double x, double y);

    //controlla se un individuo è valido (tutti i suoi punti sono nel dominio)
    boolean isValidIndividual(Individual individual);

    //bounding box per la generazione di punti casuali
    Rectangle2D getBoundingBox();
}
