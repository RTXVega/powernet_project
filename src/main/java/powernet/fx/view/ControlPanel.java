package powernet.fx.view;

import java.io.File;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import powernet.core.CostCalculator;
import powernet.fx.controller.DashboardController;



// Panneau latéral : centralise les commandes UI (ouverture, sauvegarde, optimisation, lambda).
// Affiche en direct les métriques de coût/dispersion/surcharge et colore les alertes visuelles.
public class ControlPanel extends VBox {

    private final DashboardController controller;
    private final Label lblTotalCost;
    private final Label lblDispersion;
    private final Label lblSurcharge;
    private final TextField txtLambda;

    public ControlPanel(DashboardController controller) {
        this.controller = controller;

        setPadding(new Insets(20));
        setSpacing(15);
        setStyle("-fx-background-color: #ecf0f1; -fx-pref-width: 250;");

        // Titre
        Label title = new Label("Contrôles");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        getChildren().add(title);

        getChildren().add(new Separator());

        // Bouton Charger
        Button btnLoad = new Button("Charger un fichier réseau");
        btnLoad.setMaxWidth(Double.MAX_VALUE);
        btnLoad.setOnAction(e -> loadFile());
        getChildren().add(btnLoad);

        // Bouton Sauvegarder
        Button btnSave = new Button("Sauvegarder le réseau");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> saveFile());
        getChildren().add(btnSave);

        getChildren().add(new Separator());

        // Optimisation
        Label lblOpt = new Label("Optimisation");
        lblOpt.setStyle("-fx-font-weight: bold");

        Button btnOptimize = new Button("Lancer la résolution automatique");
        btnOptimize.setMaxWidth(Double.MAX_VALUE);
        btnOptimize.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnOptimize.setOnAction(e -> {
            controller.optimizeNetwork();
            updateStats();
        });

        getChildren().addAll(lblOpt, btnOptimize);

        getChildren().add(new Separator());

        // Paramètres
        Label lblParams = new Label("Paramètres");
        lblParams.setStyle("-fx-font-weight: bold");

        Label lblLambda = new Label("Lambda:");
        txtLambda = new TextField("10.0");
        txtLambda.textProperty().addListener((obs, oldV, newV) -> updateLambda());

        getChildren().addAll(lblParams, lblLambda, txtLambda);

        getChildren().add(new Separator());

        // Statistiques
        Label lblStats = new Label("Statistiques");
        lblStats.setStyle("-fx-font-weight: bold");

        lblTotalCost = new Label("Coût: -");
        lblDispersion = new Label("Dispersion: -");
        lblSurcharge = new Label("Surcharge: -");

        getChildren().addAll(lblStats, lblTotalCost, lblDispersion, lblSurcharge);
    }

    private void loadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger un fichier réseau");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Texte", "*.txt"));
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            controller.loadNetwork(file);
            updateStats();
        }
    }

    private void saveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le réseau");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Texte", "*.txt"));
        fileChooser.setInitialFileName("network_optimized.txt");
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            controller.saveNetwork(file);
        }
    }

    private void updateLambda() {
        try {
            double l = Double.parseDouble(txtLambda.getText());
            controller.setLambda(l);
            updateStats();
        } catch (NumberFormatException ignored) {
        }
    }

    private void updateStats() {
        if (controller.getNetwork() == null)
            return;
        CostCalculator.Cost cost = controller.getCurrentCost();

        lblTotalCost.setText(String.format("Coût: %.2f", cost.total()));
        lblDispersion.setText(String.format("Dispersion: %.2f", cost.dispersion()));
        lblSurcharge.setText(String.format("Surcharge: %.2f", cost.surcharge()));

        if (cost.surcharge() > 0) {
            lblSurcharge.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            lblSurcharge.setStyle("-fx-text-fill: black;");
        }
    }
}
