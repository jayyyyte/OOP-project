package main.crawler;

public class Phone {
    private String name;
    private String price;
    private String originalPrice;
    private String discount;
    private String description;
    private String specifications;
    private String imageUrl;
    private String productUrl;
    private String rating;
    private String reviewCount;

    // Constructors
    public Phone() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(String originalPrice) { this.originalPrice = originalPrice; }
    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public String getReviewCount() { return reviewCount; }
    public void setReviewCount(String reviewCount) { this.reviewCount = reviewCount; }

    @Override
    public String toString() {
        return "Phone{" +
                "name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", originalPrice='" + originalPrice + '\'' +
                ", discount='" + discount + '\'' +
                '}';
    }
}