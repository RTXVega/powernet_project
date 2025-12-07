package powernet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("House Model Tests")
class HouseTest {

    private House house;

    @BeforeEach
    void setUp() {
        house = new House("M1", Consumption.NORMAL);
    }

    @Test
    @DisplayName("Should create house with valid id and consumption level")
    void testHouseCreation() {
        assertThat(house).isNotNull();
        assertThat(house.getId()).isEqualTo("M1");
        assertThat(house.getLevel()).isEqualTo(Consumption.NORMAL);
    }

    @Test
    @DisplayName("Should get correct demand in kW")
    void testDemandKw() {
        assertThat(house.demandKw()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should handle different consumption levels")
    void testDifferentConsumptionLevels() {
        House basse = new House("M2", Consumption.BASSE);
        House forte = new House("M3", Consumption.FORTE);

        assertThat(basse.demandKw()).isEqualTo(10);
        assertThat(forte.demandKw()).isEqualTo(40);
    }

    @Test
    @DisplayName("Should throw exception for null id")
    void testNullId() {
        assertThatThrownBy(() -> new House(null, Consumption.NORMAL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Identifiant maison invalide");
    }

    @Test
    @DisplayName("Should throw exception for empty id")
    void testEmptyId() {
        assertThatThrownBy(() -> new House("", Consumption.NORMAL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Identifiant maison invalide");
    }

    @Test
    @DisplayName("Should throw exception for null consumption level")
    void testNullConsumption() {
        assertThatThrownBy(() -> new House("M1", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Niveau de consommation requis");
    }

    @Test
    @DisplayName("Should trim id with whitespace")
    void testIdTrimming() {
        House houseWithSpaces = new House("  M1  ", Consumption.NORMAL);
        assertThat(houseWithSpaces.getId()).isEqualTo("M1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"M1", "M2", "M100", "HOUSE_1"})
    @DisplayName("Should accept various valid house ids")
    void testValidHouseIds(String id) {
        House h = new House(id, Consumption.NORMAL);
        assertThat(h.getId()).isEqualTo(id);
    }
}
