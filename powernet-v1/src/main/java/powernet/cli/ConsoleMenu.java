package powernet.cli;

import java.util.*;
import powernet.core.*;
import powernet.model.*;

/**
 * Interface textuelle pour la PARTIE 1.
 * Cette classe propose des menus en console pour construire le réseau
 * et calculer le coût associé.
 */
public class ConsoleMenu {

    /** Lecteur des entrées clavier de l'utilisateur. */
    private final Scanner in;

    /** Réseau électrique manipulé par l'utilisateur. */
    private final Network net;

    /** Outil de calcul du coût (dispersion, surcharge, total). */
    private final CostCalculator calc;

    /**
     * Crée un nouveau menu en console pour un réseau vide.
     *
     * @param in     scanner utilisé pour lire les saisies de l'utilisateur
     * @param lambda valeur du coefficient lambda utilisé dans le calcul du coût
     */
    public ConsoleMenu(Scanner in, double lambda) {
        this.in = in;
        this.net = new Network();
        this.calc = new CostCalculator(lambda);
    }

    /**
     * Lance l'interface textuelle :
     * d'abord la phase de construction du réseau, puis la phase de calcul et d'affichage.
     */
    public void run() {
        System.out.println("=== PARTIE 1 - Construction du reseau (etape 1/2) ===");
        firstStage();
        System.out.println("=== PARTIE 1 - Calculs et affichage (etape 2/2) ===");
        secondStage();
    }

    /**
     * Première étape : construction du réseau.
     * Permet d'ajouter des générateurs, des maisons et des connexions,
     * ainsi que de supprimer une connexion.
     */
    private void firstStage() {
        while (true) {
            System.out.println();
            System.out.println("1) Ajouter un generateur (ex: G1 60)");
            System.out.println("2) Ajouter une maison (ex: M1 BASSE|NORMAL|FORTE)");
            System.out.println("3) Ajouter une connexion (ex: M1 G1 ou G1 M1)");
            System.out.println("4) Supprimer une connexion (ex: M1 G1 ou G1 M1)");
            System.out.println("5) Terminer la saisie");
            System.out.print("> ");

            String line = safeNextLine();
            if (line == null) return;
            String t = line.trim();

            switch (t) {
                case "1":
                    addGeneratorFlow();
                    break;
                case "2":
                    addHouseFlow();
                    break;
                case "3":
                    addConnectionFlow();
                    break;
                case "4":
                    removeConnectionFlow();
                    break;
                case "5":
                    List<String> issues = NetworkValidator.validate(net);
                    if (issues.isEmpty()) {
                        return;
                    }
                    System.out.println("Problemes detectes:");
                    for (String s : issues) {
                        System.out.println(" - " + s);
                    }
                    System.out.println("Corrigez avant de continuer.");
                    break;
                default:
                    System.out.println("Option inconnue.");
            }
        }
    }

    /**
     * Deuxième étape : calculs et affichage.
     * Permet de calculer le coût du réseau, de modifier une connexion,
     * d'afficher le réseau, ou de quitter le programme.
     */
    private void secondStage() {
        while (true) {
            System.out.println();
            System.out.println("1) Calculer le cout du reseau");
            System.out.println("2) Modifier une connexion (reconnecter Mx vers Gy)");
            System.out.println("3) Afficher le reseau");
            System.out.println("4) Quitter");
            System.out.print("> ");

            String line = safeNextLine();
            if (line == null) return;
            String t = line.trim();

            switch (t) {
                case "1":
                    computeCost();
                    break;
                case "2":
                    addConnectionFlow(); // reconnecte la maison vers un autre générateur
                    break;
                case "3":
                    printNetwork();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Option inconnue.");
            }
        }
    }

    /**
     * Saisie et ajout d'un nouveau générateur dans le réseau.
     * Format attendu : Gx capacite_kW.
     */
    private void addGeneratorFlow() {
        System.out.println("Saisir: <id> <capacite_kW>  ex: G1 60");
        System.out.print("entree> ");
        String[] t = readTokens();
        if (t == null || t.length != 2) {
            System.out.println("Format attendu.");
            return;
        }
        try {
            String id = t[0].trim();
            int cap = Integer.parseInt(t[1]);

            if (!id.startsWith("G")) {
                System.out.println("L'id d'un generateur doit commencer par 'G' (ex: G1).");
                return;
            }

            net.addGenerator(new powernet.model.Generator(id, cap));
            System.out.println("Ajoute: " + id + " (capacite=" + cap + "kW)");
        } catch (NumberFormatException e) {
            System.out.println("Capacite invalide.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    /**
     * Saisie et ajout d'une nouvelle maison dans le réseau.
     * Format attendu : Mx niveau, avec niveau ∈ {BASSE, NORMAL, FORTE}.
     */
    private void addHouseFlow() {
        System.out.println("Saisir: <id> <niveau>  niveau∈{BASSE,NORMAL,FORTE}");
        System.out.print("entree> ");
        String[] t = readTokens();
        if (t == null || t.length != 2) {
            System.out.println("Format attendu.");
            return;
        }
        try {
            String id = t[0].trim();
            Consumption lvl = Consumption.valueOf(t[1].toUpperCase());

            if (!id.startsWith("M")) {
                System.out.println("L'id d'une maison doit commencer par 'M' (ex: M1).");
                return;
            }

            net.addHouse(new House(id, lvl));
            System.out.println("Ajoute: " + id + " (" + lvl + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    /**
     * Saisie et création d'une connexion entre une maison et un générateur.
     * L'utilisateur peut saisir Mx Gy ou Gy Mx.
     */
    private void addConnectionFlow() {
        System.out.println("Saisir: Mx Gy  (ou Gy Mx)");
        System.out.print("entree> ");
        String[] t = readTokens();
        if (t == null || t.length != 2) {
            System.out.println("Format attendu.");
            return;
        }
        String a = t[0];
        String b = t[1];
        String h = null;
        String g = null;

        if (a.startsWith("M") && b.startsWith("G")) {
            h = a; g = b;
        } else if (a.startsWith("G") && b.startsWith("M")) {
            h = b; g = a;
        } else {
            System.out.println("Il faut une maison M* et un generateur G*.");
            return;
        }

        try {
            net.connect(h, g);
            System.out.println("Connecte: " + h + " -> " + g);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    /**
     * Saisie et suppression d'une connexion existante entre une maison et un générateur.
     * L'utilisateur peut saisir Mx Gy ou Gy Mx.
     */
    private void removeConnectionFlow() {
        System.out.println("Saisir la connexion a supprimer: Mx Gy  (ou Gy Mx)");
        System.out.print("entree> ");
        String[] t = readTokens();
        if (t == null || t.length != 2) {
            System.out.println("Format attendu.");
            return;
        }
        String a = t[0];
        String b = t[1];
        String h = null;
        String g = null;

        if (a.startsWith("M") && b.startsWith("G")) {
            h = a; g = b;
        } else if (a.startsWith("G") && b.startsWith("M")) {
            h = b; g = a;
        } else {
            System.out.println("Il faut une maison M* et un generateur G*.");
            return;
        }

        try {
            net.removeConnection(h, g);
            System.out.println("Connexion supprimee: " + h + " -/-> " + g);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    /**
     * Calcule le coût du réseau courant et l'affiche à l'écran.
     */
    private void computeCost() {
        CostCalculator.Cost cost = calc.compute(net);
        System.out.println(NetworkPrinter.printSummary(net, cost));
    }

    /**
     * Affiche l'état actuel du réseau et les coûts associés.
     */
    private void printNetwork() {
        CostCalculator.Cost cost = calc.compute(net);
        System.out.println(NetworkPrinter.printSummary(net, cost));
    }

    // ---- util ----

    /**
     * Lit une ligne de la console et la découpe en mots (séparés par des espaces).
     *
     * @return un tableau de tokens ou null si aucune ligne n'est disponible.
     */
    private String[] readTokens() {
        String line = safeNextLine();
        if (line == null)
            return null;
        String[] raw = line.trim().split("\\s+");
        List<String> list = new ArrayList<>();
        for (String s : raw) {
            if (!s.isEmpty()) list.add(s);
        }
        return list.toArray(String[]::new);
    }

    /**
     * Lit une ligne de texte de manière sécurisée.
     *
     * @return la ligne lue, ou  null si aucune entrée n'est disponible ou en cas d'erreur.
     */
    private String safeNextLine() {
        try {
            if (!in.hasNextLine()) return null;
            return in.nextLine();
        } catch (Exception e) {
            return null;
        }
    }
}
