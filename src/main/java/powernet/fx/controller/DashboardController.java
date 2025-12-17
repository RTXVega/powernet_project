package powernet.fx.controller;

import java.io.File;

import powernet.core.AutoSolver;
import powernet.core.CostCalculator;
import powernet.core.Network;
import powernet.core.NetworkParser;
import powernet.core.NetworkWriter;
import powernet.fx.view.NetworkCanvas;

public class DashboardController {

    private Network currentNetwork;
    private final NetworkCanvas canvas;
    private double lambda = 10.0;

    public DashboardController(NetworkCanvas canvas) {
        this.canvas = canvas;
        this.currentNetwork = new Network(); // Start empty
    }

    public void loadNetwork(File file) {
        try {
            NetworkParser parser = new NetworkParser();
            this.currentNetwork = parser.parse(file.toPath());
            updateCanvas();
        } catch (Exception e) {
            e.printStackTrace(); // TODO: Show Alert
        }
    }

    public void saveNetwork(File file) {
        if (currentNetwork == null)
            return;
        try {
            NetworkWriter writer = new NetworkWriter();
            writer.save(currentNetwork, file.toPath());
            showAlert("Succès", "Réseau sauvegardé avec succès !", javafx.scene.control.Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la sauvegarde : " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    public void optimizeNetwork() {
        if (currentNetwork == null)
            return;

        // On met beaucoup d'itérations car c'est très rapide
        AutoSolver solver = new AutoSolver(50000);
        CostCalculator calculator = new CostCalculator(lambda);

        boolean improved = solver.improve(currentNetwork, calculator);

        if (improved) {
            updateCanvas();
            String msg = String.format("Optimisation réussie !\nNouveau coût : %.2f",
                    calculator.compute(currentNetwork).total());
            showAlert("Succès", msg, javafx.scene.control.Alert.AlertType.INFORMATION);
        } else {
            showAlert("Information", "Impossible d'optimiser davantage (Optimum local atteint).",
                    javafx.scene.control.Alert.AlertType.INFORMATION);
        }
    }

    private void showAlert(String title, String content, javafx.scene.control.Alert.AlertType type) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle("Résultat Optimisation");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public Network getNetwork() {
        return currentNetwork;
    }

    public CostCalculator.Cost getCurrentCost() {
        return new CostCalculator(lambda).compute(currentNetwork);
    }

    private void updateCanvas() {
        canvas.draw(currentNetwork);
    }
}
