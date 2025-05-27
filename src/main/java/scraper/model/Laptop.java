package scraper.model;

import java.util.Map;

public class Laptop extends AbstractProduct {
    public Laptop(String name, String productUrl, String imageUrl, double price, String priceCurrency,
                  String description, Map<String, String> specifications, double overallRating,
                  int reviewCount, Map<String, Object> categoryData) {
        super("Laptop");
        this.name = name;
        this.productUrl = productUrl;
        this.imageUrl = imageUrl;
        this.price = price;
        this.priceCurrency = priceCurrency;
        this.description = description;
        this.specifications = specifications;
        this.overallRating = overallRating;
        this.reviewCount = reviewCount;
        this.categoryData = categoryData;
    }
}