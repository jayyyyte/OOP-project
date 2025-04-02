package main.crawler;

import java.util.List;

public class Phone {
    private String name;
    private double price;
    private String imageUrl;
    private String productUrl;
    private List<String> specifications;
    
    public Phone(String name, double price, String imageUrl, String productUrl, List<String> specifications) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
        this.specifications = specifications;
    }
    
    public String getName() {
        return name;
    }
    
    public double getPrice() {
        return price;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public String getProductUrl() {
        return productUrl;
    }
    
    public List<String> getSpecifications() {
        return specifications;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Phone: ").append(name).append("\n");
        sb.append("Price: ").append(price).append(" VND\n");
        sb.append("URL: ").append(productUrl).append("\n");
        sb.append("Image: ").append(imageUrl).append("\n");
        sb.append("Specifications:\n");
        
        for (String spec : specifications) {
            sb.append("- ").append(spec).append("\n");
        }
        
        return sb.toString();
    }
}