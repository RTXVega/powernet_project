package powernet.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class GraphicalApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL fxmlUrl = getClass().getResource("/powernet/ui/MainView.fxml");
        loader.setLocation(fxmlUrl);
        Parent root = loader.load();

        primaryStage.setTitle("PowerNet Manager");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}
