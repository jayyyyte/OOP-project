package search;

import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class PriceRangeFilterEngine extends SearchEngine {

    public PriceRangeFilterEngine(String dataSource) throws IOException {
        super(dataSource);
    }

    @Override
    public List<JSONObject> search(Map<String, Object> criteria) {
        List<JSONObject> results = new ArrayList<>(data); // Bắt đầu với tất cả sản phẩm
        if (criteria != null && criteria.containsKey("minPrice") && criteria.containsKey("maxPrice")) {
            double minPrice = (double) criteria.get("minPrice");
            double maxPrice = (double) criteria.get("maxPrice");
            List<JSONObject> filteredResults = new ArrayList<>();
            for (JSONObject product : results) {
                Double price = getPrice(product);
                if (price != null && price >= minPrice && price <= maxPrice) {
                    filteredResults.add(product);
                }
            }
            return filteredResults;
        }
        return results;
    }
}