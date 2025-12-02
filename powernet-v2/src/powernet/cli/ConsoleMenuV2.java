package powernet.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;
import powernet.core.AutoSolver;
import powernet.core.CostCalculator;
import powernet.core.Network;
import powernet.core.NetworkPrinter;
import powernet.core.NetworkWriter;

/**
 * Interface textuelle pour la PARTIE 2.
 * Permet de travailler sur un réseau chargé depuis un fichier :
 * - affichage du réseau et du coût,
 * - résolution automatique (amélioration de la répartition),
 * - sauvegarde de la solution.
 */
public class ConsoleMenuV2 {

    /** Scanner utilisé pour lire les saisies de l'utilisateur. */
    private final Scanner in;

    /** Réseau à modifier et à évaluer. */
    private final Network net;

    /** Calculateur de coût (dispersion, surcharge, coût total). */
    private final CostCalculator calc;

    /** Algorithme de résolution automatique. */
    private final AutoSolver solver;

    /** Outil de sauvegarde du réseau dans un fichier. */
    private final NetworkWriter writer;

    /**
     * Crée un menu PARTIE 2 pour un réseau donné.
     *
     * @param in     scanner pour les entrées utilisateur
     * @param net    réseau déjà construit (par exemple via NetworkParser)
     * @param lambda valeur du coefficient lambda utilisé pour le calcul du coût
     */
    public ConsoleMenuV2(Scanner in, Network net, double lambda) {
        this.in = in;
        this.net = net;
        this.calc = new CostCalculator(lambda);
        this.solver = new AutoSolver(10); // par défaut : 10 passes de recherche
        this.writer = new NetworkWriter();
    }

    /**
     * Lance le menu textuel de la PARTIE 2.
     * Propose affichage, résolution automatique et sauvegarde.
     */
    public void run() {
        while (true) {
            System.out.println();
            System.out.println("=== PARTIE 2 - Menu ===");
            System.out.println("1) Afficher le reseau et le cout actuel");
            System.out.println("2) Resolution automatique (ameliorer la repartition)");
            System.out.println("3) Sauvegarder la solution dans un fichier");
            System.out.println("4) Quitter");
            System.out.print("> ");

            String line = safeNextLine();
            if (line == null) {
                return;
            }
            String choice = line.trim();

            switch (choice) {
                case "1":
                    showNetwork();
                    break;
                case "2":
                    runAutoSolver();
                    break;
                case "3":
                    saveSolution();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Option inconnue.");
            }
        }
    }

    /**
     * Affiche le reseau et le cout associe.
     */
    private void showNetwork() {
        CostCalculator.Cost cost = calc.compute(net);
        System.out.println(NetworkPrinter.printSummary(net, cost));
    }

    /**
     * Lance l'algorithme de resolution automatique
     * et affiche le nouveau cout si une amelioration a ete trouvée.
     */
    private void runAutoSolver() {
        CostCalculator.Cost before = calc.compute(net);
        double beforeTotal = before.total();

        boolean improved = solver.improve(net, calc);

        CostCalculator.Cost after = calc.compute(net);
        double afterTotal = after.total();

        if (improved && afterTotal < beforeTotal) {
            System.out.println("Solution amelioree.");
        } else {
            System.out.println("Aucune amelioration trouvee.");
        }
        System.out.println(NetworkPrinter.printSummary(net, after));
    }

    /**
     * Demande un nom de fichier a l'utilisateur et sauvegarde
     * la solution actuelle dans ce fichier.
     */
    private void saveSolution() {
        System.out.print("Nom du fichier de sortie : ");
        String name = safeNextLine();
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Nom de fichier invalide.");
            return;
        }

        Path path = Path.of(name.trim());
        try {
            writer.save(net, path);
            System.out.println("Solution sauvegardee dans : " + path);
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Lecture securisee d'une ligne depuis le scanner.
     *
     * @return la ligne lue, ou null si aucune entree n'est disponible
     */
    private String safeNextLine() {
        try {
            if (!in.hasNextLine()) {
                return null;
            }
            return in.nextLine();
        } catch (Exception e) {
            return null;
        }
    }
}
