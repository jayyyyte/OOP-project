package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.JSONObject;
import search.RAGSearchEngine;
import util.Router;
import java.util.*;
import javafx.geometry.Pos;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class SearchResultsPage {
    private Scene scene;
    private String searchQuery;
    private RAGSearchEngine searchEngine;

    public SearchResultsPage(String searchQuery) {
        this.searchQuery = searchQuery;
        try {
            // Create a temporary file to store the products.json content
            Path tempFile = Files.createTempFile("products", ".json");
            
            // Copy the resource content to the temporary file
            try (InputStream is = getClass().getResourceAsStream("/products.json")) {
                if (is == null) {
                    throw new Exception("Could not find products.json resource");
                }
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Initialize the search engine with the temporary file path
            this.searchEngine = new RAGSearchEngine(tempFile.toString());
            
            // Clean up the temporary file when the application exits
            tempFile.toFile().deleteOnExit();
        } catch (Exception e) {
            System.err.println("Error initializing search engine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Scene createScene() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Header with search query
        Label headerLabel = new Label("Search Results for: " + searchQuery);
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        root.getChildren().add(headerLabel);

        // Create grid for search results
        GridPane resultsGrid = new GridPane();
        resultsGrid.setHgap(20);
        resultsGrid.setVgap(20);
        resultsGrid.setPadding(new Insets(20));

        // Check if search engine is initialized
        if (searchEngine == null) {
            Label errorLabel = new Label("Error: Search engine could not be initialized. Please try again later.");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            root.getChildren().add(errorLabel);
        } else {
            // Perform search
            Map<String, Object> searchCriteria = new HashMap<>();
            searchCriteria.put("query", searchQuery);
            List<JSONObject> results = searchEngine.search(searchCriteria);

            if (results.isEmpty()) {
                Label noResultsLabel = new Label("No products found matching your search criteria.");
                noResultsLabel.setStyle("-fx-font-size: 14px;");
                root.getChildren().add(noResultsLabel);
            } else {
                // Display top 3 results
                for (int i = 0; i < Math.min(3, results.size()); i++) {
                    JSONObject product = results.get(i);
                    VBox productCard = createProductCard(
                        product.getString("name"),
                        String.format("%,.0f VND", product.getDouble("price")),
                        "", // original price
                        "0", // discount percent
                        product.getString("productUrl")
                    );
                    resultsGrid.add(productCard, i, 0);
                }
                root.getChildren().add(resultsGrid);
            }
        }

        // Back button
        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> Router.navigateTo(new HomePage().createScene()));
        backButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
        backButton.setPadding(new Insets(10, 20, 10, 20));

        root.getChildren().add(backButton);

        // Create scene
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        
        // Load styles
        String stylesPath = getClass().getResource("/styles.css").toExternalForm();
        if (stylesPath != null) {
            scene.getStylesheets().add(stylesPath);
        }

        return scene;
    }

    private VBox createProductCard(String name, String currentPrice, String originalPrice, String discountPercent, String imagePath) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        // Product image
        try {
            ImageView imageView = new ImageView(new Image(imagePath));
            imageView.setFitWidth(250);
            imageView.setFitHeight(250);
            imageView.setPreserveRatio(true);
            
            StackPane imageContainer = new StackPane(imageView);
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.setPrefHeight(250);
            card.getChildren().add(imageContainer);
        } catch (Exception e) {
            Label imageLabel = new Label("Image not available");
            imageLabel.setAlignment(Pos.CENTER);
            imageLabel.setPrefHeight(250);
            imageLabel.setStyle("-fx-background-color: #f5f5f5; -fx-alignment: center;");
            card.getChildren().add(imageLabel);
        }
        
        // Product name
        Label nameLabel = new Label(name);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        card.getChildren().add(nameLabel);
        
        // Price
        Label priceLabel = new Label(currentPrice);
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;");
        card.getChildren().add(priceLabel);
        
        // View details button
        Button detailBtn = new Button("View Details");
        detailBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
        detailBtn.setPrefWidth(200);
        detailBtn.setOnAction(e -> Router.navigateTo(new ProductPage().createScene()));
        
        card.getChildren().add(detailBtn);
        
        return card;
    }
} 