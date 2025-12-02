package powernet.core;

import powernet.model.Generator;
import powernet.model.House;
import powernet.model.Consumption;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sauvegarde d'un réseau dans un fichier texte au format Partie 2 :
 * generateur(id,capacite).
 * maison(id,NIVEAU).
 * connexion(gen,maison).
 */
public class NetworkWriter {

    /**
     * Sauvegarde un réseau dans un fichier, en respectant le format Partie 2.
     *
     * @param net  réseau à écrire
     * @param path chemin du fichier de sortie
     * @throws IOException en cas de problème d'écriture
     */
    public void save(Network net, Path path) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {

            // --- GENERATEURS ---
            for (Generator g : net.generators().values()) {
                bw.write(String.format(
                        "generateur(%s,%d).",
                        g.getId(),
                        g.getCapacityKw()
                ));
                bw.newLine();
            }
            bw.newLine();

            // --- MAISONS ---
            for (House h : net.houses().values()) {
                bw.write(String.format(
                        "maison(%s,%s).",
                        h.getId(),
                        h.getLevel().name()
                ));
                bw.newLine();
            }
            bw.newLine();

            // --- CONNEXIONS ---
            for (var entry : net.assignment().entrySet()) {
                String houseId = entry.getKey();
                String genId = entry.getValue();

                bw.write(String.format(
                        "connexion(%s,%s).",
                        genId,
                        houseId
                ));
                bw.newLine();
            }
        }
    }
}
