package powernet.core;

import java.nio.file.Path;

public class SolverTest {
    public static void main(String[] args) throws Exception {
        NetworkParser parser = new NetworkParser();
        Network net = parser.parse(
                Path.of("/Users/bouhailanis/Personnel/UPC-L3/S5/PAA/Projet/powernet_project/optimization_test.txt"));

        CostCalculator calc = new CostCalculator(10.0);
        AutoSolver solver = new AutoSolver(100000); // High iterations to be sure

        System.out.println("Initial Cost: " + calc.compute(net).total());
        solver.improve(net, calc);
        CostCalculator.Cost finalCost = calc.compute(net);

        System.out.println("Final Cost: " + finalCost.total());
        System.out.println("Surcharge: " + finalCost.surcharge());
        System.out.println("Dispersion: " + finalCost.dispersion());

        // Print assignments to explain
        for (String hId : net.houses().keySet()) {
            String gId = net.assignment().get(hId);
            System.out.println(hId + " on " + gId);
        }
    }
}
