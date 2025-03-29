package com.product.main;

import com.product.collector.DataCollector;
import com.product.model.Product;
import com.product.search.BasicSearchEngine;
import com.product.search.RAGSearchEngine;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {
    private ProductConsultant consultant;

    @Override
    public void start(Stage primaryStage) {
        // Khởi tạo backend
        DataCollector collector = new DataCollector();
        List<Product> products = collector.collectSampleData(); // Hoặc collectFromApi()
        consultant = new ProductConsultant(products);
        consultant.addSearchEngine(new BasicSearchEngine());
        consultant.addSearchEngine(new RAGSearchEngine());

        // Thiết kế UI
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Label hướng dẫn
        Label label = new Label("Nhập yêu cầu về điện thoại (VD: 'camera tốt dưới 20 triệu'):");
        
        // TextField để nhập yêu cầu
        TextField queryField = new TextField();
        queryField.setPromptText("Nhập yêu cầu...");

        // Button tìm kiếm
        Button searchButton = new Button("Tìm kiếm");

        // TextArea hiển thị kết quả
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(300);

        // Sự kiện khi nhấn nút Tìm kiếm
        searchButton.setOnAction(event -> {
            String query = queryField.getText().trim();
            if (!query.isEmpty()) {
                List<Product> results = consultant.consult(query);
                displayResults(results, resultArea);
            } else {
                resultArea.setText("Vui lòng nhập yêu cầu!");
            }
        });

        // Thêm các thành phần vào layout
        root.getChildren().addAll(label, queryField, searchButton, resultArea);

        // Tạo Scene và hiển thị
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Tư vấn sản phẩm");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void displayResults(List<Product> results, TextArea resultArea) {
        if (results.isEmpty()) {
            resultArea.setText("Không tìm thấy sản phẩm phù hợp.");
        } else {
            StringBuilder sb = new StringBuilder("Danh sách sản phẩm gợi ý:\n");
            for (Product p : results) {
                sb.append(p.toString()).append("\n")
                  .append("Thông số: ").append(p.getStructuredData()).append("\n")
                  .append("Đánh giá: ").append(p.getReviews()).append("\n\n");
            }
            resultArea.setText(sb.toString());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}