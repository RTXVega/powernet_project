package powernet.core;

import java.util.*;
import powernet.model.*;

/**
 * Classe utilitaire responsable de l'affichage lisible d'un réseau électrique
 * et des coûts associés (dispersion, surcharge, total).
 */
public class NetworkPrinter {

    /**
     * Génère une représentation textuelle complète du réseau :
     * - liste des générateurs avec leurs charges et ratios,
     * - liste des maisons et leurs connexions,
     * - valeurs de dispersion, surcharge et coût total.
     *
     * @param net  réseau à afficher
     * @param cost résultats du calcul de coût associés au réseau
     * @return une chaîne de caractères contenant l'affichage formaté
     */
    public static String printSummary(Network net, CostCalculator.Cost cost) {
        StringBuilder sb = new StringBuilder();

        sb.append("GENERATEURS\n");
        Map<String, Integer> loads = net.computeLoadsKw();
        for (Generator g : net.generators().values()) {
            int L = loads.getOrDefault(g.getId(), 0);
            double ratio = (double) L / g.getCapacityKw();
            sb.append(String.format(
                    " - %s: charge=%dkW / capacite=%dkW (r=%.3f)%n",
                    g.getId(), L, g.getCapacityKw(), ratio));
        }

        sb.append("\nMAISONS\n");
        for (House m : net.houses().values()) {
            String g = net.assignment().get(m.getId());
            sb.append(String.format(
                    " - %s (%d kW) -> %s%n",
                    m.getId(), m.demandKw(), g == null ? "(non connectee)" : g));
        }

        sb.append("\nCOUTS\n");
        sb.append(String.format(" Dispersion: %.6f%n", cost.dispersion()));
        sb.append(String.format(" Surcharge:  %.6f%n", cost.surcharge()));
        sb.append(String.format(" TOTAL:      %.6f%n", cost.total()));

        return sb.toString();
    }
}
