package powernet.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.within;

@DisplayName("CostCalculator Tests")
class CostCalculatorTest {

    private Network network;
    private CostCalculator calculator;

    @BeforeEach
    void setUp() {
        network = new Network();
        calculator = new CostCalculator(2.0); // lambda = 2.0
    }

    @Test
    @DisplayName("Should return zero cost for empty network")
    void testEmptyNetworkCost() {
        CostCalculator.Cost cost = calculator.compute(network);

        assertThat(cost.dispersion()).isEqualTo(0.0);
        assertThat(cost.surcharge()).isEqualTo(0.0);
        assertThat(cost.total()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should calculate cost with single house and generator")
    void testSingleHouseAndGenerator() {
        network.addHouse(new House("M1", Consumption.NORMAL)); // 20 kW
        network.addGenerator(new Generator("G1", 100)); // 100 kW capacity
        network.connect("M1", "G1");

        CostCalculator.Cost cost = calculator.compute(network);

        // Load is 20/100 = 0.2 (no surcharge since 20 < 100)
        // Only one generator, dispersion = |0.2 - 0.2| = 0
        assertThat(cost.dispersion()).isEqualTo(0.0);
        assertThat(cost.surcharge()).isEqualTo(0.0);
        assertThat(cost.total()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should calculate dispersion with multiple generators")
    void testDispersionWithMultipleGenerators() {
        network.addHouse(new House("M1", Consumption.NORMAL));   // 20 kW
        network.addHouse(new House("M2", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        network.connect("M1", "G1"); // G1 load: 20/100 = 0.2
        network.connect("M2", "G2"); // G2 load: 40/100 = 0.4

        CostCalculator.Cost cost = calculator.compute(network);

        // Average ratio: (0.2 + 0.4) / 2 = 0.3
        // Dispersion: |0.2 - 0.3| + |0.4 - 0.3| = 0.1 + 0.1 = 0.2
        assertThat(cost.dispersion()).isEqualTo(0.2);
        assertThat(cost.surcharge()).isEqualTo(0.0);
        assertThat(cost.total()).isEqualTo(0.2);
    }

    @Test
    @DisplayName("Should calculate surcharge when load exceeds capacity")
    void testSurchargeCalculation() {
        network.addHouse(new House("M1", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 30));           // 30 kW capacity
        network.connect("M1", "G1");

        CostCalculator.Cost cost = calculator.compute(network);

        // Load is 40 kW, capacity is 30 kW
        // Surcharge = (40 - 30) / 30 = 10/30 = 0.333...
        assertThat(cost.surcharge()).isGreaterThan(0.0);
        assertThat(cost.total()).isGreaterThan(cost.dispersion());
    }

    @Test
    @DisplayName("Should apply lambda coefficient to surcharge in total cost")
    void testLambdaCoefficientApplication() {
        network.addHouse(new House("M1", Consumption.FORTE));
        network.addGenerator(new Generator("G1", 30));
        network.connect("M1", "G1");

        CostCalculator calculatorLambda1 = new CostCalculator(1.0);
        CostCalculator calculatorLambda2 = new CostCalculator(2.0);

        CostCalculator.Cost cost1 = calculatorLambda1.compute(network);
        CostCalculator.Cost cost2 = calculatorLambda2.compute(network);

        // Both should have same dispersion and surcharge
        assertThat(cost1.dispersion()).isEqualTo(cost2.dispersion());
        assertThat(cost1.surcharge()).isEqualTo(cost2.surcharge());

        // But total cost should differ by surcharge value
        assertThat(cost2.total()).isGreaterThan(cost1.total());
    }

    @Test
    @DisplayName("Should handle zero lambda coefficient")
    void testZeroLambda() {
        network.addHouse(new House("M1", Consumption.FORTE));
        network.addGenerator(new Generator("G1", 30));
        network.connect("M1", "G1");

        CostCalculator zeroLambda = new CostCalculator(0.0);
        CostCalculator.Cost cost = zeroLambda.compute(network);

        // With lambda = 0, surcharge doesn't contribute to total
        assertThat(cost.total()).isEqualTo(cost.dispersion());
    }

    @Test
    @DisplayName("Should calculate cost for multiple houses on same generator")
    void testMultipleHousesSameGenerator() {
        network.addHouse(new House("M1", Consumption.BASSE));    // 10 kW
        network.addHouse(new House("M2", Consumption.NORMAL));   // 20 kW
        network.addHouse(new House("M3", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 200));

        network.connect("M1", "G1");
        network.connect("M2", "G1");
        network.connect("M3", "G1");

        CostCalculator.Cost cost = calculator.compute(network);

        // Total load: 10 + 20 + 40 = 70 kW
        // Load ratio: 70/200 = 0.35
        // With one generator, no dispersion
        assertThat(cost.dispersion()).isEqualTo(0.0);
        assertThat(cost.surcharge()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should handle disconnected houses (not counted in load)")
    void testDisconnectedHouses() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addHouse(new House("M2", Consumption.FORTE)); // Not connected
        network.addGenerator(new Generator("G1", 100));

        network.connect("M1", "G1");

        CostCalculator.Cost cost = calculator.compute(network);

        // Only M1 (20 kW) is connected, M2 is ignored
        assertThat(cost.surcharge()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should calculate cost with complex scenario")
    void testComplexScenario() {
        // 3 houses, 2 generators
        network.addHouse(new House("M1", Consumption.BASSE));    // 10 kW
        network.addHouse(new House("M2", Consumption.NORMAL));   // 20 kW
        network.addHouse(new House("M3", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 50));
        network.addGenerator(new Generator("G2", 50));

        network.connect("M1", "G1"); // G1: 10 kW, ratio = 0.2
        network.connect("M2", "G1"); // G1: 30 kW, ratio = 0.6
        network.connect("M3", "G2"); // G2: 40 kW, ratio = 0.8

        CostCalculator.Cost cost = calculator.compute(network);

        // Average ratio: (0.6 + 0.8) / 2 = 0.7
        // Dispersion: |0.6 - 0.7| + |0.8 - 0.7| = 0.1 + 0.1 = 0.2
        assertThat(cost.dispersion()).isCloseTo(0.2, within(1e-10));
        assertThat(cost.surcharge()).isEqualTo(0.0); // No surcharge
    }

    @Test
    @DisplayName("Cost object should provide dispersion, surcharge and total")
    void testCostObjectGetters() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        CostCalculator.Cost cost = calculator.compute(network);

        assertThat(cost.dispersion()).isNotNull();
        assertThat(cost.surcharge()).isNotNull();
        assertThat(cost.total()).isNotNull();
    }
}
