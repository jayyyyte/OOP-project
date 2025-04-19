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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import util.Router;
import view.HomePage;

public class CartPage {
    public Scene createScene() {
        // Root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Header with back-button and title
        HBox headerBar = createHeaderBar();
        root.setTop(headerBar);
        headerBar.setAlignment(Pos.CENTER);

        // Main content
        VBox content = new VBox(400);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
            createCartToggleBar(),
            createSelectionBar(),
            createCartItem(),
            createProtectionBar(),
            createRecommendationBar(),
            createSummaryBar()
        );
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        root.setCenter(scroll);

        // Full-screen sizing
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        return scene;
    }

    private HBox createHeaderBar() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");
        backBtn.setOnAction(e -> Router.navigateTo(new HomePage().createScene()));

        Label title = new Label("Giỏ hàng của bạn");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        header.getChildren().addAll(backBtn, title);
        return header;
    }

    private HBox createCartToggleBar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(10, 0, 0, 0));

        Button cartBtn = new Button("Giỏ hàng");
        cartBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: bold;");

        bar.getChildren().add(cartBtn);
        return bar;
    }

    private HBox createSelectionBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);

        CheckBox selectAll = new CheckBox("Bỏ chọn tất cả");
        selectAll.setSelected(true);

        Label deleteSelected = new Label("Xóa sản phẩm đã chọn");
        deleteSelected.setTextFill(Color.GRAY);

        bar.getChildren().addAll(selectAll, new Region(), deleteSelected);
        HBox.setHgrow(bar.getChildren().get(1), Priority.ALWAYS);
        return bar;
    }

    private HBox createCartItem() {
        HBox itemBox = new HBox(15);
        itemBox.setPadding(new Insets(15));
        itemBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;");
        itemBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox cb = new CheckBox();
        cb.setSelected(true);

        // Image placeholder
        ImageView iv;
        try {
            iv = new ImageView(new Image(getClass().getResourceAsStream("/images/phone_icon.png")));
        } catch (Exception ex) {
            iv = new ImageView();
            iv.setFitWidth(80);
            iv.setFitHeight(80);
            iv.setStyle("-fx-background-color: #ddd;");
        }
        iv.setFitWidth(80);
        iv.setFitHeight(80);
        iv.setPreserveRatio(true);

        // Details
        VBox details = new VBox(5);
        Label name = new Label("Xiaomi Redmi Pad SE 6GB 128GB - Xám");
        name.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox priceBox = new HBox(5);
        Label current = new Label("4.350.000đ"); current.setTextFill(Color.RED);
        Label original = new Label("5.490.000đ"); original.setStyle("-fx-strikethrough: true; -fx-text-fill: gray;");
        priceBox.getChildren().addAll(current, original);

        details.getChildren().addAll(name, priceBox);

        // Quantity controls
        HBox qtyBox = new HBox(5);
        Button minus = new Button("-");
        Label qty = new Label("1");
        Button plus = new Button("+");
        qtyBox.getChildren().addAll(minus, qty, plus);
        qtyBox.setAlignment(Pos.CENTER);

        // Delete icon
        Button del = new Button("🗑");
        del.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        itemBox.getChildren().addAll(cb, iv, details, spacer, qtyBox, del);
        return itemBox;
    }

    private HBox createProtectionBar() {
        HBox bar = new HBox(5);
        bar.setPadding(new Insets(5, 0, 0, 15));
        bar.setAlignment(Pos.CENTER_LEFT);

        Label shield = new Label("🛡");
        Label text = new Label("Bảo vệ toàn diện với Bảo hành mở rộng");
        Button choose = new Button("chọn gói >");
        choose.setTextFill(Color.RED);

        bar.getChildren().addAll(shield, text, new Region(), choose);
        HBox.setHgrow(bar.getChildren().get(2), Priority.ALWAYS);
        return bar;
    }

    private VBox createRecommendationBar() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15, 0, 0, 0));
        Label title = new Label("Mua kèm tiết kiệm hơn");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox recs = new HBox(10);
        recs.setPadding(new Insets(10));
        recs.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;");

        for (String t : new String[]{"Mua kèm tai nghe QCY", "Mua kèm sim giảm thêm 50K", "Sạc Xiaomi 67W"}) {
            VBox card = new VBox(5);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(100);
            ImageView iv = new ImageView(); iv.setFitWidth(60); iv.setFitHeight(60);
            Label lbl = new Label(t);
            lbl.setWrapText(true);
            card.getChildren().addAll(iv, lbl);
            recs.getChildren().add(card);
        }

        ScrollPane sp = new ScrollPane(recs);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        box.getChildren().addAll(title, sp);
        return box;
    }

    private HBox createSummaryBar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(15));
        bar.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(2);
        Label sum = new Label("Tạm tính: 4.350.000đ"); sum.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label note = new Label("Chưa gồm chiết khấu SMember"); note.setTextFill(Color.GRAY);
        left.getChildren().addAll(sum, note);

        Button buy = new Button("Mua ngay (1)");
        buy.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(left, spacer, buy);
        return bar;
    }
}