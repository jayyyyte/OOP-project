package util;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Router {
    private static Stage primaryStage;

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void navigateTo(Scene scene) {
        primaryStage.setScene(scene);
    }

    public static Stage getStage() {
        return primaryStage;
    }
}