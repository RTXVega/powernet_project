package powernet.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Map;

import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

/**
 * Lecture d'un fichier texte décrivant un réseau
 * (générateurs, maisons, connexions) et construction
 * de l'objet {@link Network} correspondant.
 */
public class NetworkParser {

    /** Étapes possibles lors de la lecture du fichier. */
    private enum Section {
        NONE,
        GENERATEURS,
        MAISONS,
        CONNEXIONS
    }

    /**
     * Lit un fichier et construit le réseau correspondant.
     * Le fichier doit contenir des lignes de la forme :
     * <ul>
     *   <li>generateur(nom,capacite).</li>
     *   <li>maison(nom,NIVEAU).</li>
     *   <li>connexion(x,y).</li>
     * </ul>
     *
     * @param path chemin du fichier à lire
     * @return réseau construit à partir du fichier
     * @throws IOException en cas de problème d'accès au fichier
     * @throws ParseException en cas de format invalide ou de données incohérentes
     */
    public Network parse(Path path) throws IOException, ParseException {
        Network net = new Network();
        Section section = Section.NONE;
        int lineNumber = 0;

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // on ignore les lignes vides
                }
                ensureAllowedCharacters(line, lineNumber);

                // On exige un point final.
                if (!line.endsWith(".")) {
                    throw parseError(lineNumber, "Ligne doit se terminer par un point: " + line);
                }

                // On isole le mot-clé et les arguments entre parenthèses.
                int idxOpen = line.indexOf('(');
                int idxClose = line.lastIndexOf(')');
                if (idxOpen < 0 || idxClose < 0 || idxClose < idxOpen) {
                    throw parseError(lineNumber, "Parenthèses invalides: " + line);
                }

                String keyword = line.substring(0, idxOpen).trim();
                String argsPart = line.substring(idxOpen + 1, idxClose).trim();

                // Vérification du nombre d'arguments (séparés par une virgule).
                String[] parts = argsPart.split(",");
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }

                switch (keyword) {
                    case "generateur":
                        if (section == Section.MAISONS || section == Section.CONNEXIONS) {
                            throw parseError(lineNumber,
                                    "Les generateurs doivent être declares avant les maisons et les connexions.");
                        }
                        section = Section.GENERATEURS;
                        parseGeneratorLine(parts, net, lineNumber);
                        break;

                    case "maison":
                        if (section == Section.CONNEXIONS) {
                            throw parseError(lineNumber,
                                    "Les maisons doivent être declarees avant les connexions.");
                        }
                        section = Section.MAISONS;
                        parseHouseLine(parts, net, lineNumber);
                        break;

                    case "connexion":
                        section = Section.CONNEXIONS;
                        parseConnectionLine(parts, net, lineNumber);
                        break;

                    default:
                        throw parseError(lineNumber, "Mot-clé inconnu: " + keyword);
                }
            }
        }

        return net;
    }

    /**
     * Traite une ligne de type generateur(nom,capacite).
     */
    private void parseGeneratorLine(String[] parts, Network net, int lineNumber) throws ParseException {
        if (parts.length != 2) {
            throw parseError(lineNumber, "Format attendu: generateur(nom,capacite).");
        }
        String id = parts[0];
        String capStr = parts[1];

        int cap;
        try {
            cap = Integer.parseInt(capStr);
        } catch (NumberFormatException e) {
            throw parseError(lineNumber, "Capacite invalide pour generateur " + id + ": " + capStr);
        }
        if (cap <= 0) {
            throw parseError(lineNumber, "Capacite doit être > 0 pour generateur " + id);
        }

        try {
            net.addGenerator(new Generator(id, cap));
        } catch (IllegalArgumentException e) {
            throw parseError(lineNumber, e.getMessage());
        }
    }

    /**
     * Traite une ligne de type maison(nom,NIVEAU).
     */
    private void parseHouseLine(String[] parts, Network net, int lineNumber) throws ParseException {
        if (parts.length != 2) {
            throw parseError(lineNumber, "Format attendu: maison(nom,NIVEAU).");
        }
        String id = parts[0];
        String levelStr = parts[1];

        Consumption lvl;
        try {
            lvl = Consumption.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw parseError(lineNumber,
                    "Niveau de consommation invalide pour maison " + id + ": " + levelStr);
        }

        try {
            net.addHouse(new House(id, lvl));
        } catch (IllegalArgumentException e) {
            throw parseError(lineNumber, e.getMessage());
        }
    }

    /**
     * Traite une ligne de type connexion(x,y).
     * x et y doivent désigner exactement un generateur et une maison (dans n'importe quel ordre).
     */
    private void parseConnectionLine(String[] parts, Network net, int lineNumber) throws ParseException {
        if (parts.length != 2) {
            throw parseError(lineNumber, "Format attendu: connexion(objet1,objet2).");
        }
        String a = parts[0];
        String b = parts[1];

        Map<String, Generator> gens = net.generators();
        Map<String, House> houses = net.houses();

        boolean aIsGen = gens.containsKey(a);
        boolean bIsGen = gens.containsKey(b);
        boolean aIsHouse = houses.containsKey(a);
        boolean bIsHouse = houses.containsKey(b);

        String houseId;
        String genId;

        if (aIsGen && bIsHouse) {
            genId = a;
            houseId = b;
        } else if (aIsHouse && bIsGen) {
            genId = b;
            houseId = a;
        } else {
            throw parseError(lineNumber,
                    "Une connexion doit lier exactement un generateur et une maison: " + a + ", " + b);
        }

        // On interdit plusieurs connexions pour la même maison dans le fichier.
        if (net.assignment().containsKey(houseId)) {
            throw parseError(lineNumber,
                    "Maison " + houseId + " deja connectee dans le fichier.");
        }

        try {
            net.connect(houseId, genId);
        } catch (IllegalArgumentException e) {
            throw parseError(lineNumber, e.getMessage());
        }
    }

    /**
     * Construit une ParseException portant le numéro de ligne.
     */
    private ParseException parseError(int lineNumber, String message) {
        return new ParseException("Ligne " + lineNumber + " : " + message, lineNumber);
    }

    /**
     * Vérifie que la ligne ne contient que des caractères autorisés (alphanumériques
     * ou signes de ponctuation utilisés pour la syntaxe du fichier).
     */
    private void ensureAllowedCharacters(String line, int lineNumber) throws ParseException {
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if ((c >= '0' && c <= '9')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || c == '(' || c == ')' || c == ',' || c == '.'
                    || Character.isWhitespace(c)) {
                continue;
            }
            throw parseError(lineNumber, "Caractere invalide dans le fichier: '" + c + "'");
        }
    }
}
