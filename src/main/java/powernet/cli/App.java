package powernet.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Scanner;
import powernet.core.CostCalculator;
import powernet.core.Network;
import powernet.core.NetworkParser;
import powernet.core.NetworkPrinter;

/**
 * Point d'entrée de l'application.
 * Sans fichier : interface textuelle PARTIE 1.
 * Avec fichier : lecture du réseau et menu PARTIE 2.
 */
public class App {

    /**
     * Méthode principale exécutée au lancement du programme.
     * Arguments possibles :
     * - <fichier>              : charge un réseau depuis ce fichier (PARTIE 2)
     * - --lambda <valeur>      : modifie le coefficient lambda
     */
    public static void main(String[] args) {

        double lambda = 10.0; // valeur par défaut
        String inputFile = null;

        // Parcours des arguments : on récupère éventuellement un fichier et/ou un lambda.
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if ("--lambda".equals(arg) && i + 1 < args.length) {
                try {
                    lambda = Double.parseDouble(args[++i]);
                } catch (NumberFormatException e) {
                    // valeur incorrecte : on garde la valeur par défaut
                }
            } else if (!arg.startsWith("--") && inputFile == null) {
                // Premier argument qui n'est pas une option : considéré comme fichier d'entrée
                inputFile = arg;
            }
        }

        // Aucun fichier → PARTIE 1 : construction interactive du réseau
        if (inputFile == null) {
            Scanner sc = new Scanner(System.in);
            ConsoleMenu menu = new ConsoleMenu(sc, lambda);
            menu.run();
            return;
        }

        // Fichier fourni → PARTIE 2 : lecture du réseau puis menu V2
        try {
            NetworkParser parser = new NetworkParser();
            Network net = parser.parse(Path.of(inputFile));

            // Affichage initial du réseau lu
            CostCalculator calc = new CostCalculator(lambda);
            CostCalculator.Cost cost = calc.compute(net);
            System.out.println("=== Reseau charge depuis le fichier: " + inputFile + " ===");
            System.out.println(NetworkPrinter.printSummary(net, cost));

            // Lancement du menu PARTIE 2
            Scanner sc = new Scanner(System.in);
            ConsoleMenuV2 menuV2 = new ConsoleMenuV2(sc, net, lambda);
            menuV2.run();

        } catch (IOException e) {
            System.err.println("Erreur d'entree/sortie lors de la lecture du fichier : " + e.getMessage());
        } catch (ParseException e) {
            System.err.println("Erreur de format dans le fichier : " + e.getMessage());
        }
    }
}
