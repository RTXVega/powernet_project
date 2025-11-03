package powernet.model;

/** Générateur: identifiant + capacité (kW). */
public class Generator {
    private String id; // ex: G1
    private int capacityKw;

    public Generator(String id, int capacityKw) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Identifiant générateur invalide");
        }
        if (capacityKw <= 0) {
            throw new IllegalArgumentException("Capacité doit être > 0");
        }
        this.id = id.trim();
        this.capacityKw = capacityKw;
    }
    public String getId() { return id; }
    public int getCapacityKw() { return capacityKw; }
}
