package powernet.model;

/** Niveau de consommation d'une maison (kW). */
public enum Consumption {
    BASSE(10), NORMAL(20), FORTE(40);
    private final int kw;
    Consumption(int kw) { this.kw = kw; }
    public int getKw() { return kw; }
}
