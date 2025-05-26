package search;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// Abstract class định nghĩa cấu trúc chung cho các engine tìm kiếm
public abstract class SearchEngine {

    protected List<JSONObject> data;
    protected String dataSource;

    public SearchEngine(String dataSource) throws IOException {
        this.dataSource = dataSource;
        this.data = loadData(dataSource);
    }

    protected List<JSONObject> loadData(String dataSource) throws IOException {
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

    public abstract List<JSONObject> search(Map<String, Object> criteria);

    protected Double getPrice(JSONObject product) {
        if (product.has("price") && !product.isNull("price")) {
            return product.getDouble("price");
        }
        return null;
    }
}