package com.product.search;

import com.product.model.Product;

import java.util.ArrayList;
import java.util.List;

public class BasicSearchEngine implements SearchEngine {
    @Override
    public List<Product> search(String query, List<Product> products) {
        List<Product> results = new ArrayList<>();
        String[] keywords = query.toLowerCase().split(" ");
        for (Product p : products) {
            boolean match = false;
            for (String keyword : keywords) {
                if (p.getDescription().toLowerCase().contains(keyword) || 
                    p.getName().toLowerCase().contains(keyword)) {
                    match = true;
                }
            }
            if (match && (query.contains("dưới") ? p.getPrice() < 20000000 : true)) {
                results.add(p);
            }
        }
        return results;
    }
}
