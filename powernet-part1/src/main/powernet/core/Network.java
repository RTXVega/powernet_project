package powernet.core;

import java.util.*;
import powernet.model.*;

/** Réseau S = <M, G, C> avec affectation maison->générateur (unique). */
public class Network {
    private Map<String, House> houses = new LinkedHashMap<>();
    private Map<String, Generator> generators = new LinkedHashMap<>();
    /** Affectation: idMaison -> idGenerateur */
    private Map<String, String> assignment = new LinkedHashMap<>();

    public void addHouse(House h) {
        String id = h.getId();
        if (houses.containsKey(id)) throw new IllegalArgumentException("Maison déjà déclarée: " + id);
        houses.put(id, h);
    }

    public void addGenerator(Generator g) {
        String id = g.getId();
        if (generators.containsKey(id)) throw new IllegalArgumentException("Générateur déjà déclaré: " + id);
        generators.put(id, g);
    }

    /** Connecte une maison à un générateur (remplace l'existant). */
    public void connect(String houseId, String generatorId) {
        requireHouse(houseId);
        requireGenerator(generatorId);
        assignment.put(houseId, generatorId);
    }

    public String connectedGenerator(String houseId) {
        return assignment.get(houseId);
    }

    public Map<String, Integer> computeLoadsKw() {
        Map<String, Integer> loads = new LinkedHashMap<>();
        for (String id : generators.keySet()) {
            loads.put(id, 0);
        }
        for (Map.Entry<String, String> e : assignment.entrySet()) {
            House m = houses.get(e.getKey());
            String gId = e.getValue();
            if (loads.containsKey(gId)) {
                loads.put(gId, loads.get(gId) + m.demandKw());
            }
        }
        return loads;
    }

    public Map<String, House> houses() { return Collections.unmodifiableMap(houses); }
    public Map<String, Generator> generators() { return Collections.unmodifiableMap(generators); }
    public Map<String, String> assignment() { return Collections.unmodifiableMap(assignment); }

    public List<String> unassignedHouses() {
        List<String> res = new ArrayList<>();
        for (String h : houses.keySet()) {
            if (!assignment.containsKey(h)) res.add(h);
        }
        return res;
    }

    private void requireHouse(String id) {
        if (!houses.containsKey(id)) throw new IllegalArgumentException("Maison inconnue: " + id);
    }
    private void requireGenerator(String id) {
        if (!generators.containsKey(id)) throw new IllegalArgumentException("Générateur inconnu: " + id);
    }
}
