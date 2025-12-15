package powernet.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests de l'énumération Consumption")
class ConsumptionTest {

    @Test
    @DisplayName("BASSE doit avoir la valeur 10 kW")
    void testBasseValue() {
        assertThat(Consumption.BASSE.getKw()).isEqualTo(10);
    }

    @Test
    @DisplayName("NORMAL doit avoir la valeur 20 kW")
    void testNormalValue() {
        assertThat(Consumption.NORMAL.getKw()).isEqualTo(20);
    }

    @Test
    @DisplayName("FORTE doit avoir la valeur 40 kW")
    void testForteValue() {
        assertThat(Consumption.FORTE.getKw()).isEqualTo(40);
    }

    @ParameterizedTest
    @ValueSource(strings = {"BASSE", "NORMAL", "FORTE"})
    @DisplayName("Tous les niveaux de consommation doivent avoir une valeur positive en kW")
    void testAllConsumptionLevelsPositive(String levelName) {
        Consumption level = Consumption.valueOf(levelName);
        assertThat(level.getKw()).isGreaterThan(0);
    }

    @Test
    @DisplayName("L'énumération Consumption doit contenir exactement 3 niveaux")
    void testConsumptionEnumSize() {
        assertThat(Consumption.values()).hasSize(3);
    }
    
}
