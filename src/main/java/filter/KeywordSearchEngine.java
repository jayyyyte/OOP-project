package filter;

import org.json.JSONObject;
import search.SearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeywordSearchEngine extends SearchEngine {

    public static final String CRITERIA_KEYWORD = "keyword";

    public KeywordSearchEngine(String dataSource) throws IOException {
        super(dataSource);
    }

    @Override
    public List<JSONObject> search(Map<String, Object> criteria) {
        // Nếu không có criteria hoặc không chứa 'keyword' => trả về toàn bộ data
        if (criteria == null || !criteria.containsKey(CRITERIA_KEYWORD)) {
            return data;
        }

        String keyword = ((String) criteria.get(CRITERIA_KEYWORD)).toLowerCase();
        String[] words = keyword.split("\\s+");

        List<JSONObject> results = new ArrayList<>();
        for (JSONObject product : data) {
            boolean allMatch = true;

            for (String w : words) {
                String low = w.trim();
                boolean found = false;

                if (product.optString("name", "").toLowerCase().contains(low)) {
                    found = true;
                } else if (product.optString("description", "").toLowerCase().contains(low)) {
                    found = true;
                }

                if (!found) {
                    allMatch = false;
                    break;
                }
            }

            if (allMatch) {
                results.add(product);
            }
        }
        return results;
    }
}