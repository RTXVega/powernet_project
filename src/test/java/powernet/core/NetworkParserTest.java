package powernet.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class NetworkParserTest {

    @Test
    void parse_validFile_buildsNetwork() throws IOException {
        String content = String.join("\n",
                "generateur(G1,50).",
                "generateur(G2,80).",
                "",
                "maison(H1,BASSE).",
                "maison(H2,FORTE).",
                "",
                "connexion(G1,H1).",
                "connexion(H2,G2).",
                "");

        Path file = Files.createTempFile("network-parse-ok", ".txt");
        Files.writeString(file, content);

        NetworkParser parser = new NetworkParser();
        Network net = parser.parse(file);

        assertEquals(2, net.generators().size());
        assertEquals(2, net.houses().size());
        assertEquals("G1", net.assignment().get("H1"));
        assertEquals("G2", net.assignment().get("H2"));
    }

    @Test
    void parse_invalidConnection_throwsWithLineNumber() throws IOException {
        String content = String.join("\n",
                "generateur(G1,50).",
                "maison(H1,BASSE).",
                "connexion(H1,H1).",
                "");

        Path file = Files.createTempFile("network-parse-bad", ".txt");
        Files.writeString(file, content);

        NetworkParser parser = new NetworkParser();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parser.parse(file));
        // le message doit pointer la ligne fautive et mentionner la connexion invalide
        org.junit.jupiter.api.Assertions.assertTrue(
                ex.getMessage().contains("Ligne 3"),
                "Expected line number in message");
    }
}
