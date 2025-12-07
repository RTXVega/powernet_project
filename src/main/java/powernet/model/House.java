package powernet.model;

/**
 * Représente une maison du réseau électrique.
 * Elle possède :
 * - un identifiant unique,
 * - un niveau de consommation défini par l'énumération.
 */
public class House {

    /** Identifiant de la maison (ex : M1). */
    private String id;

    /** Niveau de consommation de la maison. */
    private Consumption level;

    /**
     * Construit une maison avec un identifiant et un niveau de consommation.
     *
     * @param id identifiant de la maison
     * @param level niveau de consommation (BASSE, NORMAL ou FORTE)
     * @throws IllegalArgumentException si l'identifiant est invalide ou si le niveau est null
     */
    public House(String id, Consumption level) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Identifiant maison invalide");
        }
        if (level == null) {
            throw new IllegalArgumentException("Niveau de consommation requis");
        }
        this.id = id.trim();
        this.level = level;
    }

    /**
     * @return l'identifiant de la maison
     */
    public String getId() {
        return id;
    }

    /**
     * @return le niveau de consommation de la maison
     */
    public Consumption getLevel() {
        return level;
    }

    /**
     * @return la consommation en kW associée à cette maison
     */
    public int demandKw() {
        return level.getKw();
    }
}
