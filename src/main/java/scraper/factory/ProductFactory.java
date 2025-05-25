package scraper.factory;

import scraper.model.AbstractProduct;

import java.util.Map;

/**
 * Base factory interface for creating product objects
 */
public interface ProductFactory {

    /**
     * Creates a product with basic information
     *
     * @param name Product name
     * @param productUrl URL of the product page
     * @param imageUrl URL of the product image
     * @param price Price of the product
     * @return A new product instance
     */
    AbstractProduct createProduct(String name, String productUrl, String imageUrl, double price);

    /**
     * Creates a product with all details
     *
     * @param name Product name
     * @param productUrl URL of the product page
     * @param imageUrl URL of the product image
     * @param price Price of the product
     * @param description Product description
     * @param overallRating Overall rating of the product
     * @param reviewCount Number of reviews
     * @return A fully populated product instance
     */
    AbstractProduct createProductWithDetails(String name, String productUrl, String imageUrl,
                                             double price, String description,
                                             double overallRating, int reviewCount);

    AbstractProduct createProduct(String name, String productUrl, String imageUrl, double price, String priceCurrency,
                             String description, Map<String, String> specifications, double overallRating,
                             int reviewCount, Map<String, Object> categoryData);
}