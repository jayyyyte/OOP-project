package scraper.factory;

import java.util.Map;
import scraper.model.AbstractProduct;
import scraper.model.Smartphone;

/**
 * Factory for creating Smartphone objects
 */
public class SmartphoneFactory implements ProductFactory {

    @Override
    public AbstractProduct createProduct(String name, String productUrl, String imageUrl, double price) {
        Smartphone smartphone = new Smartphone();
        smartphone.setName(name);
        smartphone.setProductUrl(productUrl);
        smartphone.setImageUrl(imageUrl);
        smartphone.setPrice(price);
        smartphone.setPriceCurrency("VND"); // Default currency for Vietnamese sites
        return smartphone;
    }

    @Override
    public AbstractProduct createProductWithDetails(String name, String productUrl, String imageUrl,
                                                    double price, String description,
                                                    double overallRating, int reviewCount) {
        Smartphone smartphone = new Smartphone();
        smartphone.setName(name);
        smartphone.setProductUrl(productUrl);
        smartphone.setImageUrl(imageUrl);
        smartphone.setPrice(price);
        smartphone.setPriceCurrency("VND");
        smartphone.setDescription(description);
        smartphone.setOverallRating(overallRating);
        smartphone.setReviewCount(reviewCount);
        return smartphone;
    }

    /**
     * Creates a smartphone with full specifications
     *
     * @param name Product name
     * @param productUrl URL of the product page
     * @param imageUrl URL of the product image
     * @param price Price of the product
     * @param description Product description
     * @param specifications Map of specifications
     * @param overallRating Overall rating of the product
     * @param reviewCount Number of reviews
     * @return A fully populated smartphone instance
     */
    public Smartphone createSmartphoneWithSpecs(String name, String productUrl, String imageUrl,
                                                double price, String description,
                                                Map<String, String> specifications,
                                                double overallRating, int reviewCount) {
        Smartphone smartphone = new Smartphone();
        smartphone.setName(name);
        smartphone.setProductUrl(productUrl);
        smartphone.setImageUrl(imageUrl);
        smartphone.setPrice(price);
        smartphone.setPriceCurrency("VND");
        smartphone.setDescription(description);
        smartphone.setSpecifications(specifications);
        smartphone.setOverallRating(overallRating);
        smartphone.setReviewCount(reviewCount);

        // Extract smartphone-specific attributes from specifications
        extractSmartphoneDetails(smartphone, specifications);

        return smartphone;
    }

    /**
     * Extract smartphone-specific details from specifications map
     *
     * @param smartphone The smartphone object to populate
     * @param specifications Map of specifications
     */
    private void extractSmartphoneDetails(Smartphone smartphone, Map<String, String> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return;
        }

        // Extract key smartphone specifications
        for (Map.Entry<String, String> entry : specifications.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();

            if (key.contains("processor") || key.contains("cpu") || key.contains("chip")) {
                smartphone.setProcessor(value);
            } else if (key.contains("ram")) {
                smartphone.setRam(value);
            } else if (key.contains("storage") || key.contains("memory") && !key.contains("ram")) {
                smartphone.setStorage(value);
            } else if (key.contains("screen size") || key.contains("display size")) {
                smartphone.setScreenSize(value);
            } else if (key.contains("camera") && !key.contains("selfie") && !key.contains("front")) {
                smartphone.setRearCamera(value);
            } else if (key.contains("selfie") || (key.contains("camera") && key.contains("front"))) {
                smartphone.setFrontCamera(value);
            } else if (key.contains("battery")) {
                smartphone.setBatteryCapacity(value);
            } else if (key.contains("os") || key.contains("operating system")) {
                smartphone.setOperatingSystem(value);
            }
        }

        // Organize specifications into categories
        smartphone.organizeSpecificationsIntoCategories();
    }
}