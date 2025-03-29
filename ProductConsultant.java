package com.product.main;

import com.product.model.Product;
import com.product.search.SearchEngine;

import java.util.ArrayList;
import java.util.List;

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
        for (SearchEngine engine : searchEngines) {
            List<Product> engineResults = engine.search(userQuery, products);
            results.addAll(engineResults);
        }
        return results;
    }
}