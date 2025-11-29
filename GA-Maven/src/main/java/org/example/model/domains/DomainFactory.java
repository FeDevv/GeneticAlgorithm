package org.example.model.domains;

import org.example.model.domains.types.*;

import java.util.List;
import java.util.Map;

/**
 * Implementazione concreta del <b>Factory Method Pattern</b>.
 * <p>
 * Questa classe è responsabile esclusiva della creazione e della validazione preliminare
 * degli oggetti {@code Domain} concreti (es. {@code CircleDomain}, {@code RectangularDomain}).
 * Utilizza {@code DomainType} (i metadati) per sapere quali parametri sono necessari
 * e quale oggetto istanziare.
 * <p>
 * Aderisce al Single Responsibility Principle (SRP): il suo unico compito è la creazione.
 */
public class DomainFactory {

    /**
     * 1. Costruttore Privato.
     * Impedisce l'istanziazione diretta tramite 'new DomainFactory()'.
     */
    private DomainFactory() {
        // Logica di inizializzazione vuota (stateless)
    }

    /**
     * 2. Inner Class Statica (Lazy Holder).
     * Viene caricata in memoria dalla JVM solo quando viene invocato getInstance().
     * Garantisce la Thread-Safety nativa senza bisogno di 'synchronized'.
     */
    private static class FactoryHolder {
        private static final DomainFactory INSTANCE = new DomainFactory();
    }

    /**
     * 3. Punto di Accesso Globale.
     * Restituisce l'unica istanza esistente della factory.
     * @return L'istanza Singleton.
     */
    public static DomainFactory getInstance() {
        return FactoryHolder.INSTANCE;
    }

    // -----------------------------------------------------------
    // SEZIONE BUSINESS LOGIC (Invariata)
    // -----------------------------------------------------------

    /**
     * Metodo pubblico per creare un'istanza di un Domain.
     * È il punto d'ingresso del pattern Factory Method: delega la logica
     * di istanziazione specifica a un blocco {@code switch}.
     *
     * @param type Il tipo di dominio da creare, fornito tramite l'enum {@code DomainType}.
     * @param params La mappa dei parametri di configurazione.
     * @return L'istanza concreta del {@code Domain} richiesto.
     * @throws IllegalArgumentException Se i parametri sono mancanti/nulli o invalidi.
     */
    public Domain createDomain(DomainType type, Map<String, Double> params) {

        // 1. Validazione
        validateParameters(type, params);

        // 2. Creazione (switch-case sull'enum)
        return switch (type) {
            case CIRCLE ->
                    new CircleDomain(params.get("radius"));
            case RECTANGLE ->
                    new RectangleDomain(params.get("width"), params.get("height"));
            case SQUARE ->
                    new SquareDomain(params.get("side"));
            case ELLIPSE ->
                    new EllipseDomain(params.get("semi-width"), params.get("semi-height"));
            case RIGHT_ANGLED_TRIANGLE ->
                    new RATDomain(params.get("base"), params.get("height"));
            case FRAME ->
                    new FrameDomain(params.get("innerWidth"), params.get("innerHeight"), params.get("outerWidth"), params.get("outherHeight"));
            case ANNULUS ->
                    new AnnulusDomain(params.get("innerRadius"), params.get("outerRadius"));
        };
    }

    /**
     * Metodo helper privato responsabile della validazione.
     */
    private void validateParameters(DomainType type, Map<String, Double> params) {

        List<String> requiredKeys = type.getRequiredParameters();

        for (String key : requiredKeys) {
            Double value = params.get(key);

            // --- VALIDAZIONE 1: PRESENZA E NULLITÀ ---
            if (!params.containsKey(key) || value == null) {
                throw new IllegalArgumentException(
                        "Missing or null parameter for the domain '" + type.getDisplayName() + "': " + key
                );
            }

            // --- VALIDAZIONE 2: VALORE POSITIVO ---
            if (value <= 0) {
                throw new IllegalArgumentException(
                        "Invalid value for parameter '" + key + "' in domain '" + type.getDisplayName() + "'. " +
                                "Value must be strictly positive (> 0). Found: " + value
                );
            }
        }
    }
}
