package scraper.factory;

import scraper.model.Laptop;
import scraper.model.Smartphone;

import java.util.Map;

public class SmartphoneFactory implements ProductFactory {
    @Override
    public Laptop createProduct(String name, String productUrl, String imageUrl, double price, String priceCurrency,
                                String description, Map<String, String> specifications, double overallRating,
                                int reviewCount, Map<String, Object> categoryData) {
        return new Smartphone(name, productUrl, imageUrl, price, priceCurrency, description, specifications,
                overallRating, reviewCount, categoryData);
    }
}