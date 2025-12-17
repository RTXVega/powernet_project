package powernet.core;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Tests des fichiers d'instance")
// Les coûts calculés doivent être égaux aux références du fichier Excel.
class InstanceTest {

    private static final double EPS_1E3 = 1e-3; // comparaison à 3 décimales

    @Test
    @DisplayName("Test instance1.txt")
    void TestInstance1() throws Exception {
        Path instancePath = Path.of("instances", "instance1.txt");
        double expectedCost = 0.698;
        double lambda = 10.0;

        Network net = new NetworkParser().parse(instancePath);
        CostCalculator calc = new CostCalculator(lambda);

        new AutoSolver(1000).improve(net, calc);
        double actualCost = calc.compute(net).total();

        assertEquals(expectedCost, actualCost, EPS_1E3);
    }

    @Test
    @DisplayName("Test instance2.txt")
    void TestInstance2() throws Exception {
        Path instancePath = Path.of("instances", "instance2.txt");
        double expectedCost = 1.059;
        double lambda = 10.0;

        Network net = new NetworkParser().parse(instancePath);
        CostCalculator calc = new CostCalculator(lambda);

        new AutoSolver(1000).improve(net, calc);
        double actualCost = calc.compute(net).total();

        assertEquals(expectedCost, actualCost, EPS_1E3);
    }

    @Test
    @DisplayName("Test instance3.txt")
    void TestInstance3() throws Exception {
        Path instancePath = Path.of("instances", "instance3.txt");
        double expectedCost = 0;
        double lambda = 10.0;

        Network net = new NetworkParser().parse(instancePath);
        CostCalculator calc = new CostCalculator(lambda);

        new AutoSolver(1000).improve(net, calc);
        double actualCost = calc.compute(net).total();

        assertEquals(expectedCost, actualCost, EPS_1E3);
    }

    @Test
    @DisplayName("Test instance4.txt")
    void TestInstance4() throws Exception {
        Path instancePath = Path.of("instances", "instance4.txt");
        double expectedCost = 0;
        double lambda = 10.0;

        Network net = new NetworkParser().parse(instancePath);
        CostCalculator calc = new CostCalculator(lambda);

        new AutoSolver(1000).improve(net, calc);
        double actualCost = calc.compute(net).total();

        assertEquals(expectedCost, actualCost, EPS_1E3);
    }
    @Test
    @DisplayName("Test instance5.txt")
    void TestInstance5() throws Exception {
        Path instancePath = Path.of("instances", "instance5.txt");
        double expectedCost = 1.511;
        double lambda = 10.0;

        Network net = new NetworkParser().parse(instancePath);
        CostCalculator calc = new CostCalculator(lambda);

        new AutoSolver(1000).improve(net, calc);
        double actualCost = calc.compute(net).total();

        assertEquals(expectedCost, actualCost, EPS_1E3);
    }
    @Test
    @DisplayName("Test instance6.txt")
    void TestInstance6() throws Exception {
        Path instancePath = Path.of("instances", "instance6.txt");
        double expectedCost = 0.755;
        double lambda = 10.0;

        Network net = new NetworkParser().parse(instancePath);
        CostCalculator calc = new CostCalculator(lambda);

        new AutoSolver(1000).improve(net, calc);
        double actualCost = calc.compute(net).total();

        assertEquals(expectedCost, actualCost, EPS_1E3);
    }
    @Test
    @DisplayName("Test instance7.txt")
    void TestInstance7() throws Exception {
        Path instancePath = Path.of("instances", "instance7.txt");
        double expectedCost = 5.094;
        double lambda = 10.0;

        Network net = new NetworkParser().parse(instancePath);
        CostCalculator calc = new CostCalculator(lambda);

        new AutoSolver(1000).improve(net, calc);
        double actualCost = calc.compute(net).total();

        assertEquals(expectedCost, actualCost, EPS_1E3);
    }
}
