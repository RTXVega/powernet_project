package powernet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

// Couvre Generator : création valide, validation des identifiants (null/vides/espaces) et des capacités
// (valeurs positives uniquement), plus tests paramétrés et borne supérieure.
@DisplayName("Tests du modèle Generator")
class GeneratorTest {

    private Generator generator;

    @BeforeEach
    void setUp() {
        generator = new Generator("G1", 100);
    }

    // Vérifie la création d'un générateur avec des valeurs valides.
    @Test
    @DisplayName("Doit créer un générateur avec un identifiant et une capacité valides")
    void testGeneratorCreation() {
        assertThat(generator).isNotNull();
        assertThat(generator.getId()).isEqualTo("G1");
        assertThat(generator.getCapacityKw()).isEqualTo(100);
    }

    // Contrôle qu'un identifiant null déclenche une exception.
    @Test
    @DisplayName("Doit lever une exception pour un identifiant null")
    void testNullId() {
        assertThatThrownBy(() -> new Generator(null, 100))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Identifiant générateur invalide");
    }

    // Vérifie que les identifiants vides sont refusés.
    @Test
    @DisplayName("Doit lever une exception pour un identifiant vide")
    void testEmptyId() {
        assertThatThrownBy(() -> new Generator("", 100))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Identifiant générateur invalide");
    }

    // S'assure qu'une capacité nulle est interdite.
    @Test
    @DisplayName("Doit lever une exception pour une capacité nulle")
    void testZeroCapacity() {
        assertThatThrownBy(() -> new Generator("G1", 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Capacité doit être > 0");
    }

    // S'assure qu'une capacité négative est rejetée.
    @Test
    @DisplayName("Doit lever une exception pour une capacité négative")
    void testNegativeCapacity() {
        assertThatThrownBy(() -> new Generator("G1", -50))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Capacité doit être > 0");
    }

    // Vérifie que les identifiants avec espaces sont tronqués.
    @Test
    @DisplayName("Doit tronquer l'identifiant avec des espaces")
    void testIdTrimming() {
        Generator genWithSpaces = new Generator("  G1  ", 100);
        assertThat(genWithSpaces.getId()).isEqualTo("G1");
    }

    // Valide plusieurs capacités acceptables.
    @ParameterizedTest
    @ValueSource(ints = {1, 50, 100, 1000, 10000})
    @DisplayName("Doit accepter plusieurs valeurs de capacité valides")
    void testValidCapacities(int capacity) {
        Generator gen = new Generator("G1", capacity);
        assertThat(gen.getCapacityKw()).isEqualTo(capacity);
    }

    // Valide plusieurs identifiants acceptables pour un générateur.
    @ParameterizedTest
    @ValueSource(strings = {"G1", "G2", "GEN_1", "GENERATOR_100"})
    @DisplayName("Doit accepter plusieurs identifiants de générateur valides")
    void testValidGeneratorIds(String id) {
        Generator gen = new Generator(id, 100);
        assertThat(gen.getId()).isEqualTo(id);
    }

    // Vérifie qu'une capacité très grande est gérée correctement.
    @Test
    @DisplayName("Doit gérer la capacité entière maximale")
    void testMaxCapacity() {
        Generator maxGen = new Generator("MAX", Integer.MAX_VALUE);
        assertThat(maxGen.getCapacityKw()).isEqualTo(Integer.MAX_VALUE);
    }
}
