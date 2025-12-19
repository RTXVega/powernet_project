package powernet.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

// Valide le détecteur d'anomalies réseau : présence de maisons/générateurs, connexions manquantes,
// stabilité de l'ordre des messages et détection sur des réseaux de grande taille.
@DisplayName("Tests du validateur de réseau")
class NetworkValidatorTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = new Network();
    }

    // Vérifie qu'aucune anomalie n'est détectée pour un réseau valide.
    @Test
    @DisplayName("Doit renvoyer une liste vide pour un réseau valide")
    void testValidNetworkNoIssues() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).isEmpty();
    }

    // Détecte l'absence de maisons dans le réseau.
    @Test
    @DisplayName("Doit détecter l'absence de maisons")
    void testNoHouses() {
        network.addGenerator(new Generator("G1", 100));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Aucune maison");
    }

    // Détecte l'absence de générateurs dans le réseau.
    @Test
    @DisplayName("Doit détecter l'absence de générateurs")
    void testNoGenerators() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Aucun générateur");
    }

    // Vérifie la détection d'un réseau complètement vide.
    @Test
    @DisplayName("Doit détecter un réseau vide")
    void testEmptyNetwork() {
        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Aucune maison");
        assertThat(issues).contains("Aucun générateur");
    }

    // Signale une maison non connectée à un générateur.
    @Test
    @DisplayName("Doit détecter une maison non connectée")
    void testSingleDisconnectedHouse() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Maison non connectée: M1");
    }

    // Signale plusieurs maisons non connectées et ignore celles reliées.
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

    // Vérifie qu'aucun problème n'est remonté quand toutes les maisons sont connectées.
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

    // Combine plusieurs problèmes pour vérifier leur détection simultanée.
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

    // Vérifie que l'ordre des messages est stable entre deux validations identiques.
    @Test
    @DisplayName("Doit retourner une liste dans un ordre cohérent")
    void testIssuesOrder() {
        // Appeler validate deux fois sur le même réseau
        network.addHouse(new House("M1", Consumption.NORMAL));

        List<String> issues1 = NetworkValidator.validate(network);
        List<String> issues2 = NetworkValidator.validate(network);

        assertThat(issues1).isEqualTo(issues2);
    }

    // Teste la validation sur un grand réseau partiellement connecté.
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

    // Vérifie la prise en compte d'identifiants contenant des caractères spéciaux.
    @Test
    @DisplayName("Doit gérer les maisons avec caractères spéciaux dans l'identifiant")
    void testSpecialCharacterIds() {
        network.addHouse(new House("M-1_2", Consumption.NORMAL));
        network.addGenerator(new Generator("G-1_2", 100));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Maison non connectée: M-1_2");
    }
}
