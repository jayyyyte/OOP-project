package service;

import model.Product;
import search.SearchEngine;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ProductConsultant {
    private List<Product> products;
    private List<SearchEngine> searchEngines;

    public ProductConsultant(List<Product> products) {
        this.products = products;
        this.searchEngines = new ArrayList<>();
    }

    public void addSearchEngine(SearchEngine engine) {
        searchEngines.add(engine);
    }

    public List<Product> consult(String userQuery) {
        List<Product> results = new ArrayList<>();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("keyword", userQuery);
        
        for (SearchEngine engine : searchEngines) {
            List<JSONObject> engineResults = engine.search(criteria);
            // Convert JSONObject results to Product objects
            for (JSONObject jsonProduct : engineResults) {
                // Assuming you have a method to convert JSONObject to Product
                Product product = convertToProduct(jsonProduct);
                if (product != null) {
                    results.add(product);
                }
            }
        }
        return results;
    }

    private Product convertToProduct(JSONObject jsonProduct) {
        try {
            // Generate a unique ID if not present in JSON
            int id = jsonProduct.optInt("id", (int) (Math.random() * 1000000));
            String name = jsonProduct.getString("name");
            double price = jsonProduct.optDouble("price", 0.0);
            String description = jsonProduct.optString("description", "");
            
            return new Product(id, name, price, description);
        } catch (Exception e) {
            System.err.println("Error converting JSON to Product: " + e.getMessage());
            return null;
        }
    }
}