package powernet.core;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class NetworkLoader {

    private static class NetworkData {
        List<GeneratorData> generators;
        List<HouseData> houses;
        List<ConnectionData> connections;
    }

    private static class GeneratorData {
        String id;
        int capacity;
    }

    private static class HouseData {
        String id;
        Consumption consumption;
    }

    private static class ConnectionData {
        String houseId;
        String generatorId;
    }

    public static Network loadFromFile(String filePath) throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(filePath)) {
            NetworkData data = gson.fromJson(reader, NetworkData.class);
            return createNetworkFromData(data);
        }
    }

    private static Network createNetworkFromData(NetworkData data) {
        Network network = new Network();

        if (data.generators != null) {
            for (GeneratorData genData : data.generators) {
                network.addGenerator(new Generator(genData.id, genData.capacity));
            }
        }

        if (data.houses != null) {
            for (HouseData houseData : data.houses) {
                network.addHouse(new House(houseData.id, houseData.consumption));
            }
        }

        if (data.connections != null) {
            for (ConnectionData connData : data.connections) {
                try {
                    network.connect(connData.houseId, connData.generatorId);
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Could not create connection from file: " + e.getMessage());
                }
            }
        }

        return network;
    }
}
