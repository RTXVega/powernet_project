package powernet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Generator Model Tests")
class GeneratorTest {

    private Generator generator;

    @BeforeEach
    void setUp() {
        generator = new Generator("G1", 100);
    }

    @Test
    @DisplayName("Should create generator with valid id and capacity")
    void testGeneratorCreation() {
        assertThat(generator).isNotNull();
        assertThat(generator.getId()).isEqualTo("G1");
        assertThat(generator.getCapacityKw()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should throw exception for null id")
    void testNullId() {
        assertThatThrownBy(() -> new Generator(null, 100))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Identifiant générateur invalide");
    }

    @Test
    @DisplayName("Should throw exception for empty id")
    void testEmptyId() {
        assertThatThrownBy(() -> new Generator("", 100))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Identifiant générateur invalide");
    }

    @Test
    @DisplayName("Should throw exception for zero capacity")
    void testZeroCapacity() {
        assertThatThrownBy(() -> new Generator("G1", 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Capacité doit être > 0");
    }

    @Test
    @DisplayName("Should throw exception for negative capacity")
    void testNegativeCapacity() {
        assertThatThrownBy(() -> new Generator("G1", -50))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Capacité doit être > 0");
    }

    @Test
    @DisplayName("Should trim id with whitespace")
    void testIdTrimming() {
        Generator genWithSpaces = new Generator("  G1  ", 100);
        assertThat(genWithSpaces.getId()).isEqualTo("G1");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 50, 100, 1000, 10000})
    @DisplayName("Should accept various valid capacity values")
    void testValidCapacities(int capacity) {
        Generator gen = new Generator("G1", capacity);
        assertThat(gen.getCapacityKw()).isEqualTo(capacity);
    }

    @ParameterizedTest
    @ValueSource(strings = {"G1", "G2", "GEN_1", "GENERATOR_100"})
    @DisplayName("Should accept various valid generator ids")
    void testValidGeneratorIds(String id) {
        Generator gen = new Generator(id, 100);
        assertThat(gen.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should handle maximum integer capacity")
    void testMaxCapacity() {
        Generator maxGen = new Generator("MAX", Integer.MAX_VALUE);
        assertThat(maxGen.getCapacityKw()).isEqualTo(Integer.MAX_VALUE);
    }
}
