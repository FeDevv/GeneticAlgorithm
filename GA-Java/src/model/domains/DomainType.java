package model.domains;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Rappresenta i tipi di domini geometrici disponibili nel sistema.
 * <p>
 * Questo enum funge da <b>repository di metadati</b> (Domain Metadata),
 * fornendo tutte le informazioni necessarie alla DomainFactory e alla View
 * (menu ID, nome visualizzato e parametri richiesti) per la creazione
 * e la gestione del dominio.
 * <p>
 * Aderisce al Single Responsibility Principle (SRP) mantenendo la logica di
 * creazione separata nella DomainFactory.
 */
public enum DomainType {

    // Le costanti dell'enum, ognuna configurata con il suo nome visualizzato
    // e la lista dei nomi dei parametri obbligatori per la sua creazione.

    // Dominio Circolare, richiede solo il raggio.
    CIRCLE(1,"CIRCLE", List.of("radius")),
    // Dominio Rettangolare, richiede larghezza e altezza.
    RECTANGLE(2,"RECTANGLE", List.of("width", "height")),
    // Dominio quadrato, richiede solo il lato
    SQUARE (3, "SQUARE", List.of("side")),
    //Dominio ellittico, richiede i due semiassi (la width è sull'asse x, la height sull'asse y)
    ELLIPSE(4, "ELLIPSE", List.of("semi-width", "semi-height")),
    //Dominio di triangolo rettangolo, richiede i due cateti, o base (sull'asse x a partire da (0,0)) e altezza (sull'asse y a partire da (0,0))
    RIGHT_ANGLED_TRIANGLE(5, "RIGHT ANGLED TRIANGLE", List.of("base", "height")),
    //Dominio a "frame", o cornice, richiede le dimensioni per i due rettangoli
    FRAME(6, "FRAME", List.of("innerWidth", "innerHeight","outerWidth", "outherHeight")),
    //Dominio a corona circolare, o annulus, richiede i raggi della circonferenza esterna e di quella interna
    ANNULUS(7, "ANNULUS", List.of("innerRadius", "outerRadius"));

    // ------------------- ATTRIBUTI -------------------

    // L'identificativo numerico univoco utilizzato per la selezione nel menu a riga di comando (CLI).
    private final int menuId;

    // Il nome "amichevole" del dominio, usato per la visualizzazione nelle interfacce utente e nei log.
    private final String displayName;

    // La lista dei nomi delle chiavi (String) che devono essere fornite nella Map<String, Double> dei parametri
    // alla DomainFactory per creare correttamente questo specifico tipo di dominio.
    private final List<String> requiredParameters;

    // ------------------- COSTRUTTORE -------------------

    /**
     * Costruttore privato (implicito per gli enum).
     * Inizializza le costanti dell'enum associando i metadati (ID, nome e requisiti) a ogni tipo.
     * @param menuId L'ID numerico per la selezione nel menu.
     * @param displayName Il nome del dominio da mostrare all'utente.
     * @param requiredParameters La lista dei nomi delle chiavi dei parametri attesi.
     */
    DomainType(int menuId, String displayName, List<String> requiredParameters) {
        this.menuId = menuId;
        this.displayName = displayName;
        this.requiredParameters = requiredParameters;
    }

    // ------------------- GETTER PUBBLICI -------------------

    /**
     * Restituisce l'ID numerico del dominio, utilizzato per la selezione da menu.
     * @return L'ID intero univoco.
     */
    public int getMenuId() { return menuId; }

    /**
     * Ritorna la lista dei nomi dei parametri richiesti per questo tipo di dominio.
     * <p>
     * Questo è cruciale per la {@code DomainFactory} (per la validazione) e per il {@code Controller} (per la raccolta dell'input).
     * @return Una lista immutabile di stringhe (i nomi delle chiavi).
     * @implNote Scelta Implementativa: {@code List.of()} crea una lista immutabile, garantendo che i requisiti
     * non possano essere modificati esternamente.
     */
    public List<String> getRequiredParameters() {
        return this.requiredParameters;
    }

    /**
     * Ritorna il nome "amichevole" del dominio per la visualizzazione.
     * @return Il nome da mostrare (es. "Cerchio").
     */
    public String getDisplayName() {
        return this.displayName;
    }

    // ------------------- METODI DI UTILITÀ -------------------

    /**
     * Cerca e restituisce un {@code DomainType} basato sull'ID del menu fornito.
     * Utile per mappare l'input numerico dell'utente alla costante enum corretta.
     * * @param id L'ID numerico selezionato dall'utente.
     * @return Un {@code Optional} contenente il {@code DomainType} corrispondente, se trovato.
     */
    public static Optional<DomainType> fromMenuId(int id) {
        return Arrays.stream(DomainType.values())
                .filter(type -> type.menuId == id)
                .findFirst();
    }

    // ------------------- OVERRIDE -------------------

    /**
     * Metodo standard di Java, sovrascritto per mostrare
     * il nome corretto nelle interfacce utente o nei log, utilizzando il nome amichevole.
     */
    @Override
    public String toString() {
        return this.displayName;
    }
}
