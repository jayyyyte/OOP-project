package filter;

import org.json.JSONArray;
import org.json.JSONObject;
import search.SearchEngine;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Engine for filtering products based on their brand, by checking if the
 * product's "name" field contains the brand name.
 * Assumes product data is loaded from a JSON data source.
 */
public class BrandFilterEngine extends SearchEngine {

    private List<JSONObject> products;

    /**
     * Constructs a BrandFilterEngine by loading product data from the specified source.
     *
     * @param dataSource The path to the JSON data file (e.g., "products.json").
     * @throws IOException If an I/O error occurs while reading the data source.
     */
    public BrandFilterEngine(String dataSource) throws IOException {
        super(dataSource);
        this.products = loadProducts();
    }

    /**
     * Loads product data from the JSON data source.
     * (This method remains the same as loading all products is the first step)
     *
     * @param dataSource The path to the JSON data file.
     * @throws IOException If an I/O error occurs.
     */
    private List<JSONObject> loadProducts() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(dataSource)) {
            if (is == null) {
                throw new IOException("Could not find resource: " + dataSource);
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            List<JSONObject> productList = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(content);
            for (int i = 0; i < jsonArray.length(); i++) {
                productList.add(jsonArray.getJSONObject(i));
            }
            return productList;
        } catch (Exception e) {
            throw new IOException("Error loading data from " + dataSource + ": " + e.getMessage(), e);
        }
    }

    /**
     * Searches and filters products based on the specified brand criteria
     * by checking if the product's "name" field contains the brand name.
     * Expected criteria map: {"brand": "TargetBrandName"}
     *
     * @param criteria A map containing the search criteria. Expected key is "brand".
     * @return A list of JSONObject representing products whose name contains the brand,
     * or an empty list if no brand criteria is provided or no products match.
     */
    @Override
    public List<JSONObject> search(Map<String, Object> criteria) {
        List<JSONObject> results = new ArrayList<>();
        String brand = (String) criteria.get("brand");
        if (brand == null || brand.isEmpty()) {
            return results;
        }

        String brandLower = brand.toLowerCase();
        for (JSONObject product : products) {
            // Check if product has a brand in categoryData
            if (product.has("categoryData")) {
                JSONObject categoryData = product.getJSONObject("categoryData");
                if (categoryData.has("brand")) {
                    String productBrand = categoryData.getString("brand").toLowerCase();
                    if (productBrand.contains(brandLower)) {
                        results.add(product);
                        continue;
                    }
                }
            }

            // Check if brand is in product name
            if (product.has("name")) {
                String productName = product.getString("name").toLowerCase();
                if (productName.contains(brandLower)) {
                    results.add(product);
                }
            }
        }
        return results;
    }

    // You might want to add a simple printResults method here if needed.
    // public void printResults(List<JSONObject> results) { ... }
}