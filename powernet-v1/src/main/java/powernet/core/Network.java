package powernet.core;

import java.util.*;
import powernet.model.*;

/**
 * Représentation d'un réseau électrique :
 * - un ensemble de maisons,
 * - un ensemble de générateurs,
 * - une affectation unique maison → générateur.
 */
public class Network {

    /** Ensemble des maisons du réseau, indexées par leur identifiant. */
    private final Map<String, House> houses;

    /** Ensemble des générateurs du réseau, indexés par leur identifiant. */
    private final Map<String, Generator> generators;

    /** Affectation maison -> générateur (chaque maison a au plus un générateur). */
    private final Map<String, String> assignment;

    /**
     * Construit un réseau vide, sans maisons ni générateurs.
     */
    public Network() {
        this.houses = new LinkedHashMap<>();
        this.generators = new LinkedHashMap<>();
        this.assignment = new LinkedHashMap<>();
    }

    /**
     * Ajoute une maison au réseau.
     * @param h la maison à ajouter
     * @throws IllegalArgumentException si une maison avec le même identifiant existe déjà
     */
    public void addHouse(House h) {
        String id = h.getId();
        if (houses.containsKey(id)) {
            throw new IllegalArgumentException("Maison déjà déclarée: " + id);
        }
        houses.put(id, h);
    }

    /**
     * Ajoute un générateur au réseau.
     * @param g le générateur à ajouter
     * @throws IllegalArgumentException si un générateur avec le même identifiant existe déjà
     */
    public void addGenerator(Generator g) {
        String id = g.getId();
        if (generators.containsKey(id)) {
            throw new IllegalArgumentException("Générateur déjà déclaré: " + id);
        }
        generators.put(id, g);
    }

    /**
     * Connecte (ou reconnecte) une maison à un générateur.
     * @param houseId identifiant de la maison
     * @param generatorId identifiant du générateur
     * @throws IllegalArgumentException si la maison ou le générateur n'existe pas
     */
    public void connect(String houseId, String generatorId) {
        requireHouse(houseId);
        requireGenerator(generatorId);
        assignment.put(houseId, generatorId);
    }

    /**
     * Supprime une connexion existante entre une maison et un générateur.
     * @param houseId identifiant de la maison
     * @param generatorId identifiant du générateur
     * @throws IllegalArgumentException si la maison n'a pas de connexion
     *                                  ou si la connexion ne correspond pas à generatorId
     */
    public void removeConnection(String houseId, String generatorId) {
        requireHouse(houseId);
        requireGenerator(generatorId);

        if (!assignment.containsKey(houseId)) {
            throw new IllegalArgumentException("Maison " + houseId + " n'a aucune connexion.");
        }
        if (!assignment.get(houseId).equals(generatorId)) {
            throw new IllegalArgumentException(
                "La connexion " + houseId + " -> " + generatorId + " n'existe pas."
            );
        }
        assignment.remove(houseId);
    }

    /**
     * @return une vue non modifiable des maisons du réseau
     */
    public Map<String, House> houses() {
        return Collections.unmodifiableMap(houses);
    }

    /**
     * @return une vue non modifiable des générateurs du réseau
     */
    public Map<String, Generator> generators() {
        return Collections.unmodifiableMap(generators);
    }

    /**
     * @return une vue non modifiable des affectations maison -> générateur
     */
    public Map<String, String> assignment() {
        return Collections.unmodifiableMap(assignment);
    }

    /**
     * Calcule la charge totale (en kW) supportée par chaque générateur.
     * @return une map générateur → charge totale
     */
    public Map<String, Integer> computeLoadsKw() {
        Map<String, Integer> loads = new LinkedHashMap<>();
        for (String gId : generators.keySet()) {
            loads.put(gId, 0);
        }
        for (Map.Entry<String, String> e : assignment.entrySet()) {
            String houseId = e.getKey();
            String genId = e.getValue();
            House h = houses.get(houseId);
            if (h != null && loads.containsKey(genId)) {
                int old = loads.get(genId);
                loads.put(genId, old + h.demandKw());
            }
        }
        return loads;
    }

    /**
     * Vérifie qu'une maison existe dans le réseau.
     * @param id identifiant de la maison
     * @throws IllegalArgumentException si la maison est inconnue
     */
    private void requireHouse(String id) {
        if (!houses.containsKey(id)) {
            throw new IllegalArgumentException("Maison inconnue: " + id);
        }
    }

    /**
     * Vérifie qu'un générateur existe dans le réseau.
     * @param id identifiant du générateur
     * @throws IllegalArgumentException si le générateur est inconnu
     */
    private void requireGenerator(String id) {
        if (!generators.containsKey(id)) {
            throw new IllegalArgumentException("Générateur inconnu: " + id);
        }
    }
}
