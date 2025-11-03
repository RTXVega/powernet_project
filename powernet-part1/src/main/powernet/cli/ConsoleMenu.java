package powernet.cli;

import java.util.*;
import powernet.core.*;
import powernet.model.*;

/** Interface textuelle PARTIE 1 (saisie manuelle, pas de lecture/écriture de fichier). */
final class ConsoleMenu {
    private final Scanner in;
    private final Network net = new Network();
    private final CostCalculator calc;

    ConsoleMenu(Scanner in, double lambda) {
        this.in = in;
        this.calc = new CostCalculator(lambda);
    }

    public void run() {
        System.out.println("=== PARTIE 1 — Construction du réseau (étape 1/2) ===");
        firstStage();
        System.out.println("=== PARTIE 1 — Calculs et affichage (étape 2/2) ===");
        secondStage();
    }

    private void firstStage() {
        while (true) {
            System.out.println();
            System.out.println("1) Ajouter un générateur (ex: G1 60)");
            System.out.println("2) Ajouter une maison (ex: M1 BASSE|NORMAL|FORTE)");
            System.out.println("3) Connecter maison et générateur (ex: M1 G1 ou G1 M1)");
            System.out.println("4) Terminer la saisie");
            System.out.print("> ");
            String line = safeNextLine();
            if (line == null) return; // EOF
            String t = line.trim();
            if ("1".equals(t)) addGeneratorFlow();
            else if ("2".equals(t)) addHouseFlow();
            else if ("3".equals(t)) connectFlow();
            else if ("4".equals(t)) {
                List<String> issues = NetworkValidator.validate(net);
                if (!issues.isEmpty()) {
                    System.out.println("Problèmes détectés:");
                    for (String s : issues) System.out.println(" - " + s);
                    System.out.println("Veuillez corriger avant de continuer.");
                } else {
                    return; // ok, passer à l'étape 2
                }
            } else {
                System.out.println("Option inconnue.");
            }
        }
    }

    private void secondStage() {
        while (true) {
            System.out.println();
            System.out.println("1) Calculer le coût");
            System.out.println("2) Modifier une connexion (ex: M1 G2)");
            System.out.println("3) Afficher le réseau");
            System.out.println("4) Quitter");
            System.out.print("> ");
            String line = safeNextLine();
            if (line == null) return;
            String t = line.trim();
            if ("1".equals(t)) computeCost();
            else if ("2".equals(t)) connectFlow();
            else if ("3".equals(t)) printNetwork();
            else if ("4".equals(t)) return;
            else System.out.println("Option inconnue.");
        }
    }

    private void addGeneratorFlow() {
        System.out.println("Saisir: <id> <capacité_kW>  ex: G1 60");
        System.out.print("entrée> ");
        String[] t = readTokens();
        if (t == null || t.length != 2) { System.out.println("Format attendu."); return; }
        try {
            String id = t[0];
            int cap = Integer.parseInt(t[1]);
            net.addGenerator(new Generator(id, cap));
            System.out.println("Ajouté: " + id + " (capacité=" + cap + "kW)");
        } catch (NumberFormatException e) {
            System.out.println("Capacité invalide.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void addHouseFlow() {
        System.out.println("Saisir: <id> <niveau>  niveau∈{BASSE,NORMAL,FORTE}");
        System.out.print("entrée> ");
        String[] t = readTokens();
        if (t == null || t.length != 2) { System.out.println("Format attendu."); return; }
        try {
            String id = t[0];
            Consumption lvl = Consumption.valueOf(t[1].toUpperCase());
            net.addHouse(new House(id, lvl));
            System.out.println("Ajouté: " + id + " (" + lvl + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    private void connectFlow() {
        System.out.println("Saisir: Mx Gy  (ou Gy Mx)");
        System.out.print("entrée> ");
        String[] t = readTokens();
        if (t == null || t.length != 2) { System.out.println("Format attendu."); return; }
        String a = t[0], b = t[1];
        String h = a.startsWith("M") ? a : (b.startsWith("M") ? b : null);
        String g = a.startsWith("G") ? a : (b.startsWith("G") ? b : null);
        if (h == null || g == null) { System.out.println("Il faut une maison M* et un générateur G*."); return; }
        try {
            net.connect(h, g);
            System.out.println("Connecté: " + h + " -> " + g);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    private void computeCost() {
        CostCalculator.Cost cost = calc.compute(net);
        System.out.println(NetworkPrinter.printSummary(net, cost));
    }

    private void printNetwork() {
        CostCalculator.Cost cost = calc.compute(net);
        System.out.println(NetworkPrinter.printSummary(net, cost));
    }

    private String[] readTokens() {
        String line = safeNextLine();
        if (line == null) return null;
        String[] raw = line.trim().split("\\s+");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < raw.length; i++) {
            if (raw[i].length() > 0) list.add(raw[i]);
        }
        return list.toArray(new String[list.size()]);
    }

    private String safeNextLine() {
        try {
            if (!in.hasNextLine()) return null;
            return in.nextLine();
        } catch (Exception e) {
            return null;
        }
    }
}
