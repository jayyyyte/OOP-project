package scraper.factory;

import scraper.model.AbstractProduct;
import scraper.model.Laptop;
import java.util.Map;

public class LaptopFactory implements ProductFactory {
    @Override
    public AbstractProduct createProduct(String name, String productUrl, String imageUrl, double price) {
        return null;
    }

    @Override
    public AbstractProduct createProductWithDetails(String name, String productUrl, String imageUrl, double price, String description, double overallRating, int reviewCount) {
        return null;
    }

    @Override
    public AbstractProduct createProduct(String name, String productUrl, String imageUrl, double price, String priceCurrency,
                                    String description, Map<String, String> specifications, double overallRating,
                                    int reviewCount, Map<String, Object> categoryData) {
        return new Laptop(name, productUrl, imageUrl, price, priceCurrency, description, specifications,
                overallRating, reviewCount, categoryData);
    }
}