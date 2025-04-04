import java.util.List;
import java.util.Map;

public class Product {
    private String url;
    private String name;
    private String price; // Store as String initially, can be parsed later
    private String shortDescription;
    private Map<String, String> specifications; // e.g., {"Screen": "6.1 inch, OLED", "RAM": "6 GB"}
    private List<String> reviews; // Simple list of review texts for now
    private double rating; // Average rating if available

    // --- Getters and Setters ---
    // (Generate using your IDE: Alt+Insert in IntelliJ, Source -> Generate Getters and Setters in Eclipse)

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }
    public List<String> getReviews() { return reviews; }
    public void setReviews(List<String> reviews) { this.reviews = reviews; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    // --- toString() for debugging ---
    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}