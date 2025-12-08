package powernet.core;

import java.util.*;

/**
 * Classe responsable du calcul de la dispersion, de la surcharge
 * et du coût total d'un réseau électrique.
 */
public class CostCalculator {

    /** Coefficient pondérant l'importance de la surcharge dans le calcul du coût total. */
    private final double lambda;

    /**
     * Structure de données utilisée pour regrouper les résultats du calcul :
     * dispersion, surcharge et coût total.
     */
    public static final class Cost {
        /** Dispersion des taux de charge entre générateurs. */
        private final double dispersion;

        /** Somme des surtensions des générateurs dépassant leur capacité. */
        private final double surcharge;

        /** Coût total : dispersion + lambda × surcharge. */
        private final double total;

        /**
         * Crée un objet regroupant les valeurs calculées.
         *
         * @param d dispersion mesurée
         * @param s surcharge totale
         * @param t coût total
         */
        public Cost(double d, double s, double t) {
            this.dispersion = d;
            this.surcharge = s;
            this.total = t;
        }

        /** @return la dispersion du réseau */
        public double dispersion() { return dispersion; }

        /** @return la surcharge totale du réseau */
        public double surcharge()  { return surcharge; }

        /** @return le coût total du réseau */
        public double total()      { return total; }
    }

    /**
     * Construit un calculateur utilisant un coefficient lambda donné.
     *
     * @param lambda facteur multiplicatif appliqué à la surcharge
     */
    public CostCalculator(double lambda) {
        this.lambda = lambda;
    }

    /**
     * Calcule la dispersion, la surcharge et le coût total du réseau donné.
     *
     * @param net réseau électrique à analyser
     * @return un objet {@link Cost} contenant toutes les valeurs calculées
     */
    public Cost compute(Network net) {
        Map<String, Integer> loads = net.computeLoadsKw();
        if (loads.isEmpty()) {
            return new Cost(0.0, 0.0, 0.0);
        }

        List<Double> ratios = new ArrayList<>();
        double surcharge = 0.0;

        // Calcul des taux de charge et de la surcharge
        for (Map.Entry<String, Integer> e : loads.entrySet()) {
            String gId = e.getKey();
            int L = e.getValue();
            int C = net.generators().get(gId).getCapacityKw();

            double r = (C == 0) ? 0.0 : ((double) L) / C;
            ratios.add(r);

            if (L > C) {
                surcharge += ((double) (L - C)) / C;
            }
        }

        // Moyenne des taux de charge
        double sum = 0.0;
        for (Double r : ratios) {
            sum += r;
        }
        double avg = ratios.isEmpty() ? 0.0 : sum / ratios.size();

        // Dispersion (somme des écarts à la moyenne)
        double dispersion = 0.0;
        for (Double r : ratios) {
            dispersion += Math.abs(r - avg);
        }

        double total = dispersion + lambda * surcharge;
        return new Cost(dispersion, surcharge, total);
    }
}
