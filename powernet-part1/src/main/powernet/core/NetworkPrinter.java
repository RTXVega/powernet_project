package powernet.core;

import java.util.*;
import powernet.model.*;

/** Affichages lisibles (texte). */
public class NetworkPrinter {
    public static String printSummary(Network net, CostCalculator.Cost cost) {
        StringBuilder sb = new StringBuilder();
        sb.append("GÉNÉRATEURS\n");
        Map<String,Integer> loads = net.computeLoadsKw();
        for (Generator g : net.generators().values()) {
            int L = loads.containsKey(g.getId()) ? loads.get(g.getId()) : 0;
            double ratio = (double)L / g.getCapacityKw();
            sb.append(String.format(" - %s: charge=%dkW / capacité=%dkW (r=%.3f)%n", g.getId(), L, g.getCapacityKw(), ratio));
        }
        sb.append("\nMAISONS\n");
        for (House m : net.houses().values()) {
            String g = net.assignment().get(m.getId());
            sb.append(String.format(" - %s (%s kW) -> %s%n", m.getId(), m.demandKw(), g == null ? "(non connecté)" : g));
        }
        sb.append("\nCOÛTS\n");
        sb.append(String.format(" Dispersion: %.6f%n", cost.dispersion()));
        sb.append(String.format(" Surcharge:  %.6f%n", cost.surcharge()));
        sb.append(String.format(" TOTAL:      %.6f%n", cost.total()));
        return sb.toString();
    }
}
