package com.product.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {
    private int id;
    private String name;
    private double price;
    private String description;
    private Map<String, String> structuredData; // Dữ liệu cấu trúc: RAM, camera,...
    private List<Review> reviews;

    public Product(int id, String name, double price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.structuredData = new HashMap<>();
        this.reviews = new ArrayList<>();
    }

    // Getters và Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public Map<String, String> getStructuredData() { return structuredData; }
    public List<Review> getReviews() { return reviews; }

    public void addStructuredData(String key, String value) {
        structuredData.put(key, value);
    }

    public void addReview(Review review) {
        reviews.add(review);
    }

    @Override
    public String toString() {
        return "Product [id=" + id + ", name=" + name + ", price=" + price + ", description=" + description + "]";
    }
}