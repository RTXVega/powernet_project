package powernet.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

@DisplayName("Tests de la sauvegarde de réseau")
class NetworkWriterTest {

    @Test
    @DisplayName("Écrit le format attendu dans le fichier de sortie")
    void save_writesExpectedFormat() throws IOException {
        Network net = new Network();
        net.addGenerator(new Generator("G1", 60));
        net.addHouse(new House("H1", Consumption.BASSE));
        net.addHouse(new House("H2", Consumption.NORMAL));
        net.connect("H1", "G1");
        net.connect("H2", "G1");

        Path file = Files.createTempFile("network-writer", ".txt");

        new NetworkWriter().save(net, file);

        String content = Files.readString(file).replace("\r\n", "\n");
        String expected = String.join("\n",
                "generateur(G1,60).",
                "",
                "maison(H1,BASSE).",
                "maison(H2,NORMAL).",
                "",
                "connexion(G1,H1).",
                "connexion(G1,H2).",
                "");

        assertEquals(expected, content);
    }
}
