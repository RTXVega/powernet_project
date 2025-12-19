package powernet.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

// Vérifie le rendu texte : formatage des générateurs, maisons et métriques de coût dans le résumé imprimé.
@DisplayName("Tests de l'impression de réseau")
class NetworkPrinterTest {

    // Vérifie le formatage du résumé texte pour les générateurs, maisons et coûts.
    @Test
    @DisplayName("Formate les générateurs, maisons et coûts dans le résumé")
    void printSummary_formatsGeneratorsHousesAndCosts() {
        Network net = new Network();
        net.addGenerator(new Generator("G1", 100));
        net.addGenerator(new Generator("G2", 50));
        net.addHouse(new House("H1", Consumption.NORMAL));
        net.addHouse(new House("H2", Consumption.FORTE));
        net.connect("H1", "G1");
        net.connect("H2", "G2");

        CostCalculator calc = new CostCalculator(1.0);
        CostCalculator.Cost cost = calc.compute(net);

        String summary = NetworkPrinter.printSummary(net, cost).replace("\r\n", "\n");
        // Normalise le séparateur décimal pour les locales qui utilisent la virgule.
        String normalized = summary.replace(',', '.');

        assertTrue(normalized.contains("GENERATEURS"));
        assertTrue(normalized.contains(" - G1: charge=20kW / capacite=100kW (r=0.200)\n"));
        assertTrue(normalized.contains(" - G2: charge=40kW / capacite=50kW (r=0.800)\n"));
        assertTrue(normalized.contains("MAISONS"));
        assertTrue(normalized.contains(" - H1 (20 kW) -> G1\n"));
        assertTrue(normalized.contains(" - H2 (40 kW) -> G2\n"));
        assertTrue(normalized.contains("Dispersion: 0.600000"));
        assertTrue(normalized.contains("Surcharge:  0.000000"));
        assertTrue(normalized.contains("TOTAL:      0.600000"));
    }
}
