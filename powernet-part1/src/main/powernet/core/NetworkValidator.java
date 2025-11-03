package powernet.core;

import java.util.*;

/** Vérifications minimales pour la PARTIE 1. */
public class NetworkValidator {
    public static List<String> validate(Network net) {
        List<String> issues = new ArrayList<>();
        if (net.houses().isEmpty()) issues.add("Aucune maison");
        if (net.generators().isEmpty()) issues.add("Aucun générateur");
        for (String hId : net.houses().keySet()) {
            if (!net.assignment().containsKey(hId)) issues.add("Maison non connectée: " + hId);
        }
        for (Map.Entry<String,String> e : net.assignment().entrySet()) {
            if (!net.houses().containsKey(e.getKey())) issues.add("Affectation vers maison inconnue: " + e.getKey());
            if (!net.generators().containsKey(e.getValue())) issues.add("Affectation vers générateur inconnu: " + e.getValue());
        }
        return issues;
    }
}
