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

public class ProductPage {
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");
        
        // Header with product name and ratings
        HBox header = createHeader();
        root.setTop(header);
        
        // Main content area
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));
        
        // Left side - Product image and thumbnails
        VBox leftSection = createLeftSection();
        
        // Center - Product features
        VBox centerSection = createCenterSection();
        
        // Right side - Purchase options
        VBox rightSection = createRightSection();
        
        mainContent.getChildren().addAll(leftSection, centerSection, rightSection);
        root.setCenter(mainContent);
        
        // Footer with promotions
        VBox footer = createFooter();
        root.setBottom(footer);

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
        Label productName = new Label("iPhone 16 Pro Max 256GB | Chính hãng VN/A");
        productName.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        // Star ratings
        HBox ratings = new HBox(2);
        for (int i = 0; i < 5; i++) {
            Label star = new Label("★");
            star.setTextFill(Color.ORANGE);
            star.setFont(Font.font("System", 16));
            ratings.getChildren().add(star);
        }
        
        // Ratings count
        Label ratingsCount = new Label("277 đánh giá");
        ratingsCount.setTextFill(Color.GRAY);
        
        // Compare button
        Button compareButton = new Button("+ So sánh");
        compareButton.setStyle("-fx-border-color: #e74c3c; -fx-text-fill: #e74c3c; -fx-background-color: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(backButton, productName, ratings, ratingsCount, spacer, compareButton);
        return header;
    }
    
    private VBox createLeftSection() {
        VBox leftSection = new VBox(15);
        leftSection.setMaxWidth(400);
        
        // Product image with gradient background
        StackPane imageContainer = new StackPane();
        Rectangle background = new Rectangle(380, 380);
        Stop[] stops = new Stop[] { new Stop(0, Color.rgb(219, 112, 147)), new Stop(1, Color.rgb(255, 178, 107)) };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, null, stops);
        background.setFill(gradient);
        background.setArcWidth(20);
        background.setArcHeight(20);
        
        ImageView mainImage = new ImageView();
        // Replace with actual image path
        try {
            mainImage.setImage(new Image(getClass().getResourceAsStream("/images/iphone-16.png")));
        } catch (Exception e) {
            System.out.println("Image not found. Using placeholder.");
            mainImage = createPlaceholderImage(350, 350);
        }
        mainImage.setFitWidth(350);
        mainImage.setFitHeight(350);
        mainImage.setPreserveRatio(true);
        
        // Heart icon
        Button heartButton = new Button("♥");
        heartButton.setStyle("-fx-background-color: white; -fx-text-fill: #e74c3c; -fx-font-size: 18px; " +
                            "-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; " +
                            "-fx-padding: 0;");
        StackPane.setAlignment(heartButton, Pos.TOP_LEFT);
        StackPane.setMargin(heartButton, new Insets(15));
        
        imageContainer.getChildren().addAll(background, mainImage, heartButton);
        
        // Thumbnail images
        ScrollPane thumbnailsScroll = new ScrollPane();
        thumbnailsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        thumbnailsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        thumbnailsScroll.setPrefHeight(100);
        thumbnailsScroll.setStyle("-fx-background-color: transparent;");
        
        HBox thumbnails = new HBox(10);
        thumbnails.setPadding(new Insets(10));
        
        // Create thumbnails (small versions of the phone from different angles)
        for (int i = 0; i < 8; i++) {
            VBox thumbnail = new VBox();
            thumbnail.setAlignment(Pos.CENTER);
            ImageView thumbImage = createPlaceholderImage(70, 70);
            
            // First thumbnail should be highlighted
            if (i == 0) {
                Label featureLabel = new Label("Tính năng nổi bật");
                featureLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-background-color: #0066cc; " +
                                      "-fx-padding: 3 5; -fx-background-radius: 3;");
                thumbnail.getChildren().addAll(thumbImage, featureLabel);
                thumbnail.setStyle("-fx-border-color: #0066cc; -fx-border-radius: 5;");
            } else {
                thumbnail.getChildren().add(thumbImage);
                thumbnail.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
            }
            
            thumbnails.getChildren().add(thumbnail);
        }
        
        thumbnailsScroll.setContent(thumbnails);
        
        leftSection.getChildren().addAll(imageContainer, thumbnailsScroll);
        return leftSection;
    }
    
    private VBox createCenterSection() {
        VBox centerSection = new VBox(15);
        centerSection.setStyle("-fx-background-color: linear-gradient(to right, #db7093, #ffb26b); -fx-background-radius: 15;");
        centerSection.setPadding(new Insets(20));
        centerSection.setMaxWidth(320);
        
        // Feature title
        Label featureTitle = new Label("TÍNH NĂNG NỔI BẬT");
        featureTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        featureTitle.setTextFill(Color.WHITE);
        
        // Feature list
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
        
        // Next/Previous buttons
        HBox navigationButtons = new HBox();
        navigationButtons.setAlignment(Pos.CENTER_RIGHT);
        
        Button nextButton = new Button(">");
        nextButton.setStyle("-fx-background-color: white; -fx-text-fill: #666; -fx-background-radius: 50%; " +
                           "-fx-min-width: 40px; -fx-min-height: 40px; -fx-padding: 0;");
        
        navigationButtons.getChildren().add(nextButton);
        
        centerSection.getChildren().addAll(featureTitle, featureList, navigationButtons);
        return centerSection;
    }
    
    private VBox createRightSection() {
        VBox rightSection = new VBox(20);
        rightSection.setPadding(new Insets(10));
        rightSection.setMaxWidth(400);
        
        // Storage options
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
            
            // Set the last option (256GB) as selected
            if (data[0].equals("256GB")) {
                toggle.setSelected(true);
                option.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 5;");
            } else {
                option.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
            }
            
            option.getChildren().addAll(capacity, price, toggle);
            storageOptions.getChildren().add(option);
        }
        
        // Color options
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
            
            // Set different colors based on the name
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
            
            // Set the appropriate option as selected
            if (colorData[i][0].equals("Titan Đen")) {
                toggle.setSelected(true);
                option.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 5;");
            } else {
                option.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
            }
            
            option.getChildren().addAll(colorInfo, toggle);
            colorOptions.add(option, i % 2, i / 2);
        }
        
        // Pricing section
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
        
        // Member discount
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
        
        // Check final price link
        Hyperlink checkPriceLink = new Hyperlink("Kiểm tra giá cuối cùng của bạn >");
        checkPriceLink.setTextFill(Color.RED);
        
        // Buy buttons would go here
        
        rightSection.getChildren().addAll(
            storageLabel, storageOptions, 
            colorLabel, colorOptions, 
            pricingSection, memberDiscount, checkPriceLink
        );
        
        return rightSection;
    }
    
    private VBox createFooter() {
        VBox footer = new VBox(15);
        footer.setPadding(new Insets(15));
        
        // Promotion banner
        HBox promotion = new HBox();
        promotion.setPrefHeight(100);
        promotion.setStyle("-fx-background-color: #ffb6c1; -fx-background-radius: 10;");
        promotion.setPadding(new Insets(15));
        promotion.setAlignment(Pos.CENTER_LEFT);
        
        Label promoText = new Label("TẶNG 300K Cho khách hàng mới Khi mua iPhone 16 Pro Max");
        promoText.setFont(Font.font("System", FontWeight.BOLD, 18));
        promoText.setTextFill(Color.WHITE);
        
        Button promoButton = new Button("Nhận Ngay");
        promoButton.setStyle("-fx-background-color: white; -fx-text-fill: #e74c3c;");
        
        Region promoSpacer = new Region();
        HBox.setHgrow(promoSpacer, Priority.ALWAYS);
        
        promotion.getChildren().addAll(promoText, promoSpacer, promoButton);
        
        // Promotions section
        HBox promotionsSection = new HBox(15);
        
        // Gift icon
        Label giftIcon = new Label("🎁");
        giftIcon.setFont(Font.font("System", 24));
        
        Label promoTitle = new Label("Khuyến mãi");
        promoTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // Customer service
        Button customerServiceButton = new Button("Chat với nhân viên tư vấn");
        customerServiceButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        
        Region promotionSpacer = new Region();
        HBox.setHgrow(promotionSpacer, Priority.ALWAYS);
        
        promotionsSection.getChildren().addAll(giftIcon, promoTitle, promotionSpacer, customerServiceButton);
        
        footer.getChildren().addAll(promotion, promotionsSection);
        return footer;
    }
    
    private ImageView createPlaceholderImage(double width, double height) {
        // Create a placeholder image when actual images are not available
        Rectangle placeholder = new Rectangle(width, height);
        placeholder.setFill(Color.LIGHTGRAY);
        
        // Convert Rectangle to Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        
        return imageView;
    }
}