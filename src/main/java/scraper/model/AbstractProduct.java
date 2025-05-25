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
    protected Map<String, Object> categoryData;

    protected AbstractProduct(String category) {
        this.category = category;
        this.specifications = new HashMap<>();
        this.categoryData = new HashMap<>();
        this.priceCurrency = "VND";
    }

    // Getters and setters (same as Product.java, omitted for brevity)
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

    public void organizeSpecificationsIntoCategories() {
        // Same as Product.java
        Map<String, Object> techSpecs = new HashMap<>();
        Map<String, Object> displayInfo = new HashMap<>();
        Map<String, Object> cameraInfo = new HashMap<>();
        Map<String, Object> batteryInfo = new HashMap<>();
        Map<String, Object> connectivityInfo = new HashMap<>();

        for (Map.Entry<String, String> entry : specifications.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();
            if (key.contains("processor") || key.contains("cpu") || key.contains("ram") || key.contains("storage") ||
                    key.contains("memory") || key.contains("os") || key.contains("chip")) {
                techSpecs.put(entry.getKey(), value);
            } else if (key.contains("display") || key.contains("screen") || key.contains("resolution")) {
                displayInfo.put(entry.getKey(), value);
            } else if (key.contains("camera") || key.contains("selfie") || key.contains("video")) {
                cameraInfo.put(entry.getKey(), value);
            } else if (key.contains("battery") || key.contains("charging")) {
                batteryInfo.put(entry.getKey(), value);
            } else if (key.contains("wifi") || key.contains("bluetooth") || key.contains("5g") ||
                    key.contains("nfc") || key.contains("usb") || key.contains("port")) {
                connectivityInfo.put(entry.getKey(), value);
            }
        }

        if (!techSpecs.isEmpty()) categoryData.put("techSpecs", techSpecs);
        if (!displayInfo.isEmpty()) categoryData.put("displayInfo", displayInfo);
        if (!cameraInfo.isEmpty()) categoryData.put("cameraInfo", cameraInfo);
        if (!batteryInfo.isEmpty()) categoryData.put("batteryInfo", batteryInfo);
        if (!connectivityInfo.isEmpty()) categoryData.put("connectivityInfo", connectivityInfo);

        if (name != null && !name.isEmpty()) {
            String lowerName = name.toLowerCase();
            if (lowerName.contains("samsung")) categoryData.put("brand", "Samsung");
            else if (lowerName.contains("apple") || lowerName.contains("iphone")) categoryData.put("brand", "Apple");
            else if (lowerName.contains("xiaomi")) categoryData.put("brand", "Xiaomi");
            else if (lowerName.contains("oppo")) categoryData.put("brand", "Oppo");
            else if (lowerName.contains("vivo")) categoryData.put("brand", "Vivo");
            else if (lowerName.contains("nokia")) categoryData.put("brand", "Nokia");
            else if (lowerName.contains("sony")) categoryData.put("brand", "Sony");
            else if (lowerName.contains("google") || lowerName.contains("pixel")) categoryData.put("brand", "Google");
        }
    }

    public static double parsePrice(String priceText) {
        return Product.parsePrice(priceText);
    }

    @Override
    public String toString() {
        return String.format("%s{name='%s', price=%.2f %s, rating=%.1f, reviews=%d}",
                category, name, price, priceCurrency, overallRating, reviewCount);
    }
}