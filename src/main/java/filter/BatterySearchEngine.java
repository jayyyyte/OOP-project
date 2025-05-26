package filter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import search.SearchEngine;    // kế thừa lớp cha
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BatterySearchEngine extends SearchEngine {
    public static final String CRITERIA_MIN_BATTERY = "minBattery";
    private static final Pattern BATTERY_NUMBER_PATTERN = Pattern.compile("\\d+");

    public BatterySearchEngine(String dataSource) throws IOException {
        super(dataSource);
    }

    private int parseBatteryCapacity(String batteryString) {
        if (batteryString == null || batteryString.isBlank()) return -1;
        Matcher m = BATTERY_NUMBER_PATTERN.matcher(batteryString);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group());
            } catch (NumberFormatException ignored) { }
        }
        return -1;
    }

    @Override
    public List<JSONObject> search(Map<String, Object> criteria) {
        List<JSONObject> results = new ArrayList<>();
        int minBattery = -1;
        if (criteria != null && criteria.containsKey(CRITERIA_MIN_BATTERY)) {
            Object v = criteria.get(CRITERIA_MIN_BATTERY);
            if (v instanceof Number) {
                minBattery = ((Number) v).intValue();
            } else {
                System.err.println("Tiêu chí pin không hợp lệ: " + v);
            }
        }

        for (JSONObject product : data) {
            try {
                if (product.has("specifications")) {
                    Object spec = product.get("specifications");
                    boolean ok = false;

                    if (spec instanceof JSONObject) {
                        String pin = ((JSONObject) spec).optString("Pin", "");
                        if (parseBatteryCapacity(pin) >= minBattery) ok = true;
                    } else if (spec instanceof JSONArray) {
                        JSONArray arr = (JSONArray) spec;
                        for (int i = 0; i < arr.length() && !ok; i++) {
                            JSONObject obj = arr.optJSONObject(i);
                            if (obj != null) {
                                String pin = obj.optString("Pin", "");
                                if (parseBatteryCapacity(pin) >= minBattery) {
                                    ok = true;
                                }
                            }
                        }
                    }

                    if (ok || minBattery <= 0) {
                        results.add(product);
                    }
                } else if (minBattery <= 0) {
                    results.add(product);
                }
            } catch (JSONException e) {
                System.err.println("Lỗi đọc thông số pin: " + e.getMessage());
            }
        }
        return results;
    }
}
