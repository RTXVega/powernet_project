package powernet.model;

/**
 * Représente un générateur électrique caractérisé par :
 * - un identifiant unique,
 * - une capacité maximale en kW.
 */
public class Generator {

    /** Identifiant du générateur (ex : G1). */
    private String id;

    /** Capacité maximale du générateur en kW. */
    private int capacityKw;

    /**
     * Construit un générateur avec un identifiant et une capacité donnée.
     * @param id identifiant du générateur
     * @param capacityKw capacité maximale en kW
     * @throws IllegalArgumentException si l'identifiant est invalide ou si la capacité est ≤ 0
     */
    public Generator(String id, int capacityKw) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Identifiant générateur invalide");
        }
        if (capacityKw <= 0) {
            throw new IllegalArgumentException("Capacité doit être > 0");
        }
        this.id = id.trim();
        this.capacityKw = capacityKw;
    }

    /**
     * @return l'identifiant du générateur
     */
    public String getId() {
        return id;
    }

    /**
     * @return la capacité maximale du générateur en kW
     */
    public int getCapacityKw() {
        return capacityKw;
    }
}
