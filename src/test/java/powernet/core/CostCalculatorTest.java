package powernet.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

@DisplayName("Tests du calculateur de coût")
class CostCalculatorTest {

    private Network network;
    private CostCalculator calculator;

    @BeforeEach
    void setUp() {
        network = new Network();
        calculator = new CostCalculator(2.0); // lambda = 2,0
    }

    @Test
    @DisplayName("Retourner 0 pour un réseau vide")
    void testEmptyNetworkCost() {
        CostCalculator.Cost cost = calculator.compute(network);

        assertThat(cost.dispersion()).isEqualTo(0.0);
        assertThat(cost.surcharge()).isEqualTo(0.0);
        assertThat(cost.total()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Calculer le coût pour 1 maison et 1 générateur")
    void testSingleHouseAndGenerator() {
        network.addHouse(new House("M1", Consumption.NORMAL)); // 20 kW
        network.addGenerator(new Generator("G1", 100)); // capacité 100 kW
        network.connect("M1", "G1");

        CostCalculator.Cost cost = calculator.compute(network);

        // Charge = 20/100 = 0,2 (pas de surcharge car 20 < 100)
        // Un seul générateur, dispersion = |0,2 - 0,2| = 0
        assertThat(cost.dispersion()).isEqualTo(0.0);
        assertThat(cost.surcharge()).isEqualTo(0.0);
        assertThat(cost.total()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Doit calculer la dispersion avec plusieurs générateurs")
    void testDispersionWithMultipleGenerators() {
        network.addHouse(new House("M1", Consumption.NORMAL));   // 20 kW
        network.addHouse(new House("M2", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        network.connect("M1", "G1"); // Charge G1 : 20/100 = 0,2
        network.connect("M2", "G2"); // Charge G2 : 40/100 = 0,4

        CostCalculator.Cost cost = calculator.compute(network);

        // Ratio moyen : (0,2 + 0,4) / 2 = 0,3
        // Dispersion : |0,2 - 0,3| + |0,4 - 0,3| = 0,1 + 0,1 = 0,2
        assertThat(cost.dispersion()).isEqualTo(0.2);
        assertThat(cost.surcharge()).isEqualTo(0.0);
        assertThat(cost.total()).isEqualTo(0.2);
    }

    @Test
    @DisplayName("Doit calculer la surcharge quand la charge dépasse la capacité")
    void testSurchargeCalculation() {
        network.addHouse(new House("M1", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 30));           // capacité 30 kW
        network.connect("M1", "G1");

        CostCalculator.Cost cost = calculator.compute(network);

        // Charge = 40 kW, capacité = 30 kW
        // Surcharge = (40 - 30) / 30 = 10/30 = 0,333...
        assertThat(cost.surcharge()).isGreaterThan(0.0);
        assertThat(cost.total()).isGreaterThan(cost.dispersion());
    }

    @Test
    @DisplayName("Doit appliquer le coefficient lambda à la surcharge dans le coût total")
    void testLambdaCoefficientApplication() {
        network.addHouse(new House("M1", Consumption.FORTE));
        network.addGenerator(new Generator("G1", 30));
        network.connect("M1", "G1");

        CostCalculator calculatorLambda1 = new CostCalculator(1.0);
        CostCalculator calculatorLambda2 = new CostCalculator(2.0);

        CostCalculator.Cost cost1 = calculatorLambda1.compute(network);
        CostCalculator.Cost cost2 = calculatorLambda2.compute(network);

        // Les deux calculs doivent avoir la même dispersion et la même surcharge
        assertThat(cost1.dispersion()).isEqualTo(cost2.dispersion());
        assertThat(cost1.surcharge()).isEqualTo(cost2.surcharge());

        // Mais le coût total doit différer de la valeur de surcharge appliquée
        assertThat(cost2.total()).isGreaterThan(cost1.total());
    }

    @Test
    @DisplayName("Doit gérer un coefficient lambda nul")
    void testZeroLambda() {
        network.addHouse(new House("M1", Consumption.FORTE));
        network.addGenerator(new Generator("G1", 30));
        network.connect("M1", "G1");

        CostCalculator zeroLambda = new CostCalculator(0.0);
        CostCalculator.Cost cost = zeroLambda.compute(network);

        // Avec lambda = 0, la surcharge ne contribue pas au total
        assertThat(cost.total()).isEqualTo(cost.dispersion());
    }

    @Test
    @DisplayName("Doit calculer le coût pour plusieurs maisons sur un même générateur")
    void testMultipleHousesSameGenerator() {
        network.addHouse(new House("M1", Consumption.BASSE));    // 10 kW
        network.addHouse(new House("M2", Consumption.NORMAL));   // 20 kW
        network.addHouse(new House("M3", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 200));

        network.connect("M1", "G1");
        network.connect("M2", "G1");
        network.connect("M3", "G1");

        CostCalculator.Cost cost = calculator.compute(network);

        // Charge totale : 10 + 20 + 40 = 70 kW
        // Ratio de charge : 70/200 = 0,35
        // Avec un seul générateur, pas de dispersion
        assertThat(cost.dispersion()).isEqualTo(0.0);
        assertThat(cost.surcharge()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Doit gérer les maisons déconnectées (ignorées dans la charge)")
    void testDisconnectedHouses() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addHouse(new House("M2", Consumption.FORTE)); // Non connectée
        network.addGenerator(new Generator("G1", 100));

        network.connect("M1", "G1");

        CostCalculator.Cost cost = calculator.compute(network);

        // Seule M1 (20 kW) est connectée, M2 est ignorée
        assertThat(cost.surcharge()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Doit calculer le coût dans un scénario complexe")
    void testComplexScenario() {
        // 3 maisons, 2 générateurs
        network.addHouse(new House("M1", Consumption.BASSE));    // 10 kW
        network.addHouse(new House("M2", Consumption.NORMAL));   // 20 kW
        network.addHouse(new House("M3", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 50));
        network.addGenerator(new Generator("G2", 50));

        network.connect("M1", "G1"); // G1 : 10 kW, ratio = 0,2
        network.connect("M2", "G1"); // G1 : 30 kW, ratio = 0,6
        network.connect("M3", "G2"); // G2 : 40 kW, ratio = 0,8

        CostCalculator.Cost cost = calculator.compute(network);

        // Ratio moyen : (0,6 + 0,8) / 2 = 0,7
        // Dispersion : |0,6 - 0,7| + |0,8 - 0,7| = 0,1 + 0,1 = 0,2
        assertThat(cost.dispersion()).isCloseTo(0.2, within(1e-10));
        assertThat(cost.surcharge()).isEqualTo(0.0); // Pas de surcharge
    }

    @Test
    @DisplayName("L'objet Cost doit fournir dispersion, surcharge et total")
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
