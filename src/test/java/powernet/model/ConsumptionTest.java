package powernet.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests de l'énumération Consumption")
// Vérifie l'énumération : valeurs kW attendues pour chaque niveau, positivité et cardinalité totale.
class ConsumptionTest {

    // Vérifie la valeur kW associée au niveau BASSE.
    @Test
    @DisplayName("BASSE doit avoir la valeur 10 kW")
    void testBasseValue() {
        assertThat(Consumption.BASSE.getKw()).isEqualTo(10);
    }

    // Vérifie la valeur kW associée au niveau NORMAL.
    @Test
    @DisplayName("NORMAL doit avoir la valeur 20 kW")
    void testNormalValue() {
        assertThat(Consumption.NORMAL.getKw()).isEqualTo(20);
    }

    // Vérifie la valeur kW associée au niveau FORTE.
    @Test
    @DisplayName("FORTE doit avoir la valeur 40 kW")
    void testForteValue() {
        assertThat(Consumption.FORTE.getKw()).isEqualTo(40);
    }

    // S'assure que chaque niveau de consommation est strictement positif.
    @ParameterizedTest
    @ValueSource(strings = {"BASSE", "NORMAL", "FORTE"})
    @DisplayName("Tous les niveaux de consommation doivent avoir une valeur positive en kW")
    void testAllConsumptionLevelsPositive(String levelName) {
        Consumption level = Consumption.valueOf(levelName);
        assertThat(level.getKw()).isGreaterThan(0);
    }

    // Vérifie que l'énumération contient exactement trois niveaux.
    @Test
    @DisplayName("L'énumération Consumption doit contenir exactement 3 niveaux")
    void testConsumptionEnumSize() {
        assertThat(Consumption.values()).hasSize(3);
    }
    
}
