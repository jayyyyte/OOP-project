package scraper.model;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProduct {
    protected String category;
    protected String name;
    protected String productUrl;
    protected String imageUrl;
    protected double price;
    protected String priceCurrency;
    protected String description;
    protected Map<String, String> specifications;
    protected double overallRating;
    protected int reviewCount;
    protected Map<String, Object> categoryData;     // For more organized hierarchical data

    protected AbstractProduct(String category) {
        this.category = category;
        this.specifications = new HashMap<>();
        this.categoryData = new HashMap<>();
        this.priceCurrency = "VND";
    }

    // Default constructo
    public AbstractProduct() {
        this.specifications = new HashMap<>();
        this.categoryData = new HashMap<>();
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getPriceCurrency() { return priceCurrency; }
    public void setPriceCurrency(String priceCurrency) { this.priceCurrency = priceCurrency; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }
    public double getOverallRating() { return overallRating; }
    public void setOverallRating(double overallRating) { this.overallRating = overallRating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public Map<String, Object> getCategoryData() { return categoryData; }
    public void setCategoryData(Map<String, Object> categoryData) { this.categoryData = categoryData; }
    public void addCategoryData(String category, Object value) { this.categoryData.put(category, value); }

    public abstract void organizeSpecificationsIntoCategories();

    public static double parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return 0.0;
        }

        // Remove all non-numeric characters except decimal point
        String cleanPrice = priceText.replaceAll("[^\\d.,]", "");

        // Replace comma with dot if used as decimal separator
        cleanPrice = cleanPrice.replace(',', '.');

        // If multiple dots remain, keep only the last one (assuming it's the decimal separator)
        int lastDotIndex = cleanPrice.lastIndexOf('.');
        if (lastDotIndex > -1 && cleanPrice.indexOf('.') != lastDotIndex) {
            StringBuilder sb = new StringBuilder(cleanPrice);
            for (int i = 0; i < lastDotIndex; i++) {
                if (sb.charAt(i) == '.') {
                    sb.setCharAt(i, '\0'); // Replace with a null character to be removed
                }
            }
            cleanPrice = sb.toString().replace("\0", "");
        }

        try {
            return Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            System.err.println("Could not parse price: " + priceText);
            return 0.0;
        }
    }

    @Override
    public String toString() {
        return String.format("%s{name='%s', price=%.2f %s, rating=%.1f, reviews=%d}",
                category, name, price, priceCurrency, overallRating, reviewCount);
    }
}