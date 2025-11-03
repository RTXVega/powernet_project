package powernet.cli;

import java.util.Scanner;

/** Point d'entrée (PARTIE 1). Arguments facultatifs: --lambda <valeur> */
public class App {
    public static void main(String[] args) {
        double lambda = 10.0; // par défaut PARTIE 1
        for (int i = 0; i < args.length; i++) {
            if ("--lambda".equals(args[i]) && i+1 < args.length) {
                try { lambda = Double.parseDouble(args[++i]); } catch (NumberFormatException ignored) {}
            }
        }
        Scanner sc = new Scanner(System.in);
        new ConsoleMenu(sc, lambda).run();
    }
}
