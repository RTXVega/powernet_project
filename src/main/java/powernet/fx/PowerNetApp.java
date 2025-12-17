package powernet.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import powernet.fx.view.MainView;

public class PowerNetApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainView mainView = new MainView();
        Scene scene = new Scene(mainView, 1000, 700);

        primaryStage.setTitle("Réseau de distribution d’électricité - Outil de l'optimisation de réseau");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
