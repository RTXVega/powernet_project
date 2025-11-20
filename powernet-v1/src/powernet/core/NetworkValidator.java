package powernet.core;

import java.util.*;

/**
 * Classe utilitaire effectuant les vérifications minimales
 * nécessaires avant de passer à l'étape de calcul (PARTIE 1).
 */
public class NetworkValidator {

    /**
     * Analyse un réseau et signale les éventuels problèmes :
     * - absence de maisons,
     * - absence de générateurs,
     * - maisons non connectées.
     *
     * @param net le réseau à valider
     * @return une liste de messages décrivant les problèmes détectés
     */
    public static List<String> validate(Network net) {
        List<String> issues = new ArrayList<>();

        if (net.houses().isEmpty()) {
            issues.add("Aucune maison");
        }
        if (net.generators().isEmpty()) {
            issues.add("Aucun générateur");
        }

        for (String hId : net.houses().keySet()) {
            if (!net.assignment().containsKey(hId)) {
                issues.add("Maison non connectée: " + hId);
            }
        }

        return issues;
    }
}
