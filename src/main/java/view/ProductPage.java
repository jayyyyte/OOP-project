package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import util.Router;
import view.HomePage.Product;
import util.ImageCache;

public class ProductPage {  
    private Product product;

    public ProductPage(Product product) {
        this.product = product;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");
        
        // Header with product name and ratings
        HBox header = createHeader();
        root.setTop(header);
        
        // Main content area (Now only two sections: left scrollable and right)
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));
        
        // Left section (Scrollable content: Image/Features, Description, Reviews)
        ScrollPane leftScrollPane = createLeftScrollPane();
        
        // Right side - Purchase options
        VBox rightSection = createRightSection();
        rightSection.setMaxWidth(400);

        mainContent.getChildren().addAll(leftScrollPane, rightSection);
        root.setCenter(mainContent);
        
        // // Footer with promotions
        // VBox footer = createFooter();
        // root.setBottom(footer);

        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        scene.getStylesheets().add(getClass().getResource("/product-page-styles.css").toExternalForm());
        
        return scene;
    }
    
    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        // Go back button
        Button backButton = new Button("← Quay lại");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #1976d2; -fx-font-weight: bold;");

        // Navigate back to HomePage when clicked
        backButton.setOnAction(e -> {
            Router.navigateTo(new HomePage().createScene());
        });

        // Product name
        Label productName = new Label(product.name);
        productName.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        // Star ratings
        HBox ratings = new HBox(2);
        int rating = (int) Math.round(product.overallRating);
        for (int i = 0; i < 5; i++) {
            Label star = new Label("★");
            if (i < rating) {
                 star.setTextFill(Color.ORANGE);
            } else {
                 star.setTextFill(Color.GRAY);
            }
            star.setFont(Font.font("System", 16));
            ratings.getChildren().add(star);
        }
        
        // Ratings count
        Label ratingsCount = new Label(product.reviewCount + " đánh giá");
        ratingsCount.setTextFill(Color.GRAY);
        
        // Compare button
        Button compareButton = new Button("+ So sánh");
        compareButton.setStyle("-fx-border-color: #e74c3c; -fx-text-fill: #e74c3c; -fx-background-color: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(backButton, productName, ratings, ratingsCount, spacer, compareButton);
        return header;
    }
    
    private ScrollPane createLeftScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox leftContent = new VBox(20);
        leftContent.setPadding(new Insets(0));
        leftContent.setAlignment(Pos.TOP_CENTER);

        HBox topView = new HBox(20);
        topView.setAlignment(Pos.TOP_LEFT);
        topView.setPadding(new Insets(0));

        VBox imageSection = createImageSection();

        VBox featuresSection = createFeaturesSection();

        topView.getChildren().addAll(imageSection, featuresSection);

        VBox descriptionSection = createDescriptionSection();

        VBox reviewsSection = createReviewsSection();

        leftContent.getChildren().addAll(topView, descriptionSection, reviewsSection);

        scrollPane.setContent(leftContent);
        return scrollPane;
    }
    
    private VBox createImageSection() {
        VBox imageSection = new VBox(15);
        imageSection.setMaxWidth(400);
        
        StackPane imageContainer = new StackPane();
        Rectangle background = new Rectangle(380, 380);
        Stop[] stops = new Stop[] { new Stop(0, Color.rgb(219, 112, 147)), new Stop(1, Color.rgb(255, 178, 107)) };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, null, stops);
        background.setFill(gradient);
        background.setArcWidth(20);
        background.setArcHeight(20);
        
        ImageView mainImage = new ImageView();
        ImageCache.getImage(product.imageUrl).thenAccept(image -> {
             if (image != null) {
                System.out.println("Successfully loaded product image: " + product.imageUrl);
                javafx.application.Platform.runLater(() -> {
                    mainImage.setImage(image);
                });
            } else {
                System.err.println("Failed to load product image: " + product.imageUrl);
                javafx.application.Platform.runLater(() -> {
                    mainImage.setImage(createPlaceholderImage(350, 350).getImage());
                });
            }
        });

        mainImage.setFitWidth(350);
        mainImage.setFitHeight(350);
        mainImage.setPreserveRatio(true);
        
        Button heartButton = new Button("♥");
        heartButton.setStyle("-fx-background-color: white; -fx-text-fill: #e74c3c; -fx-font-size: 18px; " +
                            "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                            "-fx-padding: 0;");
        StackPane.setAlignment(heartButton, Pos.TOP_LEFT);
        StackPane.setMargin(heartButton, new Insets(15));
        
        imageContainer.getChildren().addAll(background, mainImage, heartButton);
        
        imageSection.getChildren().addAll(imageContainer);
        return imageSection;
    }
    
    private VBox createFeaturesSection() {
        VBox featuresSection = new VBox(15);
        featuresSection.setStyle("-fx-background-color: linear-gradient(to right, #db7093, #ffb26b); -fx-background-radius: 15;");
        featuresSection.setPadding(new Insets(20));
        featuresSection.setMaxWidth(320);
        
        Label featureTitle = new Label("TÍNH NĂNG NỔI BẬT");
        featureTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        featureTitle.setTextFill(Color.WHITE);
        
        VBox featureList = new VBox(15);
        
        String[] features = {
            "Màn hình Super Retina XDR 6,9 inch lớn hơn có viền mỏng hơn, đem đến cảm giác tuyệt vời khi cầm trên tay.",
            "Điều khiển Camera - Chỉ cần trượt ngón tay để điều chỉnh camera giúp chụp ảnh hoặc quay video đẹp hoàn hảo và siêu nhanh.",
            "iPhone 16 Pro Max có thiết kế titan cấp 5 với lớp hoàn thiện mới, tinh tế được xử lý bề mặt vi điểm.",
            "iPhone 16 Pro Max được cài đặt sẵn hệ điều hành iOS 18, cho trải nghiệm người dùng mượt mà."
        };
        
        for (String feature : features) {
            HBox featureItem = new HBox(10);
            
            Label bullet = new Label("•");
            bullet.setTextFill(Color.WHITE);
            bullet.setFont(Font.font("System", FontWeight.BOLD, 16));
            
            Label featureText = new Label(feature);
            featureText.setTextFill(Color.WHITE);
            featureText.setWrapText(true);
            
            featureItem.getChildren().addAll(bullet, featureText);
            featureList.getChildren().add(featureItem);
        }
        
        featuresSection.getChildren().addAll(featureTitle, featureList);
        return featuresSection;
    }
    
    private VBox createDescriptionSection() {
        VBox descriptionSection = new VBox(10);
        descriptionSection.setPadding(new Insets(20));
        descriptionSection.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 1);");
        descriptionSection.getStyleClass().add("description-section");
        descriptionSection.setMaxWidth(750);
        
        Label descriptionTitle = new Label("Mô tả sản phẩm");
        descriptionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Label descriptionContent = new Label(product.description != null ? product.description : "Không có mô tả sản phẩm.");
        descriptionContent.setWrapText(true);
        
        descriptionSection.getChildren().addAll(descriptionTitle, descriptionContent);
        return descriptionSection;
    }

    private VBox createReviewsSection() {
        VBox reviewsSection = new VBox(15);
        reviewsSection.setPadding(new Insets(20));
        reviewsSection.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 1);");
        reviewsSection.getStyleClass().add("reviews-section");
        reviewsSection.setMaxWidth(750);
        
        Label reviewsTitle = new Label("Đánh giá sản phẩm");
        reviewsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        reviewsSection.getChildren().add(reviewsTitle);

        if (product.reviews != null && !product.reviews.isEmpty()) {
            for (Product.Review review : product.reviews) {
                VBox reviewBox = new VBox(5);
                reviewBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-padding: 10;");

                Label authorLabel = new Label(review.author != null ? review.author : "Ẩn danh");
                authorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                Label contentLabel = new Label(review.content != null ? review.content : "Không có nội dung đánh giá.");
                contentLabel.setWrapText(true);

                reviewBox.getChildren().addAll(authorLabel, contentLabel);
                reviewsSection.getChildren().add(reviewBox);
            }
        } else {
            Label noReviewsLabel = new Label("Chưa có đánh giá nào cho sản phẩm này.");
            noReviewsLabel.setTextFill(Color.GRAY);
            reviewsSection.getChildren().add(noReviewsLabel);
        }
        
        return reviewsSection;
    }
    
    private VBox createRightSection() {
        VBox rightSection = new VBox(20);
        rightSection.setPadding(new Insets(10));
        rightSection.setMaxWidth(400);
        
        Label storageLabel = new Label("Chọn dung lượng");
        storageLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        HBox storageOptions = new HBox(10);
        
        String[][] storageData = {
            {"1TB", "42.990.000 đ"},
            {"512GB", "37.490.000 đ"},
            {"256GB", "30.990.000 đ"}
        };
        
        ToggleGroup storageGroup = new ToggleGroup();
        
        for (String[] data : storageData) {
            VBox option = new VBox(5);
            option.setAlignment(Pos.CENTER);
            option.setPadding(new Insets(10));
            option.setPrefWidth(100);
            
            ToggleButton toggle = new ToggleButton(data[0]);
            toggle.setToggleGroup(storageGroup);
            toggle.setStyle("-fx-background-color: transparent; -fx-opacity: 0;");
            
            Label capacity = new Label(data[0]);
            capacity.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            Label price = new Label(data[1]);
            price.setFont(Font.font("System", 12));
            
            if (data[0].equals("256GB")) {
                toggle.setSelected(true);
                option.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 5;");
            } else {
                option.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
            }
            
            option.getChildren().addAll(capacity, price, toggle);
            storageOptions.getChildren().add(option);
        }
        
        Label colorLabel = new Label("Chọn màu để xem giá và chỉ nhánh có hàng");
        colorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        GridPane colorOptions = new GridPane();
        colorOptions.setHgap(10);
        colorOptions.setVgap(10);
        
        String[][] colorData = {
            {"Titan Tự Nhiên", "31.290.000đ"},
            {"Titan Đen", "30.990.000đ"},
            {"Titan Sa Mạc", "30.990.000đ"},
            {"Titan Trắng", "30.990.000đ"}
        };
        
        ToggleGroup colorGroup = new ToggleGroup();
        
        for (int i = 0; i < colorData.length; i++) {
            VBox option = new VBox(5);
            option.setAlignment(Pos.CENTER_LEFT);
            option.setPadding(new Insets(10));
            option.setPrefWidth(185);
            
            ToggleButton toggle = new ToggleButton(colorData[i][0]);
            toggle.setToggleGroup(colorGroup);
            toggle.setStyle("-fx-background-color: transparent; -fx-opacity: 0;");
            
            HBox colorInfo = new HBox(10);
            
            Rectangle colorSwatch = new Rectangle(20, 20);
            colorSwatch.setArcWidth(5);
            colorSwatch.setArcHeight(5);
            
            switch (colorData[i][0]) {
                case "Titan Tự Nhiên":
                    colorSwatch.setFill(Color.rgb(200, 180, 160));
                    break;
                case "Titan Đen":
                    colorSwatch.setFill(Color.rgb(50, 50, 50));
                    break;
                case "Titan Sa Mạc":
                    colorSwatch.setFill(Color.rgb(220, 200, 180));
                    break;
                case "Titan Trắng":
                    colorSwatch.setFill(Color.rgb(240, 240, 240));
                    break;
            }
            
            VBox textInfo = new VBox(2);
            Label colorName = new Label(colorData[i][0]);
            colorName.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            Label colorPrice = new Label(colorData[i][1]);
            colorPrice.setFont(Font.font("System", 12));
            
            textInfo.getChildren().addAll(colorName, colorPrice);
            colorInfo.getChildren().addAll(colorSwatch, textInfo);
            
            if (colorData[i][0].equals("Titan Đen")) {
                toggle.setSelected(true);
                option.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 5;");
            } else {
                option.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
            }
            
            option.getChildren().addAll(colorInfo, toggle);
            colorOptions.add(option, i % 2, i / 2);
        }
        
        HBox pricingSection = new HBox(15);
        
        VBox tradeInPrice = new VBox(5);
        tradeInPrice.setAlignment(Pos.CENTER);
        tradeInPrice.setPadding(new Insets(10));
        tradeInPrice.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");
        
        Label tradeInPriceValue = new Label("27.990.000đ");
        tradeInPriceValue.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Label tradeInLabel = new Label("Khi thu cũ lên đời");
        tradeInLabel.setFont(Font.font("System", 12));
        tradeInLabel.setTextFill(Color.GRAY);
        
        tradeInPrice.getChildren().addAll(tradeInPriceValue, tradeInLabel);
        
        VBox regularPrice = new VBox(5);
        regularPrice.setAlignment(Pos.CENTER);
        regularPrice.setPadding(new Insets(10));
        regularPrice.setPrefWidth(200);
        regularPrice.setStyle("-fx-background-color: white; -fx-border-color: #e74c3c; -fx-border-radius: 5;");
        
        Label currentPrice = new Label("30.990.000đ");
        currentPrice.setFont(Font.font("System", FontWeight.BOLD, 18));
        currentPrice.setTextFill(Color.RED);
        
        Label originalPrice = new Label("34.990.000đ");
        originalPrice.setFont(Font.font("System", 14));
        originalPrice.setTextFill(Color.GRAY);
        originalPrice.setStyle("-fx-strikethrough: true;");
        
        regularPrice.getChildren().addAll(currentPrice, originalPrice);
        
        pricingSection.getChildren().addAll(tradeInPrice, regularPrice);
        
        HBox memberDiscount = new HBox(5);
        memberDiscount.setAlignment(Pos.CENTER_LEFT);
        
        Label discountLabel = new Label("Tiết kiệm thêm đến ");
        discountLabel.setFont(Font.font("System", 14));
        
        Label discountAmount = new Label("310.000đ");
        discountAmount.setFont(Font.font("System", FontWeight.BOLD, 14));
        discountAmount.setTextFill(Color.RED);
        
        Label memberLabel = new Label(" cho Smember");
        memberLabel.setFont(Font.font("System", 14));
        
        memberDiscount.getChildren().addAll(discountLabel, discountAmount, memberLabel);
        
        Hyperlink checkPriceLink = new Hyperlink("Kiểm tra giá cuối cùng của bạn >");
        checkPriceLink.setTextFill(Color.RED);
        
        rightSection.getChildren().addAll(
            storageLabel, storageOptions, 
            colorLabel, colorOptions, 
            pricingSection, memberDiscount, checkPriceLink
        );
        
        return rightSection;
    }
    
    private ImageView createPlaceholderImage(double width, double height) {
        Rectangle placeholder = new Rectangle(width, height);
        placeholder.setFill(Color.LIGHTGRAY);
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        
        return imageView;
    }
}