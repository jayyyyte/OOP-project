package scraper.factory;

import scraper.model.AbstractProduct;

import java.util.Map;

// Base factory interface for creating product objects
public interface ProductFactory {
    //Creates a product with basic information
    AbstractProduct createProduct(String name, String productUrl, String imageUrl, double price);

    //Creates a product with all details
    AbstractProduct createProductWithDetails(String name, String productUrl, String imageUrl,
                                             double price, String description,
                                             double overallRating, int reviewCount);

    AbstractProduct createProduct(String name, String productUrl, String imageUrl, double price, String priceCurrency,
                             String description, Map<String, String> specifications, double overallRating,
                             int reviewCount, Map<String, Object> categoryData);
}