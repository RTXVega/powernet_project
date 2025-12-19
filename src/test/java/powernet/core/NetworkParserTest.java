package powernet.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Vérifie le parseur : construction d'un réseau à partir d'un fichier valide et qualité des messages
// d'erreur (ligne concernée) pour connexions invalides ou caractères illégaux.
@DisplayName("Tests du parseur de réseau")
class NetworkParserTest {

    // Vérifie qu'un fichier valide est correctement parsé en réseau.
    @Test
    @DisplayName("Doit construire le réseau depuis un fichier valide")
    void parse_validFile_buildsNetwork() throws IOException, ParseException {
        Path file = Path.of("instances", "instance1.txt");

        NetworkParser parser = new NetworkParser();
        Network net = parser.parse(file);

        assertEquals(6, net.generators().size());
        assertEquals(9, net.houses().size());
        assertEquals("gen1", net.assignment().get("maison1"));
        assertEquals("gen2", net.assignment().get("maison5"));
        assertEquals("gen4", net.assignment().get("maison7"));
    }

    // Contrôle que les connexions invalides déclenchent une exception avec numéro de ligne.
    @Test
    @DisplayName("Signale la ligne lorsqu'une connexion est invalide")
    void parse_invalidConnection_throwsWithLineNumber() throws IOException {
        String content = String.join("\n",
                "generateur(G1,50).",
                "maison(H1,BASSE).",
                "connexion(H1,H1).",
                "");

        Path file = Files.createTempFile("network-parse-bad", ".txt");
        Files.writeString(file, content);

        NetworkParser parser = new NetworkParser();

        ParseException ex = assertThrows(ParseException.class, () -> parser.parse(file));
        // le message doit pointer la ligne fautive et mentionner la connexion invalide
        org.junit.jupiter.api.Assertions.assertTrue(
                ex.getMessage().contains("Ligne 3"),
                "Expected line number in message");
    }

    // Vérifie le signalement de caractères non alphanumériques avec la ligne concernée.
    @Test
    @DisplayName("Refuse les caracteres non alphanumeriques en signalant la ligne")
    void parse_invalidCharacter_reportsLine() throws IOException {
        String content = "generateur(G1,50$).";
        Path file = Files.createTempFile("network-parse-char", ".txt");
        Files.writeString(file, content);

        NetworkParser parser = new NetworkParser();

        ParseException ex = assertThrows(ParseException.class, () -> parser.parse(file));
        org.junit.jupiter.api.Assertions.assertTrue(
                ex.getMessage().contains("Ligne 1"),
                "Expected line number in message");
    }
}
