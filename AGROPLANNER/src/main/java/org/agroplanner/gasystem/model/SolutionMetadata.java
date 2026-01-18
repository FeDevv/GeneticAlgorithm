package org.agroplanner.gasystem.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO leggero per visualizzare l'elenco dei salvataggi disponibili.
 */
public class SolutionMetadata {
    private final int id;
    private final String title;
    private final LocalDateTime creationDate;
    private final double fitness;

    public SolutionMetadata(int id, String title, LocalDateTime creationDate, double fitness) {
        this.id = id;
        this.title = title;
        this.creationDate = creationDate;
        this.fitness = fitness;
    }

    // --- GETTERS ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public double getFitness() { return fitness; }

    @Override
    public String toString() {
        return String.format("[%d] %s (Score: %.6f) - %s",
                id, title, fitness,
                creationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}
