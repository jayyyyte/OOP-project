import java.util.List;
import java.util.Map;

// Lombok can simplify this with @Data or @Getter/@Setter
public class Product {
    private String name;
    private String productUrl; // URL of the product detail page
    private String imageUrl;
    private double price; // Store as double or BigDecimal
    private String priceCurrency = "VND"; // Assuming VND
    private String brand; // May need to extract/derive
    private String description; // From detail page
    private Map<String, String> specifications; // Key-value pairs from detail page
    private double overallRating; // Average rating (e.g., 4.5)
    private int reviewCount;
    private List<String> reviews; // List of individual review texts (optional)

    // --- Constructors ---
    public Product() {
        // Default constructor
    }

    // --- Getters and Setters (Essential) ---
    // (Generate using your IDE or write them manually)

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
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }
    public double getOverallRating() { return overallRating; }
    public void setOverallRating(double overallRating) { this.overallRating = overallRating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public List<String> getReviews() { return reviews; }
    public void setReviews(List<String> reviews) { this.reviews = reviews; }


    // --- Optional: toString() for easy printing ---
    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", productUrl='" + productUrl + '\'' +
                // Add other fields as needed
                '}';
    }

    // --- Helper method for cleaning/parsing price ---
    public static double parsePrice(String priceString) {
        if (priceString == null || priceString.isEmpty()) {
            return 0.0;
        }
        try {
            // Remove currency symbols (₫, VND), dots (as thousands separators), spaces
            String cleanedPrice = priceString
                    .replaceAll("[^\\d]", ""); // Keep only digits
            if (cleanedPrice.isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(cleanedPrice);
        } catch (NumberFormatException e) {
            System.err.println("Could not parse price string: " + priceString);
            return 0.0; // Or throw an exception
        }
    }
}