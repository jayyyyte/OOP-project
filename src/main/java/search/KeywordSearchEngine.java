package search;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

class KeywordSearchEngine extends SearchEngine {

    public KeywordSearchEngine(String dataSource) throws IOException {
        super(dataSource);
    }

    @Override
    public List<JSONObject> search(Map<String, Object> criteria) {
        List<JSONObject> results = new ArrayList<>();
        if (criteria == null || !criteria.containsKey("keyword")) {
            return data;
        }
        String keyword = ((String) criteria.get("keyword")).toLowerCase();
        String[] keywords = keyword.split("\\s+");

        for (JSONObject product : data) {
            boolean foundAll = true;
            for (String word : keywords) {
                boolean foundInProduct = false;
                if (product.has("name") && product.getString("name").toLowerCase().contains(word)) {
                    foundInProduct = true;
                }
                if (product.has("description") && product.getString("description").toLowerCase().contains(word)) {
                    foundInProduct = true;
                }
                if (!foundInProduct) {
                    foundAll = false;
                    break;
                }
            }
            if (foundAll) {
                results.add(product);
            }
        }
        return results;
    }
}