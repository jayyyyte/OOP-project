package com.product.search;

import com.product.model.Product;

import java.util.ArrayList;
import java.util.List;

public class RAGSearchEngine implements SearchEngine {
    private BasicSearchEngine basicEngine = new BasicSearchEngine();

    @Override
    public List<Product> search(String query, List<Product> products) {
        // Retrieval: Loc co ban
        List<Product> filtered = basicEngine.search(query, products);
        // Augmentation + Generation (giả lập LLM)
        if (!filtered.isEmpty()) {
            System.out.println("LLM suggests: " + filtered.get(0).getName() + " based on your query: " + query);
        }
        return filtered;
    }
}
