package powernet.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NetworkValidator Tests")
class NetworkValidatorTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = new Network();
    }

    @Test
    @DisplayName("Should return empty list for valid network")
    void testValidNetworkNoIssues() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));
        network.connect("M1", "G1");

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).isEmpty();
    }

    @Test
    @DisplayName("Should detect no houses")
    void testNoHouses() {
        network.addGenerator(new Generator("G1", 100));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Aucune maison");
    }

    @Test
    @DisplayName("Should detect no generators")
    void testNoGenerators() {
        network.addHouse(new House("M1", Consumption.NORMAL));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Aucun générateur");
    }

    @Test
    @DisplayName("Should detect empty network")
    void testEmptyNetwork() {
        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Aucune maison");
        assertThat(issues).contains("Aucun générateur");
    }

    @Test
    @DisplayName("Should detect single disconnected house")
    void testSingleDisconnectedHouse() {
        network.addHouse(new House("M1", Consumption.NORMAL));
        network.addGenerator(new Generator("G1", 100));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Maison non connectée: M1");
    }

    @Test
    @DisplayName("Should detect multiple disconnected houses")
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
    @DisplayName("Should not report issues when all houses are connected")
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
    @DisplayName("Should report combined issues")
    void testCombinedIssues() {
        // No generators and disconnected house
        network.addHouse(new House("M1", Consumption.NORMAL));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues)
            .contains("Aucun générateur")
            .contains("Maison non connectée: M1");
    }

    @Test
    @DisplayName("Should return list in consistent order")
    void testIssuesOrder() {
        // Call validate twice on same network
        network.addHouse(new House("M1", Consumption.NORMAL));

        List<String> issues1 = NetworkValidator.validate(network);
        List<String> issues2 = NetworkValidator.validate(network);

        assertThat(issues1).isEqualTo(issues2);
    }

    @Test
    @DisplayName("Should validate large network correctly")
    void testLargeNetwork() {
        // Add 100 houses
        for (int i = 1; i <= 100; i++) {
            network.addHouse(new House("M" + i, Consumption.NORMAL));
        }

        // Add only 50 generators
        for (int i = 1; i <= 50; i++) {
            network.addGenerator(new Generator("G" + i, 100));
        }

        // Connect only first 50 houses
        for (int i = 1; i <= 50; i++) {
            network.connect("M" + i, "G" + i);
        }

        List<String> issues = NetworkValidator.validate(network);

        // Should report 50 disconnected houses
        for (int i = 51; i <= 100; i++) {
            assertThat(issues).contains("Maison non connectée: M" + i);
        }
        assertThat(issues).hasSize(50);
    }

    @Test
    @DisplayName("Should handle houses with special characters in id")
    void testSpecialCharacterIds() {
        network.addHouse(new House("M-1_2", Consumption.NORMAL));
        network.addGenerator(new Generator("G-1_2", 100));

        List<String> issues = NetworkValidator.validate(network);

        assertThat(issues).contains("Maison non connectée: M-1_2");
    }
}
