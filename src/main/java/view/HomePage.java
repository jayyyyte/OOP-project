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

    private final BannerItem[] bannerItems = {
        new BannerItem("📱", "Thu cu doi moi - Giam den 3 trieu"),
        new BannerItem("🚚", "Mien phi van chuyen toan quoc"),
        new BannerItem("✅", "San pham chinh hang 100%"),
        new BannerItem("✅", "Tang qua khi mua online"),
        new BannerItem("✅", "Sale soc - Giam den 50%"),
        new BannerItem("✅", "Bao hanh chinh hang 12 thang")
    };

    // Define a Product class to match the JSON structure
    private class Product {
        String name;
        String productUrl;
        String imageUrl;
        double price;
        String priceCurrency;
        double overallRating;
        int reviewCount;
        // Add other fields as necessary
    }

    public Scene createScene() {
        VBox root = new VBox(0);

        // Top banner
        HBox topBanner = createTopBanner();

        // Navigation bar
        HBox navbar = createNavigationBar();

        // Main content
        BorderPane mainContent = createMainContent();

        // Add all components to root
        root.getChildren().addAll(topBanner, navbar, mainContent);

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

        startBannerAnimation();
        return scene;
    }

    private void startBannerAnimation() {
        bannerTimeline = new Timeline(
            new KeyFrame(Duration.seconds(4), e -> nextBanner())
        );
        bannerTimeline.setCycleCount(Timeline.INDEFINITE);
        bannerTimeline.play();
    }

    private void nextBanner() {
        currentBannerIndex = (currentBannerIndex + 1) % bannerItems.length;
        updateBannerContent();
    }

    private void previousBanner() {
        currentBannerIndex = (currentBannerIndex - 1 + bannerItems.length) % bannerItems.length;
        updateBannerContent();
    }

    private void updateBannerContent() {
        for (int i = 0; i < 3; i++) {
            int itemIndex = (currentBannerIndex + i) % bannerItems.length;
            BannerItem item = bannerItems[itemIndex];
            
            iconLabels[i].setText(item.icon);
            textLabels[i].setText(item.text);
        }
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

    private HBox createTopBanner() {
        HBox banner = new HBox();
        banner.getStyleClass().add("top-banner");
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(10));
        banner.setSpacing(15); // Add spacing between elements

        // Left arrow
        Button prevButton = new Button("<");
        prevButton.getStyleClass().add("banner-nav-button");
        prevButton.setOnAction(e -> {
            bannerTimeline.pause();
            previousBanner();
            bannerTimeline.play();
        });

        // Create container for 3 banner items
        bannerContainer = new HBox(30); // Space between items
        bannerContainer.setAlignment(Pos.CENTER);

        // Create 3 content boxes for items
        HBox[] contentBoxes = new HBox[3];
        iconLabels = new Label[3];
        textLabels = new Label[3];

        for (int i = 0; i < 3; i++) {
            contentBoxes[i] = new HBox(10);
            contentBoxes[i].setAlignment(Pos.CENTER);
            
            iconLabels[i] = new Label(bannerItems[i].icon);
            iconLabels[i].getStyleClass().add("banner-icon");
            
            textLabels[i] = new Label(bannerItems[i].text);
            textLabels[i].getStyleClass().add("banner-label");
            
            contentBoxes[i].getChildren().addAll(iconLabels[i], textLabels[i]);
            bannerContainer.getChildren().add(contentBoxes[i]);
        }

        // Right arrow
        Button nextButton = new Button(">");
        nextButton.getStyleClass().add("banner-nav-button");
        nextButton.setOnAction(e -> {
            bannerTimeline.pause();
            nextBanner();
            bannerTimeline.play();
        });

        // Add spacers for centering
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        banner.getChildren().addAll(
            leftSpacer,
            prevButton,
            bannerContainer,
            nextButton,
            rightSpacer
        );

        return banner;
    }

    private HBox createNavigationBar() {
        HBox navbar = new HBox(20);
        navbar.getStyleClass().add("nav-bar");
        navbar.setAlignment(Pos.CENTER);

        // Logo
        Label logo = new Label("CellphoneS");
        logo.getStyleClass().add("logo");

        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Ban can tim gi?");
        searchField.setPrefWidth(400);
        searchField.getStyleClass().add("search-field");
        
        // Add search functionality
        searchField.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                Router.navigateTo(new SearchResultsPage(query).createScene());
            }
        });
        
        // Navigation buttons
        Button categoryBtn = new Button("Danh muc");
        Button locationBtn = new Button("Dia diem");
        Button cartBtn = new Button("Gio hang");
        Button orderBtn = new Button("Tra cuu don hang");
        Button loginBtn = new Button("Dang nhap");
        Button themeBtn = new Button("Toggle Theme");
        
        categoryBtn.getStyleClass().add("nav-button-light");
        locationBtn.getStyleClass().add("nav-button-light");
        cartBtn.getStyleClass().add("nav-button");
        orderBtn.getStyleClass().add("nav-button");
        loginBtn.getStyleClass().add("nav-button");
        themeBtn.getStyleClass().add("nav-button");
        
        cartBtn.setOnAction(e -> {
            Router.navigateTo(new CartPage().createScene());
        });
        themeBtn.setOnAction(e -> toggleTheme());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        navbar.getChildren().addAll(
            logo, 
            categoryBtn,    
            locationBtn,    
            searchField, 
            spacer, 
            cartBtn, 
            orderBtn, 
            loginBtn, 
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
            {"Dien thoai, Tablet", "/images/phone_icon.png"},
            {"Laptop", "/images/laptop_icon.png"},
            {"Am thanh, Mic thu am", "/images/headphones_icon.png"},
            {"Dong ho, Camera", "/images/smartwatch_icon.png"},
            {"Phu kien", "/images/usb_icon.png"},
            {"PC, Man hinh, May in", "/images/pc_icon.png"}
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
                btn.setGraphicTextGap(10);  // Space between icon and text
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
            {"IPHONE 16 PRO MAX", "Len doi ngay"},
            {"OPPO FIND N5", "Dat gach ngay"},
            {"REDMI NOTE 14 5G", "Uu dai tot chot ngay"},
            {"GALAXY S25 ULTRA", "Gia tot chot ngay"},
            {"VIVO Y04", "Gia chi tu 2.99 trieu"}
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
        try (InputStream is = getClass().getResourceAsStream("/products.json");
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
                products.add(product);
            }
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
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
            VBox productCard = createProductCard(
                product.name, // name
                String.format("%,.0f %s", product.price, product.priceCurrency), // current price
                "",  // Assuming no original price in JSON
                "0",  // Assuming no discount percentage in JSON
                product.imageUrl
            );
            grid.add(productCard, i % 5, i / 5);
        }
        
        return grid;
    }

    private VBox createProductCard(String name, String currentPrice, String originalPrice, String discountPercent, String imagePath) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setMinWidth(250);
        card.setMaxWidth(250);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label discountLabel = new Label("Giam " + discountPercent + "%");
        discountLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;");
        
        Label installmentLabel = new Label("Tra gop 0%");
        installmentLabel.setStyle("-fx-background-color: white; -fx-text-fill: #0066cc; -fx-padding: 5 10; -fx-border-color: #0066cc; -fx-border-radius: 3;");
        installmentLabel.setTranslateX(30);
        
        topRow.getChildren().addAll(discountLabel, installmentLabel);
        card.getChildren().add(topRow);
        
        // Product image
        try {
            if (!imagePath.startsWith("/")) {
                imagePath = "/" + imagePath;
            }
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
            imageView.setFitWidth(190);
            imageView.setFitHeight(190);
            imageView.setPreserveRatio(true);
            
            StackPane imageContainer = new StackPane(imageView);
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.setPrefHeight(200);
            card.getChildren().add(imageContainer);
        } catch (Exception e) {
            // Fallback for missing images
            Label imageLabel = new Label("Image not available");
            imageLabel.setAlignment(Pos.CENTER);
            imageLabel.setPrefHeight(200);
            imageLabel.setStyle("-fx-background-color: #f5f5f5; -fx-alignment: center;");
            card.getChildren().add(imageLabel);
        }
        
        // Product name
        Label nameLabel = new Label(name);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        card.getChildren().add(nameLabel);
        
        // Price information
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        
        Label currentPriceLabel = new Label(currentPrice);
        currentPriceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: red;");
        priceBox.getChildren().add(currentPriceLabel);
        
        if (!originalPrice.isEmpty()) {
            Label originalPriceLabel = new Label(originalPrice);
            originalPriceLabel.setStyle("-fx-strikethrough: true; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: gray;");
            priceBox.getChildren().add(originalPriceLabel);
        }
        
        card.getChildren().add(priceBox);
        
        // Additional benefits
        Label benefitLabel = new Label("Smember giảm thêm đến 310.000đ");
        benefitLabel.setFont(Font.font("System", 12));
        card.getChildren().add(benefitLabel);
        
        // Rating and like button
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        
        // Stars rating
        HBox starsBox = new HBox(2);
        for (int i = 0; i < 5; i++) {
            Label star = new Label("★");
            star.setTextFill(Color.ORANGE);
            starsBox.getChildren().add(star);
        }
        
        Button likeButton = new Button("Yêu thích ♡");
        likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666;");
        
        Button detailBtn = new Button("Chi tiết");
        detailBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5;");
        detailBtn.setPrefWidth(100);

        detailBtn.setOnAction(e -> Router.navigateTo(new ProductPage().createScene()));

        HBox.setHgrow(starsBox, Priority.ALWAYS);
        bottomRow.getChildren().addAll(starsBox, likeButton, detailBtn);
        card.getChildren().add(bottomRow);
        
        return card;
    }
}