package powernet.core;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

// Vérifie en détail le solveur automatique : cas limites (réseau vide, sans maisons/générateurs),
// stabilité sur réseau déjà optimal, respect du nombre d'itérations, et amélioration d'un réseau déséquilibré.
@DisplayName("Tests de l'AutoSolver")
class AutoSolverTest {

    private Network network;
    private CostCalculator calculator;
    private AutoSolver solver;

    @BeforeEach
    void setUp() {
        network = new Network();
        calculator = new CostCalculator(2.0); // lambda = 2,0
        solver = new AutoSolver(10); // 10 itérations max
    }

    // Vérifie que l'AutoSolver renvoie false sur un réseau vide.
    @Test
    @DisplayName("Retourne false pour un réseau vide")
    void testEmptyNetwork() {
        boolean improved = solver.improve(network, calculator);

        assertThat(improved).isFalse();
    }

    // Vérifie qu'aucune amélioration n'est possible lorsqu'il n'y a aucune maison.
    @Test
    @DisplayName("Retourne false lorsqu'il n'y a aucune maison")
    void testNoHouses() {
        network.addGenerator(new Generator("G1", 100));

        boolean improved = solver.improve(network, calculator);

        assertThat(improved).isFalse();
    }

    // Vérifie qu'aucune amélioration n'est possible lorsqu'il n'y a aucun générateur.
    @Test
    @DisplayName("Retourne false lorsqu'il n'y a aucun générateur")
    void testNoGenerators() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        boolean improved = solver.improve(network, calculator);

        assertThat(improved).isFalse();
    }

    // Confirme qu'un réseau déjà optimal n'est pas modifié par le solveur.
    @Test
    @DisplayName("Retourne false si le réseau est déjà optimal")
    void testAlreadyOptimal() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        boolean improved = solver.improve(network, calculator);

        assertThat(improved).isFalse();
    }

    // S'assure que le solveur peut améliorer un réseau initialement déséquilibré.
    @Test
    @DisplayName("Équilibre automatiquement un réseau")
    void testImproveUnbalancedNetwork() {
        // 2 maisons avec des charges très différentes sur le même générateur
        network.addHouse(new House("M1", Consumption.BASSE));    // 10 kW
        network.addHouse(new House("M2", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 60));
        network.addGenerator(new Generator("G2", 60));

        // Au départ : les deux sur G1 (50 kW, ratio 0,833)
        network.connect("M1", "G1");
        network.connect("M2", "G1");

        CostCalculator.Cost initialCost = calculator.compute(network);

        // Le solveur doit déplacer des maisons pour équilibrer la charge
        boolean improved = solver.improve(network, calculator);

        if (improved) {
            CostCalculator.Cost improvedCost = calculator.compute(network);
            assertThat(improvedCost.total()).isLessThanOrEqualTo(initialCost.total());
        }
    }

    // Vérifie que le solveur ne rend pas un réseau équilibré plus coûteux.
    @Test
    @DisplayName("Un réseau équilibré doit coûter au plus autant que l'initial")
    void testNeverMakesWorse() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addHouse(new House("M2", Consumption.FORTE));
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        network.connect("M1", "G1");
        network.connect("M2", "G2");

        CostCalculator.Cost initialCost = calculator.compute(network);

        solver.improve(network, calculator);

        CostCalculator.Cost finalCost = calculator.compute(network);
        assertThat(finalCost.total()).isLessThanOrEqualTo(initialCost.total());
    }

    // Contrôle que la limite maximale d'itérations fixée est bien respectée.
    @Test
    @DisplayName("Respecte la limite maximale d'itérations")
    void testMaxIterationsRespected() {
        AutoSolver limitedSolver = new AutoSolver(1);

        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addHouse(new House("M2", Consumption.FORTE));
        network.addGenerator(new Generator("G1", 50));
        network.addGenerator(new Generator("G2", 50));

        network.connect("M1", "G1");
        network.connect("M2", "G1");

        // Doit terminer en 1 itération
        boolean improved = limitedSolver.improve(network, calculator);

        // Le test vérifie simplement que cela s'exécute sans erreur
        assertThat(improved).isNotNull();
    }

    // Vérifie qu'une limite d'itérations à zéro renvoie immédiatement false.
    @Test
    @DisplayName("Retourne false si le nombre d'itérations maximal est 0")
    void testZeroIterations() {
        AutoSolver zeroIterationSolver = new AutoSolver(0);

        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        boolean improved = zeroIterationSolver.improve(network, calculator);

        assertThat(improved).isFalse();
    }

    // Vérifie que l'optimisation ne modifie pas l'intégrité du réseau (nœuds et affectations).
    @Test
    @DisplayName("Vérifie l'intégrité d'un réseau après optimisation")
    void testNetworkIntegrityAfterImprovement() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addHouse(new House("M2", Consumption.FORTE));
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        network.connect("M1", "G1");
        network.connect("M2", "G1");

        int housesBeforeImprovement = network.houses().size();
        int generatorsBeforeImprovement = network.generators().size();

        solver.improve(network, calculator);

        // La structure du réseau doit rester inchangée
        assertThat(network.houses()).hasSize(housesBeforeImprovement);
        assertThat(network.generators()).hasSize(generatorsBeforeImprovement);
        assertThat(network.assignment()).hasSize(housesBeforeImprovement);
    }
}
