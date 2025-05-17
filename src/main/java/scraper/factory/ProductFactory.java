package scraper.factory;

import scraper.model.Laptop;

import java.util.Map;

public interface ProductFactory {
    Laptop createProduct(String name, String productUrl, String imageUrl, double price, String priceCurrency,
                         String description, Map<String, String> specifications, double overallRating,
                         int reviewCount, Map<String, Object> categoryData);
}