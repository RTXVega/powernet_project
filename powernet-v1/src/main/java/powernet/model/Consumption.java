package powernet.model;

/**
 * Enumération représentant les niveaux de consommation possibles
 * pour une maison, exprimés en kW.
 */
public enum Consumption {

    /** Consommation faible (10 kW). */
    BASSE(10),

    /** Consommation normale (20 kW). */
    NORMAL(20),

    /** Consommation forte (40 kW). */
    FORTE(40);

    /** Valeur numérique associée au niveau de consommation. */
    private final int kw;

    /**
     * Constructeur interne de l’énumération.
     * @param kw valeur de consommation en kW
     */
    Consumption(int kw) {
        this.kw = kw;
    }

    /**
     * @return la consommation (en kW) associée à ce niveau
     */
    public int getKw() {
        return kw;
    }
}
