package powernet.core;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

// Couvre l'API Network : ajout/validation des nœuds, connexions et reconnexions, calculs de charge,
// cohérence des suppressions et immutabilité des vues exposées (maisons, générateurs, affectations).
@DisplayName("Tests du cœur du réseau")
class NetworkTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = new Network();
    }

    // Vérifie qu'un réseau nouvellement créé est vide.
    @Test
    @DisplayName("Doit créer un réseau vide")
    void testCreateEmptyNetwork() {
        assertThat(network.houses()).isEmpty();
        assertThat(network.generators()).isEmpty();
        assertThat(network.assignment()).isEmpty();
    }

    // S'assure qu'une maison peut être ajoutée et retrouvée.
    @Test
    @DisplayName("Doit ajouter une maison")
    void testAddSingleHouse() {
        House house = new House("M1", Consumption.NORMAL);
        network.addHouse(house);

        assertThat(network.houses()).hasSize(1);
        assertThat(network.houses()).containsKey("M1");
        assertThat(network.houses().get("M1")).isEqualTo(house);
    }

    // Vérifie l'ajout de plusieurs maisons distinctes.
    @Test
    @DisplayName("Doit ajouter plusieurs maisons")
    void testAddMultipleHouses() {
        network.addHouse(new House("M1", Consumption.BASSE));
        network.addHouse(new House("M2", Consumption.NORMAL));
        network.addHouse(new House("M3", Consumption.FORTE));

        assertThat(network.houses()).hasSize(3);
        assertThat(network.houses()).containsKeys("M1", "M2", "M3");
    }

    // Contrôle que l'ajout d'une maison en double lève une exception.
    @Test
    @DisplayName("Doit lever une exception pour un identifiant de maison en double")
    void testAddDuplicateHouse() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        assertThatThrownBy(() -> network.addHouse(new House("M1", Consumption.BASSE)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Maison déjà déclarée");
    }

    // S'assure qu'un générateur peut être ajouté au réseau.
    @Test
    @DisplayName("Doit ajouter un générateur")
    void testAddSingleGenerator() {
        Generator generator = new Generator("G1", 100);
        network.addGenerator(generator);

        assertThat(network.generators()).hasSize(1);
        assertThat(network.generators()).containsKey("G1");
        assertThat(network.generators().get("G1")).isEqualTo(generator);
    }

    // Vérifie l'ajout de plusieurs générateurs distincts.
    @Test
    @DisplayName("Doit ajouter plusieurs générateurs")
    void testAddMultipleGenerators() {
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 200));
        network.addGenerator(new Generator("G3", 150));

        assertThat(network.generators()).hasSize(3);
        assertThat(network.generators()).containsKeys("G1", "G2", "G3");
    }

    // Contrôle qu'un identifiant de générateur dupliqué déclenche une erreur.
    @Test
    @DisplayName("Doit lever une exception pour un identifiant de générateur en double")
    void testAddDuplicateGenerator() {
        network.addGenerator(new Generator("G1", 100));

        assertThatThrownBy(() -> network.addGenerator(new Generator("G1", 150)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Générateur déjà déclaré");
    }

    // Vérifie qu'une maison peut être connectée à un générateur.
    @Test
    @DisplayName("Doit connecter une maison à un générateur")
    void testConnect() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        assertThat(network.assignment()).hasSize(1);
        assertThat(network.assignment().get("M1")).isEqualTo("G1");
    }

    // S'assure qu'une maison peut être reconnectée à un autre générateur.
    @Test
    @DisplayName("Doit reconnecter une maison à un autre générateur")
    void testReconnect() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        network.connect("M1", "G1");
        assertThat(network.assignment().get("M1")).isEqualTo("G1");

        network.connect("M1", "G2");
        assertThat(network.assignment().get("M1")).isEqualTo("G2");
    }

    // Vérifie qu'une maison inconnue ne peut pas être connectée.
    @Test
    @DisplayName("Doit lever une exception lors de la connexion d'une maison inconnue")
    void testConnectUnknownHouse() {
        network.addGenerator(new Generator("G1", 100));

        assertThatThrownBy(() -> network.connect("UNKNOWN", "G1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Maison inconnue");
    }

    // Vérifie qu'un générateur inconnu ne peut pas être utilisé pour une connexion.
    @Test
    @DisplayName("Doit lever une exception lors de la connexion à un générateur inconnu")
    void testConnectToUnknownGenerator() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        assertThatThrownBy(() -> network.connect("M1", "UNKNOWN"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Générateur inconnu");
    }

    // Contrôle la suppression d'une connexion existante.
    @Test
    @DisplayName("Doit supprimer une connexion")
    void testRemoveConnection() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        network.removeConnection("M1", "G1");

        assertThat(network.assignment()).isEmpty();
    }

    // Vérifie qu'on ne peut pas supprimer une connexion inexistante.
    @Test
    @DisplayName("Doit lever une exception lors de la suppression d'une connexion inexistante")
    void testRemoveNonExistentConnection() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));

        assertThatThrownBy(() -> network.removeConnection("M1", "G1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("n'a aucune connexion");
    }

    // Vérifie qu'une connexion incohérente n'est pas supprimable.
    @Test
    @DisplayName("Doit lever une exception lors de la suppression d'une connexion incohérente")
    void testRemoveMismatchedConnection() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));
        network.connect("M1", "G1");

        assertThatThrownBy(() -> network.removeConnection("M1", "G2"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("n'existe pas");
    }

    // Calcule la charge lorsqu'une seule maison est connectée.
    @Test
    @DisplayName("Doit calculer la charge pour une seule maison")
    void testComputeLoadsSingleHouse() {
        network.addHouse(new House("M1", Consumption.NORMAL)); // 20 kW
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        Map<String, Integer> loads = network.computeLoadsKw();

        assertThat(loads).hasSize(1);
        assertThat(loads.get("G1")).isEqualTo(20);
    }

    // Calcule la charge cumulée de plusieurs maisons sur le même générateur.
    @Test
    @DisplayName("Doit calculer la charge de plusieurs maisons sur le même générateur")
    void testComputeLoadsMultipleHouses() {
        network.addHouse(new House("M1", Consumption.BASSE));    // 10 kW
        network.addHouse(new House("M2", Consumption.NORMAL));   // 20 kW
        network.addHouse(new House("M3", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 200));

        network.connect("M1", "G1");
        network.connect("M2", "G1");
        network.connect("M3", "G1");

        Map<String, Integer> loads = network.computeLoadsKw();

        assertThat(loads.get("G1")).isEqualTo(70); // 10 + 20 + 40
    }

    // Mesure les charges séparées sur plusieurs générateurs.
    @Test
    @DisplayName("Doit calculer les charges pour plusieurs générateurs")
    void testComputeLoadsMultipleGenerators() {
        network.addHouse(new House("M1", Consumption.NORMAL));   // 20 kW
        network.addHouse(new House("M2", Consumption.FORTE));    // 40 kW
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        network.connect("M1", "G1");
        network.connect("M2", "G2");

        Map<String, Integer> loads = network.computeLoadsKw();

        assertThat(loads).hasSize(2);
        assertThat(loads.get("G1")).isEqualTo(20);
        assertThat(loads.get("G2")).isEqualTo(40);
    }

    // Vérifie qu'une maison déconnectée ne contribue pas à la charge.
    @Test
    @DisplayName("Doit retourner 0 de charge pour les maisons déconnectées")
    void testComputeLoadsDisconnected() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addHouse(new House("M2", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));

        network.connect("M1", "G1");
        // M2 n'est pas connectée

        Map<String, Integer> loads = network.computeLoadsKw();

        assertThat(loads.get("G1")).isEqualTo(20); // Charge uniquement de M1
    }

    // S'assure qu'un générateur sans maison a une charge nulle.
    @Test
    @DisplayName("Doit retourner une charge nulle pour un générateur sans maison")
    void testComputeLoadsEmptyGenerator() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        network.connect("M1", "G1");

        Map<String, Integer> loads = network.computeLoadsKw();

        assertThat(loads.get("G1")).isEqualTo(20);
        assertThat(loads.get("G2")).isEqualTo(0);
    }

    // Vérifie le résultat lorsque le réseau est vide.
    @Test
    @DisplayName("Doit retourner des charges vides pour un réseau vide")
    void testComputeLoadsEmptyNetwork() {
        Map<String, Integer> loads = network.computeLoadsKw();
        assertThat(loads).isEmpty();
    }

    // Vérifie que la vue des maisons ne peut pas être modifiée.
    @Test
    @DisplayName("Doit renvoyer une vue non modifiable des maisons")
    void testHousesUnmodifiable() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        assertThatThrownBy(() -> network.houses().put("M2", new House("M2", Consumption.BASSE)))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    // Vérifie que la vue des générateurs est protégée contre les modifications.
    @Test
    @DisplayName("Doit renvoyer une vue non modifiable des générateurs")
    void testGeneratorsUnmodifiable() {
        network.addGenerator(new Generator("G1", 100));

        assertThatThrownBy(() -> network.generators().put("G2", new Generator("G2", 100)))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    // Vérifie que la vue des affectations est immuable.
    @Test
    @DisplayName("Doit renvoyer une vue non modifiable des affectations")
    void testAssignmentUnmodifiable() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        assertThatThrownBy(() -> network.assignment().put("M2", "G1"))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
