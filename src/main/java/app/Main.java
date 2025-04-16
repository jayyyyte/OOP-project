import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.Node;

public class Main extends Application {
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

    @Override
    public void start(Stage primaryStage) {
        try {
            // Main container
            VBox root = new VBox(0); // Reduced spacing

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
            
            // Create scene with screen dimensions
            scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
            
            // Load default theme
            String stylesPath = getClass().getResource("/styles.css").toExternalForm();
            if (stylesPath != null) {
                scene.getStylesheets().add(stylesPath);
            } else {
                System.err.println("Warning: Could not load styles.css");
            }
            
            primaryStage.setTitle("CellphoneS Clone");
            primaryStage.setScene(scene);
            
            // Make window maximized by default
            primaryStage.setMaximized(true);
            
            // Add F11 key handler for toggling full screen
            scene.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case F11:
                        primaryStage.setFullScreen(!primaryStage.isFullScreen());
                        break;
                    default:
                        break;
                }
            });
            
            // Start banner animation
            startBannerAnimation();
            
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting application: " + e.getMessage());
        }
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
                
                // Align content to left
                btn.setAlignment(Pos.CENTER_LEFT);
                
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
        content.setLeft(sidebar);
        
        // Center content - Main banner and products
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("main-scroll-pane");
        
        VBox centerContent = new VBox(10);
        centerContent.getStyleClass().add("content-area");
        centerContent.setPadding(new Insets(10));
        
        // Main promotional banner
        ImageView mainBanner = new ImageView(new Image(getClass().getResourceAsStream("/images/mainBanner.png")));
        mainBanner.setFitWidth(1000);
        mainBanner.setPreserveRatio(true);
        
        // Side banners container
        HBox sideBanners = new HBox(15);
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
        VBox productShowcase = new VBox(10);
        productShowcase.getStyleClass().add("product-showcase");
        
        // Featured products
        HBox featuredProducts = new HBox(10);
        featuredProducts.setAlignment(Pos.CENTER);
        
        String[][] products = {
            {"IPHONE 16 PRO MAX", "Len doi ngay"},
            {"OPPO FIND N5", "Dat gach ngay"},
            {"REDMI NOTE 14 5G", "Uu dai tot chot ngay"},
            {"GALAXY S25 ULTRA", "Gia tot chot ngay"},
            {"VIVO Y04", "Gia chi tu 2.99 trieu"}
        };
        
        for (String[] product : products) {
            VBox productCard = createProductCard(product[0], product[1]);
            featuredProducts.getChildren().add(productCard);
        }
        
        productShowcase.getChildren().add(featuredProducts);
        
        // Add all components to center content
        centerContent.getChildren().addAll(mainBanner, sideBanners, productShowcase);
        
        scrollPane.setContent(centerContent);
        content.setCenter(scrollPane);
        
        return content;
    }
    
    private VBox createProductCard(String title, String subtitle) {
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

    public static void main(String[] args) {
        launch(args);
    }
}