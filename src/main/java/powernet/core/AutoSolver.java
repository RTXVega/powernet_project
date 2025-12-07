package powernet.core;

import powernet.model.House;

import java.util.Map;

/**
 * Résolution automatique : amélioration du réseau
 * par recherche locale sur les affectations maison → générateur.
 */
public class AutoSolver {

    /** Nombre maximal de passes complètes sur l'ensemble des maisons. */
    private final int maxIterations;

    /**
     * Construit un solveur automatique avec un nombre maximal d'itérations.
     *
     * @param maxIterations nombre maximal de passes de recherche locale
     */
    public AutoSolver(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * Améliore (si possible) la répartition des maisons entre les générateurs.
     * L'algorithme parcourt les maisons et tente de les reconnecter vers
     * d'autres générateurs si cela diminue le coût total du réseau.
     *
     * @param net  réseau à modifier
     * @param calc calculateur de coût
     * @return true si au moins une amélioration a été trouvée, false sinon
     */
    public boolean improve(Network net, CostCalculator calc) {
        boolean improvedAtLeastOnce = false;

        if (net.houses().isEmpty() || net.generators().isEmpty()) {
            return false;
        }

        CostCalculator.Cost currentCost = calc.compute(net);

        for (int iter = 0; iter < maxIterations; iter++) {
            boolean improvedThisIteration = false;

            // Parcours de toutes les maisons
            for (House house : net.houses().values()) {
                String houseId = house.getId();
                String currentGen = net.assignment().get(houseId);

                // On essaie de connecter la maison à chaque générateur possible
                String bestGen = currentGen;
                CostCalculator.Cost bestCost = currentCost;

                for (String candidateGen : net.generators().keySet()) {
                    // Si déjà connecté à ce générateur, on ne teste pas
                    if (candidateGen.equals(currentGen)) {
                        continue;
                    }

                    // On sauvegarde l'affectation actuelle
                    String previousGen = currentGen;

                    // On applique une affectation temporaire
                    if (candidateGen != null) {
                        net.connect(houseId, candidateGen);
                    }

                    // On calcule le nouveau coût
                    CostCalculator.Cost candidateCost = calc.compute(net);

                    // Si mieux, on garde cette affectation comme meilleure candidate
                    if (candidateCost.total() < bestCost.total()) {
                        bestCost = candidateCost;
                        bestGen = candidateGen;
                    }

                    // On restaure l'affectation d'origine
                    if (previousGen != null) {
                        net.connect(houseId, previousGen);
                    }
                }

                // Si on a trouvé un meilleur générateur pour cette maison, on applique le changement
                if (bestGen != null && !bestGen.equals(currentGen)) {
                    net.connect(houseId, bestGen);
                    currentCost = bestCost;
                    improvedThisIteration = true;
                    improvedAtLeastOnce = true;
                }
            }

            // Si aucune amélioration pendant cette itération, on s'arrête
            if (!improvedThisIteration) {
                break;
            }
        }

        return improvedAtLeastOnce;
    }
}
