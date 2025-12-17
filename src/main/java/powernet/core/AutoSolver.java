package powernet.core;

import powernet.model.Generator;
import powernet.model.House;
import java.util.*;

/**
 * Solveur Hybride : Heuristique Constructive + Recuit Simulé.
 * Cette version est conçue pour trouver des coûts minimaux très rapidement
 * en commençant par une distribution intelligente avant d'optimiser.
 */
public class AutoSolver {

    private final int maxIterations;
    private final double initialTemperature;
    private final double coolingRate;

    public AutoSolver(int maxIterations) {
        this.maxIterations = maxIterations;
        this.initialTemperature = 1000.0; // Température plus élevée pour plus de liberté au début
        this.coolingRate = 0.9995; // Refroidissement très lent pour explorer finement
    }

    /**
     * Exécute l'optimisation hybride.
     */
    public boolean improve(Network net, CostCalculator calc) {
        if (net.houses().isEmpty() || net.generators().isEmpty()) {
            return false;
        }

        // --- ÉTAPE 1 : Initialisation Intelligente ---
        // On sauvegarde l'état actuel au cas où notre heuristique serait moins
        // performante
        // (peu probable mais prudent).
        Map<String, String> originalAssignment = new HashMap<>(net.assignment());
        double originalCost = calc.compute(net).total();

        // On applique l'algorithme "Best-Fit Decreasing" pour partir d'une base saine
        runBestFitDecreasing(net);

        // On évalue ce point de départ "intelligent"
        CostCalculator.Cost smartStartCostObj = calc.compute(net);
        double currentCost = smartStartCostObj.total();

        // Si l'état original était meilleur que le démarrage intelligent, on revient en
        // arrière
        if (originalCost < currentCost) {
            applyAssignment(net, originalAssignment);
            currentCost = originalCost;
        }

        // --- ÉTAPE 2 : Recuit Simulé ---
        // Maintenant que le point de départ est bon, on affine avec la métaheuristique.

        Map<String, String> bestAssignment = new HashMap<>(net.assignment());
        double bestCost = currentCost;

        List<String> houseIds = new ArrayList<>(net.houses().keySet());
        List<String> genIds = new ArrayList<>(net.generators().keySet());
        Random rand = new Random();

        double temperature = initialTemperature;

        for (int i = 0; i < maxIterations; i++) {

            // Stratégie de mouvement : 50% Échange (Swap), 50% Déplacement (Move)
            boolean isSwap = rand.nextBoolean() && houseIds.size() > 1;

            String houseA = houseIds.get(rand.nextInt(houseIds.size()));
            String oldGenA = net.assignment().get(houseA);

            String houseB = null;
            String oldGenB = null;

            if (isSwap) {
                // ÉCHANGE : On échange deux maisons entre deux générateurs
                houseB = houseIds.get(rand.nextInt(houseIds.size()));
                oldGenB = net.assignment().get(houseB);

                if (oldGenA.equals(oldGenB))
                    continue; // Inutile si même générateur

                // On applique l'échange
                net.connect(houseA, oldGenB);
                net.connect(houseB, oldGenA);
            } else {
                // DÉPLACEMENT : On déplace une maison vers un autre générateur
                String newGen = genIds.get(rand.nextInt(genIds.size()));

                if (newGen.equals(oldGenA))
                    continue; // Inutile

                // On applique le déplacement
                net.connect(houseA, newGen);
            }

            // Calcul du delta (gain ou perte)
            CostCalculator.Cost newCostObj = calc.compute(net);
            double newCost = newCostObj.total();
            double delta = newCost - currentCost;

            // Critère de Metropolis : On accepte si c'est mieux OU si la température le
            // permet
            if (delta < 0 || Math.exp(-delta / temperature) > rand.nextDouble()) {
                currentCost = newCost;

                // Mise à jour du meilleur score absolu
                if (currentCost < bestCost) {
                    bestCost = currentCost;
                    bestAssignment = new HashMap<>(net.assignment());
                }
            } else {
                // Annulation du mouvement (Rollback)
                net.connect(houseA, oldGenA);
                if (isSwap) {
                    net.connect(houseB, oldGenB);
                }
            }

            // Refroidissement
            temperature *= coolingRate;
        }

        // Application finale de la meilleure solution trouvée
        applyAssignment(net, bestAssignment);

        // Retourne true si on a réussi à battre le score original
        return bestCost < originalCost;
    }

    /**
     * Heuristique gloutonne : "Best-Fit Decreasing".
     * Trie les maisons par consommation décroissante et les place sur le générateur
     * ayant le plus de capacité restante (ou le taux d'utilisation le plus bas).
     */
    private void runBestFitDecreasing(Network net) {
        // 1. Récupérer toutes les maisons et les trier (plus grosse -> plus petite)
        List<House> sortedHouses = new ArrayList<>(net.houses().values());
        // Tri décroissant sur la demande (40kW avant 10kW)
        sortedHouses.sort((h1, h2) -> Integer.compare(h2.demandKw(), h1.demandKw()));

        // 2. Réinitialiser les connexions (virtuellement, car on va tout réassigner)
        // On a besoin de suivre la charge actuelle de chaque générateur pendant la
        // construction
        Map<String, Integer> currentLoad = new HashMap<>();
        for (String genId : net.generators().keySet()) {
            currentLoad.put(genId, 0);
        }

        // 3. Placement glouton
        for (House house : sortedHouses) {
            String bestGenId = null;
            double bestScore = Double.MAX_VALUE;

            // On cherche le générateur qui "souffrira le moins" d'accueillir cette maison
            for (Generator gen : net.generators().values()) {
                String genId = gen.getId();
                double capacity = gen.getCapacityKw();
                double load = currentLoad.get(genId);

                // Simulation : quel serait le taux d'utilisation si on ajoutait cette maison ?
                double newLoad = load + house.demandKw();
                double ratio = newLoad / capacity;

                // Critère : On veut le ratio le plus bas possible (pour équilibrer et éviter
                // surcharge)
                if (ratio < bestScore) {
                    bestScore = ratio;
                    bestGenId = genId;
                }
            }

            // Affectation
            if (bestGenId != null) {
                net.connect(house.getId(), bestGenId);
                currentLoad.put(bestGenId, currentLoad.get(bestGenId) + house.demandKw());
            }
        }
    }

    private void applyAssignment(Network net, Map<String, String> assignment) {
        for (Map.Entry<String, String> entry : assignment.entrySet()) {
            net.connect(entry.getKey(), entry.getValue());
        }
    }
}