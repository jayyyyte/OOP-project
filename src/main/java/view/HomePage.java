package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import view.*;
import util.Router;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import util.ImageCache;

public class HomePage {
    private Scene scene;
    private boolean isDarkTheme = false;
    private int currentBannerIndex = 0;
    private Timeline bannerTimeline;

    // Store references to banner components
    private HBox bannerContainer;
    private Label[] iconLabels;
    private Label[] textLabels;

    // Banner data structure
    private class BannerItem {
        String icon;
        String text;

        BannerItem(String icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }

    // Define a Product class to match the JSON structure
    public static class Product {
        String name;
        String productUrl;
        String imageUrl;
        double price;
        String priceCurrency;
        double overallRating;
        int reviewCount;
        String description;
        List<Review> reviews;
        Map<String, String> specifications;

        // Define a Review class
        public static class Review {
            String author;
            String content;

            // Constructor for Review
            public Review(String author, String content) {
                this.author = author;
                this.content = content;
            }
        }
    }

    public Scene createScene() {
        VBox root = new VBox(0);

        // Navigation bar
        HBox navbar = createNavigationBar();

        // Main content
        BorderPane mainContent = createMainContent();

        // Add all components to root
        root.getChildren().addAll(navbar, mainContent);

        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

        // Load default theme
        String stylesPath = getClass().getResource("/styles.css").toExternalForm();
        if (stylesPath != null) {
            scene.getStylesheets().add(stylesPath);
        } else {
            System.err.println("Warning: Could not load styles.css");
        }

        // F11 to toggle fullscreen
        scene.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("F11")) {
                Router.getStage().setFullScreen(!Router.getStage().isFullScreen());
            }
        });

        return scene;
    }

    private void toggleTheme() {
        try {
            scene.getStylesheets().clear();
            String stylesPath = getClass().getResource("/styles.css").toExternalForm();

            if (isDarkTheme) {
                // Switch to light theme
                if (stylesPath != null) {
                    scene.getStylesheets().add(stylesPath);
                }
            } else {
                // Switch to dark theme
                if (stylesPath != null) {
                    scene.getStylesheets().add(stylesPath);
                }
                String darkThemePath = getClass().getResource("/dark-theme.css").toExternalForm();
                if (darkThemePath != null) {
                    scene.getStylesheets().add(darkThemePath);
                }
            }
            isDarkTheme = !isDarkTheme;
        } catch (Exception e) {
            System.err.println("Error toggling theme: " + e.getMessage());
        }
    }

    private HBox createNavigationBar() {
        HBox navbar = new HBox(20);
        navbar.getStyleClass().add("nav-bar");
        navbar.setAlignment(Pos.CENTER);

        // Add small spacer after logo
        Region logoSpacer = new Region();
        logoSpacer.setPrefWidth(40);

        // Search bar
        HBox searchContainer = new HBox(5);
        searchContainer.setAlignment(Pos.CENTER);

        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("Smartphones", "Laptops");
        categoryComboBox.setValue("Smartphones"); // Default value
        categoryComboBox.getStyleClass().add("search-category");

        ComboBox<String> searchTypeComboBox = new ComboBox<>();
        searchTypeComboBox.getItems().addAll("RAG Search", "Basic Search");
        searchTypeComboBox.setValue("RAG Search"); // Default value
        searchTypeComboBox.getStyleClass().add("search-category");

        TextField searchField = new TextField();
        searchField.setPromptText("Bạn cần tìm gì?");
        searchField.setPrefWidth(400);
        searchField.getStyleClass().add("search-field");

        // Add search functionality
        searchField.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                String selectedCategory = categoryComboBox.getValue();
                String searchType = searchTypeComboBox.getValue();
                String jsonFile = selectedCategory.equals("Smartphones") ? "smartphones.json" : "laptops.json";
                Router.navigateTo(new SearchResultsPage(query, jsonFile, searchType).createScene());
            }
        });

        searchContainer.getChildren().addAll(categoryComboBox, searchTypeComboBox, searchField);

        // Navigation buttons
        Button cartBtn = new Button("Giỏ hàng");
        Button themeBtn = new Button("Toggle Theme");

        cartBtn.getStyleClass().add("nav-button");
        themeBtn.getStyleClass().add("nav-button");

        cartBtn.setOnAction(e -> {
            Router.navigateTo(new CartPage().createScene());
        });
        themeBtn.setOnAction(e -> toggleTheme());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        navbar.getChildren().addAll(
                logoSpacer, // Add the spacer here
                searchContainer,
                spacer,
                cartBtn,
                themeBtn
        );
        return navbar;
    }

    private BorderPane createMainContent() {
        BorderPane content = new BorderPane();

        // Left sidebar - Categories
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");

        // Category data with names and image paths
        String[][] categories = {
                {"Smartphones", "/images/phone_icon.png"},
                {"Laptops", "/images/laptop_icon.png"}
        };

        for (String[] category : categories) {
            try {
                // Create image view
                Image img = new Image(getClass().getResourceAsStream(category[1]));
                if (img.isError()) {
                    throw new Exception("Failed to load image: " + category[1]);
                }
                ImageView icon = new ImageView(img);
                icon.setFitHeight(24);
                icon.setFitWidth(24);

                // Create button with text and icon
                Button btn = new Button(category[0]);
                btn.setGraphic(icon);
                btn.setGraphicTextGap(10);
                btn.getStyleClass().add("category-button");

                // Make button fill width of sidebar
                btn.setMaxWidth(Double.MAX_VALUE);

                sidebar.getChildren().add(btn);
            } catch (Exception e) {
                System.err.println("Error loading image for category: " + category[0] + " - " + e.getMessage());
                Button btn = new Button(category[0]);
                btn.getStyleClass().add("category-button");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setAlignment(Pos.CENTER_LEFT);
                sidebar.getChildren().add(btn);
            }
        }

        // Add some padding to the sidebar
        sidebar.setPadding(new Insets(10));

        // Center content - Main banner and products
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("main-scroll-pane");

        VBox centerContent = new VBox(30);
        centerContent.getStyleClass().add("content-area");
        centerContent.setPadding(new Insets(10));

        HBox topView = new HBox(20);
        topView.getStyleClass().add("content-area");
        topView.setPadding(new Insets(10));

        VBox mainBannerContent = new VBox(0);
        mainBannerContent.getStyleClass().add("content-area");

        // Main promotional banner
        ImageView mainBanner = new ImageView(new Image(getClass().getResourceAsStream("/images/mainBanner.png")));
        mainBanner.setFitWidth(800);
        mainBanner.setPreserveRatio(true);

        // Side banners container
        VBox sideBanners = new VBox(5);
        sideBanners.setAlignment(Pos.CENTER);

        // Side promotional banners
        ImageView sideBanner1 = new ImageView(new Image(getClass().getResourceAsStream("/images/sideBanner1.png")));
        sideBanner1.setFitWidth(300);
        sideBanner1.setPreserveRatio(true);

        ImageView sideBanner2 = new ImageView(new Image(getClass().getResourceAsStream("/images/sideBanner2.png")));
        sideBanner2.setFitWidth(300);
        sideBanner2.setPreserveRatio(true);

        ImageView sideBanner3 = new ImageView(new Image(getClass().getResourceAsStream("/images/sideBanner3.png")));
        sideBanner3.setFitWidth(300);
        sideBanner3.setPreserveRatio(true);

        sideBanners.getChildren().addAll(sideBanner1, sideBanner2, sideBanner3);

        // Product showcase
        VBox productShowcase = new VBox(0);
        productShowcase.getStyleClass().add("product-showcase");

        // Featured products
        HBox featuredProducts = new HBox(0);
        featuredProducts.setAlignment(Pos.CENTER);

        String[][] products = {
                {"IPHONE 16 PRO MAX", "Lên đời ngay"},
                {"OPPO FIND N5", "Đặt gạch ngay"},
                {"REDMI NOTE 14 5G", "Ưu đãi tốt chốt ngay"},
                {"GALAXY S25 ULTRA", "Giá tốt chốt ngay"},
                {"VIVO Y04", "Giá chỉ từ 2.99 trieu"}
        };

        for (String[] product : products) {
            VBox productCard = createBannerProductCard(product[0], product[1]);
            featuredProducts.getChildren().add(productCard);
        }

        productShowcase.getChildren().add(featuredProducts);

        GridPane productGrid = createProductGrid();
        productGrid.setPadding(new Insets(10));

        // Add all components to center content
        mainBannerContent.getChildren().addAll(mainBanner, productShowcase);
        topView.getChildren().addAll(sidebar, mainBannerContent, sideBanners);
        centerContent.getChildren().addAll(topView, productGrid);

        scrollPane.setContent(centerContent);
        content.setCenter(scrollPane);

        return content;
    }

    private VBox createBannerProductCard(String title, String subtitle) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER);

        try {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("product-title");

            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.getStyleClass().add("product-subtitle");

            card.getChildren().addAll(titleLabel, subtitleLabel);
        } catch (Exception e) {
            System.err.println("Error creating product card: " + e.getMessage());
        }

        return card;
    }

    private List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();

        // Load products from smartphones.json
        try (InputStream is = getClass().getResourceAsStream("/smartphones.json");
             JsonReader reader = Json.createReader(is)) {

            JsonArray jsonArray = reader.readArray();
            for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
                Product product = new Product();
                product.name = jsonObject.getString("name", "");
                product.productUrl = jsonObject.getString("productUrl", "");
                product.imageUrl = jsonObject.getString("imageUrl", "");
                product.price = jsonObject.getJsonNumber("price").doubleValue();
                product.priceCurrency = jsonObject.getString("priceCurrency", "");
                product.overallRating = jsonObject.getJsonNumber("overallRating").doubleValue();
                product.reviewCount = jsonObject.getInt("reviewCount", 0);

                // Load description (assuming it's a String in JSON)
                product.description = jsonObject.getString("description", null);

                // Parse specifications
                if (jsonObject.containsKey("specifications")) {
                    product.specifications = new java.util.LinkedHashMap<>();
                    javax.json.JsonObject specsObj = jsonObject.getJsonObject("specifications");
                    for (String key : specsObj.keySet()) {
                        product.specifications.put(key, specsObj.getString(key, ""));
                    }
                }

                // Load reviews (assuming it's nested within categoryData) - Need to handle potential null/missing keys
                product.reviews = new ArrayList<>();
                if (jsonObject.containsKey("categoryData")) {
                    JsonObject categoryDataObject = jsonObject.getJsonObject("categoryData");
                    if (categoryDataObject != null && categoryDataObject.containsKey("reviews")) {
                        JsonArray reviewsArray = categoryDataObject.getJsonArray("reviews");
                        if (reviewsArray != null) {
                            for (JsonObject reviewObject : reviewsArray.getValuesAs(JsonObject.class)) {
                                String author = reviewObject.getString("author", "");
                                String content = reviewObject.getString("content", "");
                                product.reviews.add(new Product.Review(author, content));
                            }
                        }
                    }
                }

                products.add(product);

                // Debug log
                System.out.println("Found product image URL: " + product.imageUrl);
                imageUrls.add(product.imageUrl);
            }

        } catch (Exception e) {
            System.err.println("Error loading products from smartphones.json: " + e.getMessage());
            e.printStackTrace();
        }

        // Load products from laptops.json
        try (InputStream is = getClass().getResourceAsStream("/laptops.json");
             JsonReader reader = Json.createReader(is)) {

            JsonArray jsonArray = reader.readArray();
            for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
                Product product = new Product();
                product.name = jsonObject.getString("name", "");
                product.productUrl = jsonObject.getString("productUrl", "");
                product.imageUrl = jsonObject.getString("imageUrl", "");
                product.price = jsonObject.getJsonNumber("price").doubleValue();
                product.priceCurrency = jsonObject.getString("priceCurrency", "");
                product.overallRating = jsonObject.getJsonNumber("overallRating").doubleValue();
                product.reviewCount = jsonObject.getInt("reviewCount", 0);

                // Load description (assuming it's a String in JSON)
                product.description = jsonObject.getString("description", null);

                // Parse specifications
                if (jsonObject.containsKey("specifications")) {
                    product.specifications = new java.util.LinkedHashMap<>();
                    javax.json.JsonObject specsObj = jsonObject.getJsonObject("specifications");
                    for (String key : specsObj.keySet()) {
                        product.specifications.put(key, specsObj.getString(key, ""));
                    }
                }

                // Load reviews (assuming it's nested within categoryData) - Need to handle potential null/missing keys
                product.reviews = new ArrayList<>();
                if (jsonObject.containsKey("categoryData")) {
                    JsonObject categoryDataObject = jsonObject.getJsonObject("categoryData");
                    if (categoryDataObject != null && categoryDataObject.containsKey("reviews")) {
                        JsonArray reviewsArray = categoryDataObject.getJsonArray("reviews");
                        if (reviewsArray != null) {
                            for (JsonObject reviewObject : reviewsArray.getValuesAs(JsonObject.class)) {
                                String author = reviewObject.getString("author", "");
                                String content = reviewObject.getString("content", "");
                                product.reviews.add(new Product.Review(author, content));
                            }
                        }
                    }
                }

                products.add(product);

                // Debug log
                System.out.println("Found product image URL: " + product.imageUrl);
                imageUrls.add(product.imageUrl);
            }

        } catch (Exception e) {
            System.err.println("Error loading products from laptops.json: " + e.getMessage());
            e.printStackTrace();
        }

        // Preload all images
        System.out.println("Preloading " + imageUrls.size() + " images...");
        ImageCache.preloadImages(imageUrls);

        return products;
    }

    private GridPane createProductGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        List<Product> products = loadProducts();

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            VBox productCard = createProductCard(product);
            grid.add(productCard, i % 5, i / 5);
        }

        return grid;
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setMinWidth(250);
        card.setMaxWidth(250);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        // Product image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(200);
        imageContainer.setAlignment(Pos.CENTER);

        // Create a placeholder while image loads
        Label loadingLabel = new Label("Loading...");
        loadingLabel.setStyle("-fx-background-color: #f5f5f5; -fx-alignment: center;");
        imageContainer.getChildren().add(loadingLabel);

        card.getChildren().add(imageContainer);

        // Load image asynchronously
        ImageCache.getImage(product.imageUrl).thenAccept(image -> {
            if (image != null) {
                System.out.println("Successfully loaded image: " + product.imageUrl);
                javafx.application.Platform.runLater(() -> {
                    try {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(190);
                        imageView.setFitHeight(190);
                        imageView.setPreserveRatio(true);

                        if (imageView.getImage().isError()) {
                            throw new Exception("Failed to load image: " + product.imageUrl);
                        }

                        imageContainer.getChildren().clear();
                        imageContainer.getChildren().add(imageView);
                    } catch (Exception e) {
                        System.err.println("Error displaying image: " + e.getMessage());
                        imageContainer.getChildren().clear();
                        Label errorLabel = new Label("Image not available");
                        errorLabel.setStyle("-fx-background-color: #f5f5f5; -fx-alignment: center;");
                        imageContainer.getChildren().add(errorLabel);
                    }
                });
            } else {
                System.err.println("Failed to load image: " + product.imageUrl);
                javafx.application.Platform.runLater(() -> {
                    imageContainer.getChildren().clear();
                    Label errorLabel = new Label("Image not availableeeeee");
                    errorLabel.setStyle("-fx-background-color: #f5f5f5; -fx-alignment: center;");
                    imageContainer.getChildren().add(errorLabel);
                });
            }
        });

        // Product name
        Label nameLabel = new Label(product.name);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        nameLabel.setPrefHeight(35);
        card.getChildren().add(nameLabel);

        // Price information
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label currentPriceLabel = new Label(String.format("%,.0f %s", product.price*1000, product.priceCurrency));
        currentPriceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: red;");
        priceBox.getChildren().add(currentPriceLabel);
        card.getChildren().add(priceBox);

        // Rating and details button
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        // Stars rating (hardcoded for now)
        HBox starsBox = new HBox(2);
        for (int i = 0; i < 5; i++) {
            Label star = new Label("★");
            star.setTextFill(Color.ORANGE);
            starsBox.getChildren().add(star);
        }

        Button detailBtn = new Button("Chi tiết");
        detailBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5;");
        detailBtn.setPrefWidth(100);

        detailBtn.setOnAction(e -> Router.navigateTo(new ProductPage(product).createScene()));

        HBox.setHgrow(starsBox, Priority.ALWAYS);
        bottomRow.getChildren().addAll(starsBox, detailBtn);
        card.getChildren().add(bottomRow);

        return card;
    }
}