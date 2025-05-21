package scraper.factory;

import java.util.Map;
import scraper.model.AbstractProduct;
import scraper.model.Laptop;

/**
 * Factory for creating Laptop objects
 */
public class LaptopFactory implements ProductFactory {

    @Override
    public AbstractProduct createProduct(String name, String productUrl, String imageUrl, double price) {
        Laptop laptop = new Laptop();
        laptop.setName(name);
        laptop.setProductUrl(productUrl);
        laptop.setImageUrl(imageUrl);
        laptop.setPrice(price);
        laptop.setPriceCurrency("VND"); // Default currency for Vietnamese sites
        return laptop;
    }

    @Override
    public AbstractProduct createProductWithDetails(String name, String productUrl, String imageUrl,
                                                    double price, String description,
                                                    double overallRating, int reviewCount) {
        Laptop laptop = new Laptop();
        laptop.setName(name);
        laptop.setProductUrl(productUrl);
        laptop.setImageUrl(imageUrl);
        laptop.setPrice(price);
        laptop.setPriceCurrency("VND");
        laptop.setDescription(description);
        laptop.setOverallRating(overallRating);
        laptop.setReviewCount(reviewCount);
        return laptop;
    }

    /**
     * Creates a laptop with full specifications
     *
     * @param name Product name
     * @param productUrl URL of the product page
     * @param imageUrl URL of the product image
     * @param price Price of the product
     * @param description Product description
     * @param specifications Map of specifications
     * @param overallRating Overall rating of the product
     * @param reviewCount Number of reviews
     * @return A fully populated laptop instance
     */
    public Laptop createLaptopWithSpecs(String name, String productUrl, String imageUrl,
                                        double price, String description,
                                        Map<String, String> specifications,
                                        double overallRating, int reviewCount) {
        Laptop laptop = new Laptop();
        laptop.setName(name);
        laptop.setProductUrl(productUrl);
        laptop.setImageUrl(imageUrl);
        laptop.setPrice(price);
        laptop.setPriceCurrency("VND");
        laptop.setDescription(description);
        laptop.setSpecifications(specifications);
        laptop.setOverallRating(overallRating);
        laptop.setReviewCount(reviewCount);

        // Extract laptop-specific attributes from specifications
        extractLaptopDetails(laptop, specifications);

        return laptop;
    }

    /**
     * Extract laptop-specific details from specifications map
     *
     * @param laptop The laptop object to populate
     * @param specifications Map of specifications
     */
    private void extractLaptopDetails(Laptop laptop, Map<String, String> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return;
        }

        // Extract key laptop specifications
        for (Map.Entry<String, String> entry : specifications.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();

            if (key.contains("processor") || key.contains("cpu") || key.contains("chip")) {
                laptop.setProcessor(value);
            } else if (key.contains("ram")) {
                laptop.setRam(value);
            } else if (key.contains("storage") || key.contains("ssd") || key.contains("hdd")) {
                laptop.setStorage(value);
            } else if (key.contains("screen size") || key.contains("display size")) {
                laptop.setScreenSize(value);
            } else if (key.contains("resolution")) {
                laptop.setScreenResolution(value);
            } else if (key.contains("graphic") || key.contains("gpu") || key.contains("vga")) {
                laptop.setGraphicsCard(value);
            } else if (key.contains("battery")) {
                laptop.setBatteryLife(value);
            } else if (key.contains("os") || key.contains("operating system")) {
                laptop.setOperatingSystem(value);
            } else if (key.contains("weight")) {
                laptop.setWeight(value);
            }
        }

        // Organize specifications into categories
        laptop.organizeSpecificationsIntoCategories();
    }
}