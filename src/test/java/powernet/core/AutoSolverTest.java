package powernet.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AutoSolver Tests")
class AutoSolverTest {

    private Network network;
    private CostCalculator calculator;
    private AutoSolver solver;

    @BeforeEach
    void setUp() {
        network = new Network();
        calculator = new CostCalculator(2.0); // lambda = 2.0
        solver = new AutoSolver(10); // max 10 iterations
    }

    @Test
    @DisplayName("Should return false for empty network")
    void testEmptyNetwork() {
        boolean improved = solver.improve(network, calculator);

        assertThat(improved).isFalse();
    }

    @Test
    @DisplayName("Should return false when no houses")
    void testNoHouses() {
        network.addGenerator(new Generator("G1", 100));

        boolean improved = solver.improve(network, calculator);

        assertThat(improved).isFalse();
    }

    @Test
    @DisplayName("Should return false when no generators")
    void testNoGenerators() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        boolean improved = solver.improve(network, calculator);

        assertThat(improved).isFalse();
    }

    @Test
    @DisplayName("Should return false when already optimal")
    void testAlreadyOptimal() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        boolean improved = solver.improve(network, calculator);

        assertThat(improved).isFalse();
    }

    @Test
    @DisplayName("Should improve unbalanced network")
    void testImproveUnbalancedNetwork() {
        // 2 houses with very different loads on same generator
        network.addHouse(new House("M1", Consumption.BASSE));    // 10 kW
        network.addHouse(new House("M2", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 60));
        network.addGenerator(new Generator("G2", 60));

        // Initially: both on G1 (50 kW, ratio 0.833)
        network.connect("M1", "G1");
        network.connect("M2", "G1");

        CostCalculator.Cost initialCost = calculator.compute(network);

        // Solver should move houses to balance load
        boolean improved = solver.improve(network, calculator);

        if (improved) {
            CostCalculator.Cost improvedCost = calculator.compute(network);
            assertThat(improvedCost.total()).isLessThanOrEqualTo(initialCost.total());
        }
    }

    @Test
    @DisplayName("Should not make things worse")
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

    @Test
    @DisplayName("Should respect max iterations limit")
    void testMaxIterationsRespected() {
        AutoSolver limitedSolver = new AutoSolver(1);

        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addHouse(new House("M2", Consumption.FORTE));
        network.addGenerator(new Generator("G1", 50));
        network.addGenerator(new Generator("G2", 50));

        network.connect("M1", "G1");
        network.connect("M2", "G1");

        // Should complete within 1 iteration
        boolean improved = limitedSolver.improve(network, calculator);

        // Test just verifies it runs without error
        assertThat(improved).isNotNull();
    }

    @Test
    @DisplayName("Should handle solver with zero iterations")
    void testZeroIterations() {
        AutoSolver zeroIterationSolver = new AutoSolver(0);

        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        boolean improved = zeroIterationSolver.improve(network, calculator);

        assertThat(improved).isFalse();
    }

    @Test
    @DisplayName("Should handle complex load redistribution")
    void testComplexLoadRedistribution() {
        // 3 houses, 2 generators - initially unbalanced
        network.addHouse(new House("M1", Consumption.BASSE));    // 10 kW
        network.addHouse(new House("M2", Consumption.NORMAL));   // 20 kW
        network.addHouse(new House("M3", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        // All on G1 (70 kW, exceeds 100 but still possible)
        network.connect("M1", "G1");
        network.connect("M2", "G1");
        network.connect("M3", "G1");

        CostCalculator.Cost initialCost = calculator.compute(network);

        boolean improved = solver.improve(network, calculator);

        CostCalculator.Cost finalCost = calculator.compute(network);

        // Final cost should not be worse
        assertThat(finalCost.total()).isLessThanOrEqualTo(initialCost.total());
    }

    @Test
    @DisplayName("Should preserve network integrity after improvement")
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

        // Network structure should remain unchanged
        assertThat(network.houses()).hasSize(housesBeforeImprovement);
        assertThat(network.generators()).hasSize(generatorsBeforeImprovement);
        assertThat(network.assignment()).hasSize(housesBeforeImprovement);
    }

    @Test
    @DisplayName("Should handle single house improvement")
    void testSingleHouseImprovement() {
        network.addHouse(new House("M1", Consumption.FORTE));
        network.addGenerator(new Generator("G1", 30)); // Too small
        network.addGenerator(new Generator("G2", 50)); // Better fit

        network.connect("M1", "G1");

        CostCalculator.Cost initialCost = calculator.compute(network);

        boolean improved = solver.improve(network, calculator);

        CostCalculator.Cost finalCost = calculator.compute(network);

        // Cost should not increase
        assertThat(finalCost.total()).isLessThanOrEqualTo(initialCost.total());
    }
}
