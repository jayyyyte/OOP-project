package com.product.collector;

import com.product.model.Product;
import com.product.model.Review;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

public class DataCollector {
    public List<Product> collectSampleData() {
        List<Product> products = new ArrayList<>();
        // Dữ liệu mẫu (giả lập crawl)
        Product p1 = new Product(1, "iPhone 14", 20000000, "Điện thoại cao cấp, camera 12MP");
        p1.addStructuredData("camera", "12MP");
        p1.addStructuredData("ram", "6GB");
        p1.addReview(new Review("user1", 5, "Tuyệt vời!"));

        Product p2 = new Product(2, "Samsung S23", 18000000, "Camera 50MP, pin 4000mAh");
        p2.addStructuredData("camera", "50MP");
        p2.addStructuredData("ram", "8GB");

        products.add(p1);
        products.add(p2);
        return products;
    }

    // Crawl thực tế (ví dụ)
    public String collectFromWeb(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.select(".product-description").text();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error collecting data";
        }
    }
}
// change