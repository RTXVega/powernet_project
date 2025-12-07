package powernet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Consumption Enum Tests")
class ConsumptionTest {

    @Test
    @DisplayName("BASSE should have value 10 kW")
    void testBasseValue() {
        assertThat(Consumption.BASSE.getKw()).isEqualTo(10);
    }

    @Test
    @DisplayName("NORMAL should have value 20 kW")
    void testNormalValue() {
        assertThat(Consumption.NORMAL.getKw()).isEqualTo(20);
    }

    @Test
    @DisplayName("FORTE should have value 40 kW")
    void testForteValue() {
        assertThat(Consumption.FORTE.getKw()).isEqualTo(40);
    }

    @ParameterizedTest
    @ValueSource(strings = {"BASSE", "NORMAL", "FORTE"})
    @DisplayName("All consumption levels should have positive kW values")
    void testAllConsumptionLevelsPositive(String levelName) {
        Consumption level = Consumption.valueOf(levelName);
        assertThat(level.getKw()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Consumption enum should have exactly 3 levels")
    void testConsumptionEnumSize() {
        assertThat(Consumption.values()).hasSize(3);
    }
}
