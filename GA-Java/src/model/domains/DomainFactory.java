package model.domains;

import model.domains.types.*;

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
     * Metodo pubblico per creare un'istanza di Domain.
     * È il punto d'ingresso del pattern Factory Method: delega la logica
     * di istanziazione specifica a un blocco {@code switch}.
     *
     * @param type Il tipo di dominio da creare, fornito tramite l'enum {@code DomainType}.
     * @param params La mappa dei parametri di configurazione, dove la chiave è il nome del parametro
     * e il valore è il dato numerico (es. {"radius": 10.0}).
     * @return L'istanza concreta del {@code Domain} richiesto, trattata come interfaccia {@code Domain}.
     * @throws IllegalArgumentException Se i parametri sono mancanti/nulli o se il {@code DomainType}
     * non è supportato da questa factory.
     */
    public Domain createDomain(DomainType type, Map<String, Double> params) {

        // 1. Validazione: Controlla che tutti i parametri richiesti dall'enum siano presenti.
        // @implNote La validazione preventiva impedisce l'instanziazione con valori nulli/mancanti,
        // evitando potenziali eccezioni a runtime nei costruttori dei domini.
        validateParameters(type, params);

        // 2. Creazione (switch-case sull'enum)
        // Cuore del factory. L'uso dello switch expression è pulito e type-safe.
        return switch (type) {
            case CIRCLE ->
                // L'esistenza della chiave "raggio" è garantita dal metodo validateParameters.
                // La validazione del valore (raggio > 0) è gestita all'interno del costruttore di CircleDomain.
                new CircleDomain(params.get("radius"));
            case RECTANGLE ->
                // L'esistenza delle chiavi "larghezza" e "altezza" è garantita dalla validazione.
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
            /*
            default ->
                // Meccanismo di sicurezza: Cattura eventuali nuovi tipi non ancora gestiti nel factory.
                    throw new IllegalArgumentException("Domain type not supported " + type);

            */
        };

    }

    /**
     * Metodo helper privato responsabile della validazione della presenza e della non-nullità dei parametri.
     *
     * @param type Il tipo di dominio, usato per ottenere la lista dei nomi dei parametri obbligatori (metadati).
     * @param params La mappa dei parametri forniti dal codice client/utente.
     * @throws IllegalArgumentException Se un parametro obbligatorio è mancante o il suo valore è {@code null}.
     */
    private void validateParameters(DomainType type, Map<String, Double> params) {

        // 1. Ottiene la lista dei nomi dei parametri obbligatori dall'enum (metadati).
        List<String> requiredKeys = type.getRequiredParameters();

        // 2. Itera sui requisiti e controlla la mappa fornita.
        for (String key : requiredKeys) {
            Double value = params.get(key);

            // --- VALIDAZIONE 1: PRESENZA E NULLITÀ ---
            // Controlla se il parametro è assente o se il valore è nullo.
            if (!params.containsKey(key) || value == null) {
                // Lancia un errore specifico e descrittivo
                throw new IllegalArgumentException(
                        "Missing or null parameter for the domain '" + type.getDisplayName() + "': " + key
                );
            }

            // --- VALIDAZIONE 2: VALORE POSITIVO ---
            // Controlla che il valore sia strettamente maggiore di zero.
            // Larghezza, altezza e raggio, ... devono essere > 0.
            if (value <= 0) {
                // Lancia un errore specifico se il valore è invalido (negativo o zero).
                // teoricamente qua non ci si arriva mai, in quanto l'errore dato da valori non conformi
                // è gestito precedentemente.
                throw new IllegalArgumentException(
                        "Invalid value for parameter '" + key + "' in domain '" + type.getDisplayName() + "'. " +
                                "Value must be strictly positive (> 0). Found: " + value
                );
            }
        }
        // La validazione del limite superiore (es. raggio max) resta delegata al codice chiamante (View/Controller)
        // o al costruttore del Domain specifico per ragioni contestuali.
    }
}
