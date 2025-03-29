package com.product.search;

import com.product.model.Product;

import java.util.List;

public interface SearchEngine {
    List<Product> search(String query, List<Product> products);
}