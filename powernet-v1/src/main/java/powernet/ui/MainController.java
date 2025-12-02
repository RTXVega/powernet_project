package powernet.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import powernet.core.Network;
import powernet.core.NetworkLoader;
import powernet.model.Consumption;
import powernet.model.Generator;
import powernet.model.House;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Button loadButton;

    @FXML
    private TableView<Generator> generatorsTable;
    @FXML
    private TableColumn<Generator, String> generatorIdCol;
    @FXML
    private TableColumn<Generator, Integer> generatorCapacityCol;

    @FXML
    private TableView<House> housesTable;
    @FXML
    private TableColumn<House, String> houseIdCol;
    @FXML
    private TableColumn<House, Consumption> houseConsumptionCol;

    private Network currentNetwork;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup Generator table
        generatorIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        generatorCapacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        // Setup House table
        houseIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        houseConsumptionCol.setCellValueFactory(new PropertyValueFactory<>("consumption"));
    }

    @FXML
    private void loadNetwork() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open PowerNet Network File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(loadButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                currentNetwork = NetworkLoader.loadFromFile(selectedFile.getAbsolutePath());
                populateTables();
            } catch (IOException e) {
                // In a real app, you'd show an alert to the user
                System.err.println("Failed to load network file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void populateTables() {
        if (currentNetwork != null) {
            generatorsTable.setItems(FXCollections.observableArrayList(currentNetwork.generators().values()));
            housesTable.setItems(FXCollections.observableArrayList(currentNetwork.houses().values()));
        }
    }
}