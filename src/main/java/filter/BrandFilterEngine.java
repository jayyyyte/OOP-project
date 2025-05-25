package filter; // Make sure this matches thư mục của bạn

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Engine for filtering products based on their brand, by checking if the
 * product's "name" field contains the brand name.
 * Assumes product data is loaded from a JSON data source in classpath.
 */
public class BrandFilterEngine {

    private final List<JSONObject> allProducts = new ArrayList<>();

    /**
     * Constructs a BrandFilterEngine by loading product data from the specified source.
     *
     * @param dataSource The JSON file name inside src/main/resources (e.g. "smartphones.json").
     * @throws IOException   If I/O error occurs while reading the data.
     * @throws JSONException If the JSON is invalid.
     */
    public BrandFilterEngine(String dataSource) throws IOException, JSONException {
        loadProducts(dataSource);
    }

    private void loadProducts(String dataSource) throws IOException, JSONException {
        // Load as resource from classpath
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(dataSource)) {
            if (is == null) {
                throw new IOException("Không tìm thấy file trong resources: " + dataSource);
            }
            // Parse JSON
            JSONTokener tokener = new JSONTokener(is);
            Object root = tokener.nextValue();

            if (!(root instanceof JSONArray)) {
                throw new JSONException("Root element must be a JSONArray in " + dataSource);
            }
            JSONArray jsonArray = (JSONArray) root;
            for (int i = 0; i < jsonArray.length(); i++) {
                Object item = jsonArray.get(i);
                if (item instanceof JSONObject) {
                    allProducts.add((JSONObject) item);
                } else {
                    System.err.println("Skipping non-JSONObject at index " + i + " in " + dataSource);
                }
            }
        }
    }

    /**
     * Filters products whose "name" contains the given brand (case-insensitive).
     *
     * @param criteria Map với key "brand" và value là tên hãng cần tìm.
     * @return Danh sách JSONObject thoả mãn, hoặc rỗng nếu không có criteria hoặc không tìm thấy.
     */
    public List<JSONObject> search(Map<String, Object> criteria) {
        List<JSONObject> filtered = new ArrayList<>();

        Object brandValue = criteria.get("brand");
        if (!(brandValue instanceof String)) {
            System.err.println("BrandFilterEngine: Missing or invalid 'brand' criteria.");
            return filtered;
        }
        String target = ((String) brandValue).toLowerCase(Locale.ENGLISH);

        for (JSONObject product : allProducts) {
            if (product.has("name")) {
                String name = product.optString("name", "");
                if (name.toLowerCase(Locale.ENGLISH).contains(target)) {
                    filtered.add(product);
                }
            }
        }
        return filtered;
    }
}
