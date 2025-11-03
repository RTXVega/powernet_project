package powernet.model;

/** Maison: identifiant + type de consommation. */
public class House {
    private String id; // ex: M1
    private Consumption level;

    public House(String id, Consumption level) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Identifiant maison invalide");
        }
        if (level == null) {
            throw new IllegalArgumentException("Niveau de consommation requis");
        }
        this.id = id.trim();
        this.level = level;
    }
    public String getId() { return id; }
    public Consumption getLevel() { return level; }
    public int demandKw() { return level.getKw(); }
}
