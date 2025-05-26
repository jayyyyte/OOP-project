package filter;

import org.json.JSONObject;
import search.SearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PriceRangeFilterEngine extends SearchEngine {
    public static final String CRITERIA_MIN_PRICE = "minPrice";
    public static final String CRITERIA_MAX_PRICE = "maxPrice";

    public PriceRangeFilterEngine(String dataSource) throws IOException {
        super(dataSource);
    }

    @Override
    public List<JSONObject> search(Map<String, Object> criteria) {
        // Nếu không có đầy đủ minPrice và maxPrice, trả về toàn bộ data
        if (criteria == null
            || !criteria.containsKey(CRITERIA_MIN_PRICE)
            || !criteria.containsKey(CRITERIA_MAX_PRICE)) {
            return new ArrayList<>(data);
        }

        // Lấy min/max, kiểm tra kiểu an toàn
        Object minObj = criteria.get(CRITERIA_MIN_PRICE);
        Object maxObj = criteria.get(CRITERIA_MAX_PRICE);
        double minPrice, maxPrice;

        try {
            minPrice = ((Number) minObj).doubleValue();
            maxPrice = ((Number) maxObj).doubleValue();
        } catch (ClassCastException e) {
            System.err.println("Giá trị minPrice/maxPrice không hợp lệ.");
            return new ArrayList<>(data);
        }

        List<JSONObject> filtered = new ArrayList<>();
        for (JSONObject product : data) {
            Double price = getPrice(product);
            if (price != null && price >= minPrice && price <= maxPrice) {
                filtered.add(product);
            }
        }
        return filtered;
    }
}
