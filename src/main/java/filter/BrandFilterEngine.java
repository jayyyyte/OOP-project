package filter; // Make sure this matches thư mục của bạn

import org.json.JSONObject;
import search.SearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class BrandFilterEngine extends SearchEngine {
    public BrandFilterEngine(String dataSource) throws IOException {
        super(dataSource);
    }

    @Override
    public List<JSONObject> search(Map<String, Object> criteria) {
        if (criteria == null || !criteria.containsKey("brand")) {
            return new ArrayList<>(data);
        }
        String target = ((String) criteria.get("brand")).toLowerCase(Locale.ENGLISH);

        return data.stream()
                .filter(p -> p.optString("name", "").toLowerCase(Locale.ENGLISH).contains(target))
                .collect(Collectors.toList());
    }
}