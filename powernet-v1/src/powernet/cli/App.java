package powernet.cli;

import java.util.Scanner;

/**
 * Point d'entrée de l'application (PARTIE 1).
 * Cette classe initialise la valeur de lambda puis lance l'interface textuelle.
 */
public class App {

    /**
     * Méthode principale exécutée au lancement du programme.
     * @param args arguments de la ligne de commande. Option possible : --lambda <valeur>.
     */
    public static void main(String[] args) {

        double lambda = 10.0; // valeur par défaut

        for (int i = 0; i < args.length; i++) {
            if ("--lambda".equals(args[i]) && i + 1 < args.length) {
                try {
                    lambda = Double.parseDouble(args[++i]);
                } catch (NumberFormatException ignored) {
                    // valeur incorrecte : on garde la valeur par défaut
                }
            }
        }

        Scanner sc = new Scanner(System.in);
        ConsoleMenu menu = new ConsoleMenu(sc, lambda);
        menu.run();
    }
}
