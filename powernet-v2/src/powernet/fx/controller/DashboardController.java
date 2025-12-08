package powernet.fx.controller;

import powernet.core.AutoSolver;
import powernet.core.CostCalculator;
import powernet.core.Network;
import powernet.core.NetworkParser;
import powernet.fx.view.NetworkCanvas;
import java.io.File;
import java.nio.file.Path;

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
