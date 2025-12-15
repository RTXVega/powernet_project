package powernet.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests du validateur de réseau")
class NetworkValidatorTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = new Network();
    }

    @Test
    @DisplayName("Doit renvoyer une liste vide pour un réseau valide")
    void testValidNetworkNoIssues() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).isEmpty();
    }

    @Test
    @DisplayName("Doit détecter l'absence de maisons")
    void testNoHouses() {
        network.addGenerator(new Generator("G1", 100));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Aucune maison");
    }

    @Test
    @DisplayName("Doit détecter l'absence de générateurs")
    void testNoGenerators() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Aucun générateur");
    }

    @Test
    @DisplayName("Doit détecter un réseau vide")
    void testEmptyNetwork() {
        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Aucune maison");
        assertThat(issues).contains("Aucun générateur");
    }

    @Test
    @DisplayName("Doit détecter une maison non connectée")
    void testSingleDisconnectedHouse() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Maison non connectée: M1");
    }

    @Test
    @DisplayName("Doit détecter plusieurs maisons non connectées")
    void testMultipleDisconnectedHouses() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addHouse(new House("M2", Consumption.BASSE));
        network.addHouse(new House("M3", Consumption.FORTE));
        network.addGenerator(new Generator("G1", 100));

        network.connect("M2", "G1");

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues)
            .contains("Maison non connectée: M1")
            .contains("Maison non connectée: M3")
            .doesNotContain("Maison non connectée: M2");
    }

    @Test
    @DisplayName("Ne doit pas signaler de problèmes quand toutes les maisons sont connectées")
    void testAllHousesConnected() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addHouse(new House("M2", Consumption.BASSE));
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        network.connect("M1", "G1");
        network.connect("M2", "G2");

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).isEmpty();
    }

    @Test
    @DisplayName("Doit signaler les problèmes combinés")
    void testCombinedIssues() {
        // Aucun générateur et maison déconnectée
        network.addHouse(new House("M1", Consumption.NORMAL));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues)
            .contains("Aucun générateur")
            .contains("Maison non connectée: M1");
    }

    @Test
    @DisplayName("Doit retourner une liste dans un ordre cohérent")
    void testIssuesOrder() {
        // Appeler validate deux fois sur le même réseau
        network.addHouse(new House("M1", Consumption.NORMAL));

        List<String> issues1 = NetworkValidator.validate(network);
        List<String> issues2 = NetworkValidator.validate(network);

        assertThat(issues1).isEqualTo(issues2);
    }

    @Test
    @DisplayName("Doit valider correctement un grand réseau")
    void testLargeNetwork() {
        // Ajouter 100 maisons
        for (int i = 1; i <= 100; i++) {
            network.addHouse(new House("M" + i, Consumption.NORMAL));
        }

        // Ajouter seulement 50 générateurs
        for (int i = 1; i <= 50; i++) {
            network.addGenerator(new Generator("G" + i, 100));
        }

        // Connecter uniquement les 50 premières maisons
        for (int i = 1; i <= 50; i++) {
            network.connect("M" + i, "G" + i);
        }

        List<String> issues = NetworkValidator.validate(network);

        // Doit signaler 50 maisons déconnectées
        for (int i = 51; i <= 100; i++) {
            assertThat(issues).contains("Maison non connectée: M" + i);
        }
        assertThat(issues).hasSize(50);
    }

    @Test
    @DisplayName("Doit gérer les maisons avec caractères spéciaux dans l'identifiant")
    void testSpecialCharacterIds() {
        network.addHouse(new House("M-1_2", Consumption.NORMAL));
        network.addGenerator(new Generator("G-1_2", 100));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Maison non connectée: M-1_2");
    }
}
