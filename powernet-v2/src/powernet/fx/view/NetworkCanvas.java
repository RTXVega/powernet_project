package powernet.fx.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.control.Tooltip;
import powernet.core.Network;
import powernet.model.Generator;
import powernet.model.House;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NetworkCanvas extends Pane {

    private static final double NODE_RADIUS = 20;
    private static final double GEN_SIZE = 50;

    public NetworkCanvas() {
        setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 1;");
    }

    public void draw(Network network) {
        getChildren().clear();
        if (network == null)
            return;

        Map<String, Generator> generators = network.generators();
        Map<String, House> houses = network.houses();
        Map<String, String> assignment = network.assignment();

        Map<String, Point> genPositions = new HashMap<>();
        Map<String, Point> housePositions = new HashMap<>();

        double width = getWidth();
        double height = getHeight();
        if (width == 0)
            width = 800; // Default if not yet rendered
        if (height == 0)
            height = 600;

        // Layout Generators (Top Row)
        int gCount = generators.size();
        int i = 0;
        for (Generator g : generators.values()) {
            double x = (width / (gCount + 1)) * (i + 1);
            double y = height * 0.2; // Top 20%
            genPositions.put(g.getId(), new Point(x, y));
            i++;
        }

        // Layout Houses (Bottom Area - Grid or Random)
        int hCount = houses.size();
        int cols = (int) Math.ceil(Math.sqrt(hCount * 2)); // heuristic for grid
        int j = 0;
        for (House h : houses.values()) {
            // Simple grid layout for houses
            double row = (j / cols);
            double col = (j % cols);

            // Center the grid at the bottom
            double gridWidth = width * 0.8;
            double startX = width * 0.1;
            double startY = height * 0.5;

            double x = startX + (col * (gridWidth / cols));
            double y = startY + (row * 60) + (Math.random() * 20); // Add jitter

            housePositions.put(h.getId(), new Point(x, y));
            j++;
        }

        // Draw Connections first (so they are behind nodes)
        for (Map.Entry<String, String> entry : assignment.entrySet()) {
            String houseId = entry.getKey();
            String genId = entry.getValue();

            Point p1 = housePositions.get(houseId);
            Point p2 = genPositions.get(genId);

            if (p1 != null && p2 != null) {
                Line line = new Line(p1.x, p1.y, p2.x, p2.y);
                line.setStroke(Color.GRAY);
                line.setStrokeWidth(1.5);
                getChildren().add(line);
            }
        }

        // Compute loads
        Map<String, Integer> loads = network.computeLoadsKw();

        // Draw Generators
        for (Generator g : generators.values()) {
            Point p = genPositions.get(g.getId());
            Rectangle rect = new Rectangle(p.x - GEN_SIZE / 2, p.y - GEN_SIZE / 2, GEN_SIZE, GEN_SIZE);
            rect.setFill(Color.web("#e74c3c"));
            rect.setStroke(Color.DARKRED);
            rect.setStrokeWidth(2);

            // Label (ID + Cap)
            Label label = new Label(g.getId() + "\n" + g.getCapacityKw() + "kW");
            label.setLayoutX(p.x - GEN_SIZE / 2);
            label.setLayoutY(p.y - GEN_SIZE / 2);
            label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
            label.setMinWidth(GEN_SIZE);
            label.setAlignment(Pos.CENTER);
            label.setMouseTransparent(true);

            Tooltip.install(rect, new Tooltip("Generator " + g.getId() + "\nCap: " + g.getCapacityKw() + "kW"));
            getChildren().addAll(rect, label);

            // --- Load Bar ---
            int currentLoad = loads.getOrDefault(g.getId(), 0);
            int capacity = g.getCapacityKw();
            double ratio = (double) currentLoad / capacity;

            // Bar Calculation
            double barWidth = GEN_SIZE + 20; // Slightly wider than the box
            double barHeight = 6;
            double barX = p.x - barWidth / 2;
            double barY = p.y + (GEN_SIZE / 2) + 5; // Below the box

            // Background (Gray)
            Rectangle bgBar = new Rectangle(barX, barY, barWidth, barHeight);
            bgBar.setFill(Color.LIGHTGRAY);
            bgBar.setStroke(Color.GRAY);
            bgBar.setStrokeWidth(0.5);

            // Foreground (Load)
            double fillWidth = Math.min(ratio, 1.0) * barWidth; // Cap visual at 100%
            Rectangle fillBar = new Rectangle(barX, barY, fillWidth, barHeight);

            if (currentLoad > capacity) {
                fillBar.setFill(Color.RED); // Overload
            } else {
                fillBar.setFill(Color.LIMEGREEN); // OK
            }

            // Text Label (e.g. "80/100")
            Label loadLbl = new Label(currentLoad + "/" + capacity);
            loadLbl.setLayoutX(barX);
            loadLbl.setLayoutY(barY + barHeight);
            loadLbl.setMinWidth(barWidth);
            loadLbl.setAlignment(Pos.CENTER);
            loadLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #333;");

            getChildren().addAll(bgBar, fillBar, loadLbl);
        }

        // Draw Houses
        for (House h : houses.values()) {
            Point p = housePositions.get(h.getId());
            Circle circle = new Circle(p.x, p.y, NODE_RADIUS);

            // Color based on consumption
            switch (h.getLevel()) {
                case BASSE:
                    circle.setFill(Color.LIGHTGREEN);
                    break;
                case NORMAL:
                    circle.setFill(Color.LIGHTBLUE);
                    break;
                case FORTE:
                    circle.setFill(Color.ORANGE);
                    break;
            }
            circle.setStroke(Color.DARKSLATEGRAY);

            Label label = new Label(h.getId());
            label.setLayoutX(p.x - 10);
            label.setLayoutY(p.y - 10);
            label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
            label.setMouseTransparent(true);

            Tooltip.install(circle, new Tooltip("House " + h.getId() + "\nLevel: " + h.getLevel()));

            getChildren().addAll(circle, label);
        }
    }

    // Helper class for coordinates
    private static class Point {
        double x, y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
