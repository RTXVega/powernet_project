package powernet.fx.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import powernet.fx.controller.DashboardController;

public class MainView extends BorderPane {

    private final NetworkCanvas networkCanvas;
    private final DashboardController controller;
    private final ControlPanel controlPanel;

    public MainView() {
        this.networkCanvas = new NetworkCanvas();
        this.controller = new DashboardController(networkCanvas);
        this.controlPanel = new ControlPanel(controller);

        setCenter(networkCanvas);
        setRight(controlPanel);

        // En-tête
        Label title = new Label("Réseau de distribution d’électricité");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setPadding(new Insets(10, 0, 10, 0));

        Label authors = new Label("Almas Kassymbekov · Anis Bouhail · Rui Ma");
        authors.setFont(Font.font("System", FontWeight.NORMAL, 12));
        authors.setStyle("-fx-text-fill: #555;");

        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(16, title, authors);
        header.setPadding(new Insets(10));
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        setTop(header);

        // Légende (Bas)
        setBottom(createLegendPanel());
    }

    private javafx.scene.layout.HBox createLegendPanel() {
        javafx.scene.layout.HBox legend = new javafx.scene.layout.HBox(20);
        legend.setPadding(new Insets(10));
        legend.setAlignment(javafx.geometry.Pos.CENTER);
        legend.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1 0 0 0;");

        legend.getChildren().addAll(
                createLegendItem(javafx.scene.paint.Color.LIGHTGREEN, "BASSE (10Kw)"),
                createLegendItem(javafx.scene.paint.Color.LIGHTBLUE, "NORMALE (20Kw)"),
                createLegendItem(javafx.scene.paint.Color.ORANGE, "FORTE (40Kw)"));
        return legend;
    }

    private javafx.scene.layout.HBox createLegendItem(javafx.scene.paint.Color color, String text) {
        javafx.scene.layout.HBox item = new javafx.scene.layout.HBox(5);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(8, color);
        circle.setStroke(javafx.scene.paint.Color.DARKSLATEGRAY);

        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        item.getChildren().addAll(circle, lbl);
        return item;
    }
}
