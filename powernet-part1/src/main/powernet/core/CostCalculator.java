package powernet.core;

import java.util.*;

/** Calculs Dispersion / Surcharge / Coût total. */
public class CostCalculator {
    private double lambda; // poids de la surcharge

    public static class Cost {
        private final double dispersion;
        private final double surcharge;
        private final double total;
        public Cost(double d, double s, double t) { this.dispersion=d; this.surcharge=s; this.total=t; }
        public double dispersion() { return dispersion; }
        public double surcharge()  { return surcharge; }
        public double total()      { return total; }
    }

    public CostCalculator(double lambda) { this.lambda = lambda; }

    public Cost compute(Network net) {
        Map<String, Integer> loads = net.computeLoadsKw();
        if (loads.isEmpty()) return new Cost(0,0,0);

        List<Double> ratios = new ArrayList<>();
        double surcharge = 0.0;
        for (Map.Entry<String,Integer> e : loads.entrySet()) {
            String gId = e.getKey();
            int L = e.getValue();
            int C = net.generators().get(gId).getCapacityKw();
            double r = C == 0 ? 0.0 : ((double) L) / C;
            ratios.add(r);
            if (L > C) surcharge += ((double)(L - C)) / C;
        }
        double sum = 0.0;
        for (Double r : ratios) sum += r;
        double avg = ratios.isEmpty() ? 0.0 : sum / ratios.size();
        double dispersion = 0.0;
        for (Double r : ratios) dispersion += Math.abs(r - avg);
        double total = dispersion + lambda * surcharge;
        return new Cost(dispersion, surcharge, total);
    }
}
