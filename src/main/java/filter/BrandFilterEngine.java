package filter; // Make sure this package matches your project structure

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException; // Import JSONException explicitly

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale; // For case-insensitive comparison

/**
 * Engine for filtering products based on their brand, by checking if the
 * product's "name" field contains the brand name.
 * Assumes product data is loaded from a JSON data source.
 */
public class BrandFilterEngine {

    private List<JSONObject> allProducts; // Store all loaded products

    /**
     * Constructs a BrandFilterEngine by loading product data from the specified source.
     *
     * @param dataSource The path to the JSON data file (e.g., "products.json").
     * @throws IOException If an I/O error occurs while reading the data source.
     * @throws JSONException If the data source is not valid JSON.
     */
    public BrandFilterEngine(String dataSource) throws IOException, JSONException {
        loadProducts(dataSource);
    }

    /**
     * Loads product data from the JSON data source.
     * (This method remains the same as loading all products is the first step)
     *
     * @param dataSource The path to the JSON data file.
     * @throws IOException If an I/O error occurs.
     * @throws JSONException If the JSON is invalid or has an unexpected root structure.
     */
    private void loadProducts(String dataSource) throws IOException, JSONException {
        allProducts = new ArrayList<>();
        try (InputStream is = Files.newInputStream(Paths.get(dataSource))) {
            // Assuming the JSON file contains a root JSONArray
            JSONTokener tokener = new JSONTokener(is);
            Object root = tokener.nextValue();

            if (root instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) root;
                for (int i = 0; i < jsonArray.length(); i++) {
                     try {
                         Object item = jsonArray.get(i);
                         if (item instanceof JSONObject) {
                             allProducts.add((JSONObject) item);
                         } else {
                              System.err.println("Skipping non-JSONObject item at index " + i + " in " + dataSource);
                         }
                     } catch (JSONException e) {
                         System.err.println("Error processing item at index " + i + " in " + dataSource + ": " + e.getMessage());
                         // Continue loading other items
                     }
                }
            } else {
                 throw new JSONException("Root element in data source is not a JSONArray: " + dataSource);
            }

        } catch (JSONException e) {
             throw new JSONException("Invalid JSON format or structure in data source: " + dataSource, e);
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
    public List<JSONObject> search(Map<String, Object> criteria) {
        List<JSONObject> filteredProducts = new ArrayList<>();

        // Get the target brand from the criteria
        Object brandValue = criteria.get("brand");

        // If no brand criteria is provided or it's not a String, return an empty list
        if (brandValue == null || !(brandValue instanceof String)) {
             System.err.println("BrandFilterEngine: Missing or invalid 'brand' criteria.");
             return filteredProducts; // Return empty list
        }

        // Convert the target brand to lowercase for case-insensitive matching
        String targetBrandLowerCase = ((String) brandValue).toLowerCase(Locale.ENGLISH);

        // Filter the loaded products
        for (JSONObject product : allProducts) {
            try {
                // **MODIFICATION HERE: Access and check the "name" field**
                if (product.has("name") && product.get("name") instanceof String) {
                    String productName = product.getString("name");

                    // Check if the product's name (in lowercase) contains the target brand (in lowercase)
                    if (productName.toLowerCase(Locale.ENGLISH).contains(targetBrandLowerCase)) {
                        filteredProducts.add(product);
                    }
                }
                // Products without a "name" field or with a non-string name are skipped.

            } catch (JSONException e) {
                 // Handle cases where a product is missing the "name" field unexpectedly
                 // (Though the outer has() check should prevent this specific JSONException)
                 System.err.println("Skipping product due to missing or invalid 'name' field: " + e.getMessage());
            }
        }

        return filteredProducts;
    }

    // You might want to add a simple printResults method here if needed.
    // public void printResults(List<JSONObject> results) { ... }
}