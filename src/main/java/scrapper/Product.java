package scrapper;

import java.util.HashMap;
import java.util.Map;

public class Product {
    private String name;
    private String productUrl;
    private String imageUrl;
    private double price;
    private String priceCurrency = "VND";
    private String description;
    private Map<String, String> specifications;
    private double overallRating;
    private int reviewCount;
    private Map<String, Object> categoryData; // For more organized hierarchical data

    // Default constructor
    public Product() {
        this.specifications = new HashMap<>();
        this.categoryData = new HashMap<>();
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }

    public void setPriceCurrency(String priceCurrency) {
        this.priceCurrency = priceCurrency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Map<String, String> specifications) {
        this.specifications = specifications;
    }

    public double getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(double overallRating) {
        this.overallRating = overallRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public Map<String, Object> getCategoryData() {
        return categoryData;
    }
    
    public void setCategoryData(Map<String, Object> categoryData) {
        this.categoryData = categoryData;
    }
    
    public void addCategoryData(String category, Object value) {
        this.categoryData.put(category, value);
    }

    // Helper method to parse price strings into doubles
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
    
    // Method to extract category data from specifications
    public void organizeSpecificationsIntoCategories() {
        Map<String, Object> techSpecs = new HashMap<>();
        Map<String, Object> displayInfo = new HashMap<>();
        Map<String, Object> cameraInfo = new HashMap<>();
        Map<String, Object> batteryInfo = new HashMap<>();
        Map<String, Object> connectivityInfo = new HashMap<>();
        
        for (Map.Entry<String, String> entry : specifications.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();
            
            // Sort specifications into categories
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
        
        // Add non-empty categories to categoryData
        if (!techSpecs.isEmpty()) categoryData.put("techSpecs", techSpecs);
        if (!displayInfo.isEmpty()) categoryData.put("displayInfo", displayInfo);
        if (!cameraInfo.isEmpty()) categoryData.put("cameraInfo", cameraInfo);
        if (!batteryInfo.isEmpty()) categoryData.put("batteryInfo", batteryInfo);
        if (!connectivityInfo.isEmpty()) categoryData.put("connectivityInfo", connectivityInfo);
        
        // Extract brand and model for better search organization
        if (name != null && !name.isEmpty()) {
            String lowerName = name.toLowerCase();
            if (lowerName.contains("samsung")) {
                categoryData.put("brand", "Samsung");
            } else if (lowerName.contains("apple") || lowerName.contains("iphone")) {
                categoryData.put("brand", "Apple");
            } else if (lowerName.contains("xiaomi")) {
                categoryData.put("brand", "Xiaomi");
            } else if (lowerName.contains("oppo")) {
                categoryData.put("brand", "Oppo");
            } else if (lowerName.contains("vivo")) {
                categoryData.put("brand", "Vivo");
            } else if (lowerName.contains("nokia")) {
                categoryData.put("brand", "Nokia");
            } else if (lowerName.contains("sony")) {
                categoryData.put("brand", "Sony");
            } else if (lowerName.contains("google") || lowerName.contains("pixel")) {
                categoryData.put("brand", "Google");
            }
            // Add model extraction logic here if needed
        }
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price=" + price + " " + priceCurrency +
                ", rating=" + overallRating +
                ", reviewCount=" + reviewCount +
                '}';
    }
}