package powernet.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Network Core Tests")
class NetworkTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = new Network();
    }

    @Test
    @DisplayName("Should create empty network")
    void testCreateEmptyNetwork() {
        assertThat(network.houses()).isEmpty();
        assertThat(network.generators()).isEmpty();
        assertThat(network.assignment()).isEmpty();
    }

    @Test
    @DisplayName("Should add single house")
    void testAddSingleHouse() {
        House house = new House("M1", Consumption.NORMAL);
        network.addHouse(house);

        assertThat(network.houses()).hasSize(1);
        assertThat(network.houses()).containsKey("M1");
        assertThat(network.houses().get("M1")).isEqualTo(house);
    }

    @Test
    @DisplayName("Should add multiple houses")
    void testAddMultipleHouses() {
        network.addHouse(new House("M1", Consumption.BASSE));
        network.addHouse(new House("M2", Consumption.NORMAL));
        network.addHouse(new House("M3", Consumption.FORTE));

        assertThat(network.houses()).hasSize(3);
        assertThat(network.houses()).containsKeys("M1", "M2", "M3");
    }

    @Test
    @DisplayName("Should throw exception for duplicate house id")
    void testAddDuplicateHouse() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        assertThatThrownBy(() -> network.addHouse(new House("M1", Consumption.BASSE)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Maison déjà déclarée");
    }

    @Test
    @DisplayName("Should add single generator")
    void testAddSingleGenerator() {
        Generator generator = new Generator("G1", 100);
        network.addGenerator(generator);

        assertThat(network.generators()).hasSize(1);
        assertThat(network.generators()).containsKey("G1");
        assertThat(network.generators().get("G1")).isEqualTo(generator);
    }

    @Test
    @DisplayName("Should add multiple generators")
    void testAddMultipleGenerators() {
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 200));
        network.addGenerator(new Generator("G3", 150));

        assertThat(network.generators()).hasSize(3);
        assertThat(network.generators()).containsKeys("G1", "G2", "G3");
    }

    @Test
    @DisplayName("Should throw exception for duplicate generator id")
    void testAddDuplicateGenerator() {
        network.addGenerator(new Generator("G1", 100));

        assertThatThrownBy(() -> network.addGenerator(new Generator("G1", 150)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Générateur déjà déclaré");
    }

    @Test
    @DisplayName("Should connect house to generator")
    void testConnect() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        assertThat(network.assignment()).hasSize(1);
        assertThat(network.assignment().get("M1")).isEqualTo("G1");
    }

    @Test
    @DisplayName("Should reconnect house to different generator")
    void testReconnect() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        network.connect("M1", "G1");
        assertThat(network.assignment().get("M1")).isEqualTo("G1");

        network.connect("M1", "G2");
        assertThat(network.assignment().get("M1")).isEqualTo("G2");
    }

    @Test
    @DisplayName("Should throw exception when connecting unknown house")
    void testConnectUnknownHouse() {
        network.addGenerator(new Generator("G1", 100));

        assertThatThrownBy(() -> network.connect("UNKNOWN", "G1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Maison inconnue");
    }

    @Test
    @DisplayName("Should throw exception when connecting to unknown generator")
    void testConnectToUnknownGenerator() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        assertThatThrownBy(() -> network.connect("M1", "UNKNOWN"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Générateur inconnu");
    }

    @Test
    @DisplayName("Should remove connection")
    void testRemoveConnection() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        network.removeConnection("M1", "G1");

        assertThat(network.assignment()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent connection")
    void testRemoveNonExistentConnection() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));

        assertThatThrownBy(() -> network.removeConnection("M1", "G1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("n'a aucune connexion");
    }

    @Test
    @DisplayName("Should throw exception when removing mismatched connection")
    void testRemoveMismatchedConnection() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));
        network.connect("M1", "G1");

        assertThatThrownBy(() -> network.removeConnection("M1", "G2"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("n'existe pas");
    }

    @Test
    @DisplayName("Should compute loads for single house")
    void testComputeLoadsSingleHouse() {
        network.addHouse(new House("M1", Consumption.NORMAL)); // 20 kW
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        Map<String, Integer> loads = network.computeLoadsKw();

        assertThat(loads).hasSize(1);
        assertThat(loads.get("G1")).isEqualTo(20);
    }

    @Test
    @DisplayName("Should compute loads for multiple houses on same generator")
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

    @Test
    @DisplayName("Should compute loads for multiple generators")
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

    @Test
    @DisplayName("Should return zero loads for disconnected houses")
    void testComputeLoadsDisconnected() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addHouse(new House("M2", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));

        network.connect("M1", "G1");
        // M2 is not connected

        Map<String, Integer> loads = network.computeLoadsKw();

        assertThat(loads.get("G1")).isEqualTo(20); // Only M1's load
    }

    @Test
    @DisplayName("Should return zero load for generator with no houses")
    void testComputeLoadsEmptyGenerator() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.addGenerator(new Generator("G2", 100));

        network.connect("M1", "G1");

        Map<String, Integer> loads = network.computeLoadsKw();

        assertThat(loads.get("G1")).isEqualTo(20);
        assertThat(loads.get("G2")).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return empty loads for empty network")
    void testComputeLoadsEmptyNetwork() {
        Map<String, Integer> loads = network.computeLoadsKw();
        assertThat(loads).isEmpty();
    }

    @Test
    @DisplayName("Should return unmodifiable view of houses")
    void testHousesUnmodifiable() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        assertThatThrownBy(() -> network.houses().put("M2", new House("M2", Consumption.BASSE)))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should return unmodifiable view of generators")
    void testGeneratorsUnmodifiable() {
        network.addGenerator(new Generator("G1", 100));

        assertThatThrownBy(() -> network.generators().put("G2", new Generator("G2", 100)))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should return unmodifiable view of assignment")
    void testAssignmentUnmodifiable() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        assertThatThrownBy(() -> network.assignment().put("M2", "G1"))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
