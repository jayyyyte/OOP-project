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
import search.SearchManager;
import search.PineconeConfig;
import util.Router;
import java.util.*;
import javafx.geometry.Pos;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import view.HomePage.Product;
import util.ImageCache;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;

public class SearchResultsPage {
    private Scene scene;
    private String searchQuery;
    private RAGSearchEngine ragSearchEngine;
    private SearchManager basicSearchManager;
    private String searchType;

    public SearchResultsPage(String searchQuery, String jsonFile, String searchType) {
        this.searchQuery = searchQuery;
        this.searchType = searchType;

        try {
            if (searchType.equals("RAG Search")) {
                String namespace;
                if (jsonFile.equals("smartphones.json")) {
                    namespace = PineconeConfig.NAMESPACE_SMARTPHONES;
                } else if (jsonFile.equals("laptops.json")) {
                    namespace = PineconeConfig.NAMESPACE_LAPTOPS;
                } else {
                    throw new Exception("Unsupported product category: " + jsonFile);
                }
                
                // Initialize the RAG search engine with resource path
                this.ragSearchEngine = new RAGSearchEngine("/" + jsonFile, namespace);
            } else {
                // Initialize the basic search manager
                this.basicSearchManager = new SearchManager();
            }
        } catch (Exception e) {
            System.err.println("Error initializing search engine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Scene createScene() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Header with search query and search type
        Label headerLabel = new Label(String.format("Search Results for: %s (%s)", searchQuery, searchType));
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        root.getChildren().add(headerLabel);

        // Create grid for search results
        GridPane resultsGrid = new GridPane();
        resultsGrid.setHgap(20);
        resultsGrid.setVgap(20);
        resultsGrid.setPadding(new Insets(20));

        // Check if search engine is initialized
        if ((searchType.equals("RAG Search") && ragSearchEngine == null) || 
            (searchType.equals("Basic Search") && basicSearchManager == null)) {
            Label errorLabel = new Label("Error: Search engine could not be initialized. Please try again later.");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            root.getChildren().add(errorLabel);
        } else {
            // Perform search based on search type
            List<JSONObject> results;
            if (searchType.equals("RAG Search")) {
                Map<String, Object> searchCriteria = new HashMap<>();
                searchCriteria.put("query", searchQuery);
                results = ragSearchEngine.search(searchCriteria);
            } else {
                results = basicSearchManager.search(searchQuery);
            }

            if (results.isEmpty()) {
                Label noResultsLabel = new Label("No products found matching your search criteria.");
                noResultsLabel.setStyle("-fx-font-size: 14px;");
                root.getChildren().add(noResultsLabel);
            } else {
                // Create a loading indicator
                ProgressIndicator loadingIndicator = new ProgressIndicator();
                loadingIndicator.setMaxSize(50, 50);
                root.getChildren().add(loadingIndicator);

                // Create a list to store all image loading futures
                List<CompletableFuture<Void>> imageLoadingFutures = new ArrayList<>();
                List<VBox> productCards = new ArrayList<>();

                // Display all results, 4 per row
                for (int i = 0; i < results.size(); i++) {
                    JSONObject product = results.get(i);
                    VBox productCard = createProductCard(product, imageLoadingFutures);
                    productCards.add(productCard);
                }

                // Wait for all images to load
                CompletableFuture.allOf(imageLoadingFutures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        Platform.runLater(() -> {
                            // Remove loading indicator
                            root.getChildren().remove(loadingIndicator);
                            
                            // Add all product cards to the grid
                            for (int i = 0; i < productCards.size(); i++) {
                                int col = i % 4; // Column index (0 to 3)
                                int row = i / 4; // Row index
                                resultsGrid.add(productCards.get(i), col, row);
                            }
                            // Wrap resultsGrid in a ScrollPane
                            ScrollPane scrollPane = new ScrollPane(resultsGrid);
                            scrollPane.setFitToWidth(true); // Allow horizontal scrolling if needed, but fit width generally
                            // Add the ScrollPane to the root
                            root.getChildren().add(scrollPane);
                        });
                    });
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

    private VBox createProductCard(JSONObject productJson, List<CompletableFuture<Void>> imageLoadingFutures) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        // Create a Product object from JSONObject
        Product product = new Product();
        
        // Safely extract string values with containsKey check
        product.name = productJson.has("name") ? productJson.getString("name") : "";
        product.productUrl = productJson.has("productUrl") ? productJson.getString("productUrl") : "";
        product.imageUrl = productJson.has("imageUrl") ? productJson.getString("imageUrl") : "";
        
        // Use optDouble/optInt for numeric values as they support default values
        product.price = productJson.optDouble("price", 0.0);
        product.priceCurrency = productJson.has("priceCurrency") ? productJson.getString("priceCurrency") : "";
        product.overallRating = productJson.optDouble("overallRating", 0.0);
        product.reviewCount = productJson.optInt("reviewCount", 0);

        // Add description
        product.description = productJson.has("description") ? productJson.getString("description") : "";

        // Add specifications
        product.specifications = new HashMap<>();
        if (productJson.has("Spec_1")) {
            product.specifications.put("Spec_1", productJson.getString("Spec_1"));
        }
        if (productJson.has("Spec_2")) {
            product.specifications.put("Spec_2", productJson.getString("Spec_2"));
        }
        if (productJson.has("Spec_3")) {
            product.specifications.put("Spec_3", productJson.getString("Spec_3"));
        }
        if (productJson.has("Spec_4")) {
            product.specifications.put("Spec_4", productJson.getString("Spec_4"));
        }

        // Add reviews
        if (productJson.has("reviews")) {
            product.reviews = new ArrayList<>();
            JSONArray reviewsArray = productJson.getJSONArray("reviews");
            for (int i = 0; i < reviewsArray.length(); i++) {
                JSONObject reviewJson = reviewsArray.getJSONObject(i);
                Product.Review review = new Product.Review(
                    reviewJson.has("author") ? reviewJson.getString("author") : "",
                    reviewJson.has("content") ? reviewJson.getString("content") : ""
                );
                product.reviews.add(review);
            }
        }

        // Product image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(250);
        imageContainer.setAlignment(Pos.CENTER);
        
        // Create a placeholder while image loads
        Label loadingLabel = new Label("Loading...");
        loadingLabel.setStyle("-fx-background-color: #f5f5f5; -fx-alignment: center;");
        imageContainer.getChildren().add(loadingLabel);
        
        card.getChildren().add(imageContainer);
        
        // Load image asynchronously using ImageCache
        CompletableFuture<Void> imageLoadingFuture = ImageCache.getImage(product.imageUrl)
            .thenAccept(image -> {
                if (image != null) {
                    Platform.runLater(() -> {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(250);
                        imageView.setFitHeight(250);
                        imageView.setPreserveRatio(true);
                        
                        imageContainer.getChildren().clear();
                        imageContainer.getChildren().add(imageView);
                    });
                } else {
                    Platform.runLater(() -> {
                        imageContainer.getChildren().clear();
                        Label errorLabel = new Label("Image not available");
                        errorLabel.setStyle("-fx-background-color: #f5f5f5; -fx-alignment: center;");
                        imageContainer.getChildren().add(errorLabel);
                    });
                }
            })
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    imageContainer.getChildren().clear();
                    Label errorLabel = new Label("Error loading image");
                    errorLabel.setStyle("-fx-background-color: #f5f5f5; -fx-alignment: center;");
                    imageContainer.getChildren().add(errorLabel);
                });
                return null;
            });
        
        imageLoadingFutures.add(imageLoadingFuture);
        
        // Product name
        Label nameLabel = new Label(product.name);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        card.getChildren().add(nameLabel);
        
        // Price
        Label priceLabel = new Label(String.format("%,.0f VND", product.price*1000));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;");
        card.getChildren().add(priceLabel);
        
        // View details button
        Button detailBtn = new Button("View Details");
        detailBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
        detailBtn.setPrefWidth(200);
        detailBtn.setOnAction(e -> Router.navigateTo(new ProductPage(product).createScene()));
        
        card.getChildren().add(detailBtn);
        
        return card;
    }
} 