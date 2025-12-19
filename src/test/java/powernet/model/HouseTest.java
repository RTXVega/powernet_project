package powernet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

// Passe en revue House : création valide, calcul de demande, validation des identifiants
// (null/vides/espaces) et couverture des différents niveaux de consommation.
@DisplayName("Tests du modèle House")
class HouseTest {

    private House house;

    @BeforeEach
    void setUp() {
        house = new House("M1", Consumption.NORMAL);
    }

    // Vérifie la création d'une maison avec des valeurs valides.
    @Test
    @DisplayName("Doit créer une maison avec un identifiant et un niveau de consommation valides")
    void testHouseCreation() {
        assertThat(house).isNotNull();
        assertThat(house.getId()).isEqualTo("M1");
        assertThat(house.getLevel()).isEqualTo(Consumption.NORMAL);
    }

    // S'assure que la demande en kW correspond au niveau de consommation.
    @Test
    @DisplayName("Doit retourner la demande correcte en kW")
    void testDemandKw() {
        assertThat(house.demandKw()).isEqualTo(20);
    }

    // Vérifie les demandes pour différents niveaux de consommation.
    @Test
    @DisplayName("Doit gérer différents niveaux de consommation")
    void testDifferentConsumptionLevels() {
        House basse = new House("M2", Consumption.BASSE);
        House forte = new House("M3", Consumption.FORTE);

        assertThat(basse.demandKw()).isEqualTo(10);
        assertThat(forte.demandKw()).isEqualTo(40);
    }

    // Contrôle qu'un identifiant null déclenche une exception.
    @Test
    @DisplayName("Doit lever une exception pour un identifiant null")
    void testNullId() {
        assertThatThrownBy(() -> new House(null, Consumption.NORMAL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Identifiant maison invalide");
    }

    // Contrôle qu'un identifiant vide est refusé.
    @Test
    @DisplayName("Doit lever une exception pour un identifiant vide")
    void testEmptyId() {
        assertThatThrownBy(() -> new House("", Consumption.NORMAL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Identifiant maison invalide");
    }

    // Vérifie qu'un niveau de consommation null est rejeté.
    @Test
    @DisplayName("Doit lever une exception pour un niveau de consommation null")
    void testNullConsumption() {
        assertThatThrownBy(() -> new House("M1", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Niveau de consommation requis");
    }

    // S'assure que l'identifiant est bien tronqué des espaces.
    @Test
    @DisplayName("Doit tronquer l'identifiant avec des espaces")
    void testIdTrimming() {
        House houseWithSpaces = new House("  M1  ", Consumption.NORMAL);
        assertThat(houseWithSpaces.getId()).isEqualTo("M1");
    }

    // Valide plusieurs identifiants acceptables pour une maison.
    @ParameterizedTest
    @ValueSource(strings = {"M1", "M2", "M100", "HOUSE_1"})
    @DisplayName("Doit accepter plusieurs identifiants de maison valides")
    void testValidHouseIds(String id) {
        House h = new House(id, Consumption.NORMAL);
        assertThat(h.getId()).isEqualTo(id);
    }
}
