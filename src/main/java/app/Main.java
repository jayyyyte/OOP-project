import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import util.Router;
import view.HomePage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize router with the primary stage
            Router.init(primaryStage);

            // Load HomePage scene
            Scene homeScene = new HomePage().createScene();

            // Set stage properties
            primaryStage.setTitle("OOP Project");
            primaryStage.setScene(homeScene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}