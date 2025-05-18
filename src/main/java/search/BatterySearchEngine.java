package search;

// Remove Gson imports, use org.json consistent with SearchEngine
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException; // Keep JSONException for handling potential issues with product structure

import java.io.IOException;
// Remove unused Reader/FileReader imports
import java.util.ArrayList; // Needed for results list
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher; // Needed for battery parsing regex
import java.util.regex.Pattern; // Needed for battery parsing regex


// Make BatterySearchEngine extend the abstract SearchEngine
public class BatterySearchEngine extends SearchEngine {

     // Define constants and patterns for battery parsing/filtering criteria
    public static final String CRITERIA_MIN_BATTERY = "minBattery";
    private static final Pattern BATTERY_NUMBER_PATTERN = Pattern.compile("\\d+"); // Regex to find numbers in battery string


    /**
     * Khởi tạo BatterySearchEngine bằng cách đọc dữ liệu từ file JSON.
     * Sử dụng constructor của lớp cha SearchEngine để tải dữ liệu.
     *
     * @param dataSource Đường dẫn đến file JSON chứa mảng dữ liệu sản phẩm.
     * @throws IOException Nếu có lỗi khi đọc file trong lớp cha.
     */
    public BatterySearchEngine(String dataSource) throws IOException {
        // Call the parent class constructor to load data from the dataSource
        super(dataSource);
        // The data is now available in the protected field 'data' inherited from SearchEngine
    }

    /**
     * Trích xuất dung lượng pin dưới dạng số nguyên từ chuỗi.
     * (Copied from SearchManager, needed for filtering logic)
     *
     * @param batteryString Chuỗi chứa thông tin pin (ví dụ: "4000 mAh" hoặc "4000").
     * @return Dung lượng pin dưới dạng số nguyên, hoặc -1 nếu không thể parse.
     */
    private int parseBatteryCapacity(String batteryString) {
        if (batteryString == null || batteryString.trim().isEmpty()) {
            return -1;
        }

        try {
            // Regex để tìm số trong chuỗi
            Matcher matcher = BATTERY_NUMBER_PATTERN.matcher(batteryString);

            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }

            // Fallback: Try traditional method if regex fails
            String numericPart = batteryString.trim().replaceAll("[^0-9]", "").trim();
            if (!numericPart.isEmpty()) {
                return Integer.parseInt(numericPart);
            }

            return -1;
        } catch (NumberFormatException e) {
            // Log or handle error if necessary, but returning -1 is often sufficient for filtering
            // System.err.println("Could not parse battery capacity from string: " + batteryString);
            return -1;
        }
    }


    /**
     * Thực hiện tìm kiếm sản phẩm dựa trên tiêu chí, cụ thể là dung lượng pin.
     * Triển khai phương thức abstract từ SearchEngine.
     *
     * @param criteria Map chứa các tiêu chí tìm kiếm. Dự kiến chứa "minBattery" (int).
     * @return Danh sách các JSONObject sản phẩm đáp ứng tiêu chí pin.
     */
    @Override
    public List<JSONObject> search(Map<String, Object> criteria) {
        List<JSONObject> results = new ArrayList<>();

        // Kiểm tra xem dữ liệu đã được tải thành công chưa
        if (this.data == null || this.data.isEmpty()) {
            System.err.println("Dữ liệu sản phẩm chưa được tải hoặc rỗng.");
            return results; // Trả về danh sách rỗng
        }

        // Lấy ngưỡng pin tối thiểu từ tiêu chí
        int minBatteryCapacity = -1; // Giá trị mặc định không lọc

        if (criteria != null && criteria.containsKey(CRITERIA_MIN_BATTERY)) {
            try {
                // Criteria map stores Object, cast to Integer
                minBatteryCapacity = (Integer) criteria.get(CRITERIA_MIN_BATTERY);
            } catch (ClassCastException | NullPointerException e) {
                System.err.println("Tiêu chí '" + CRITERIA_MIN_BATTERY + "' không hợp lệ: " + e.getMessage());
                // Keep minBatteryCapacity at -1, means no filtering or invalid filter
            }
        }

        // Nếu không có tiêu chí pin hợp lệ, có thể trả về tất cả hoặc danh sách rỗng tùy logic mong muốn.
        // Hiện tại, nếu minBatteryCapacity <= 0, sẽ không lọc theo pin (mọi pin >= -1 đều được chấp nhận).
        // Nếu bạn muốn chỉ tìm khi có tiêu chí pin, thêm kiểm tra ở đây.
        // if (minBatteryCapacity <= 0) return results; // Uncomment this if you only search by battery

        // Lặp qua tất cả sản phẩm đã được tải bởi SearchEngine
        for (JSONObject product : this.data) {
            try {
                // Lấy trường 'specifications'
                if (product.has("specifications")) {
                    Object specObj = product.get("specifications");

                    // Logic để tìm thông tin pin trong specifications (JSONObject hoặc JSONArray)
                    boolean meetsBatteryCriteria = false;

                    if (specObj instanceof JSONObject) {
                        JSONObject specifications = (JSONObject) specObj;
                        if (specifications.has("Pin")) {
                            String batteryString = specifications.getString("Pin");
                            int batteryCapacity = parseBatteryCapacity(batteryString);
                            if (batteryCapacity >= minBatteryCapacity) {
                                meetsBatteryCriteria = true;
                            }
                        }
                    } else if (specObj instanceof JSONArray) {
                         // Handle JSONArray of specifications (array of objects with specs)
                         JSONArray specArray = (JSONArray) specObj;
                         for (int i = 0; i < specArray.length(); i++) {
                             JSONObject spec = specArray.optJSONObject(i); // Try to get an object
                             if (spec != null && spec.has("Pin")) { // Check if object exists and has "Pin"
                                 String batteryString = spec.getString("Pin");
                                 int batteryCapacity = parseBatteryCapacity(batteryString);
                                 if (batteryCapacity >= minBatteryCapacity) {
                                     meetsBatteryCriteria = true;
                                     break; // Found sufficient battery in specs array
                                 }
                             }
                         }
                    }
                    // else: specifications is not JSONObject or JSONArray, ignore for battery filter


                    // Nếu sản phẩm đáp ứng tiêu chí pin (hoặc không có tiêu chí pin hợp lệ để lọc), thêm vào kết quả
                     if (meetsBatteryCriteria || minBatteryCapacity <= 0) {
                        results.add(product);
                    }

                } else if (minBatteryCapacity <= 0) {
                    // If no specifications field, and no battery criteria is set, include the product
                     results.add(product);
                }
                // If specifications is missing AND minBatteryCriteria > 0, product is not added.

            } catch (JSONException e) {
                 // Log or handle error if parsing a specific product fails
                 System.err.println("Lỗi xử lý JSON cho sản phẩm (pin filter): " +
                    product.optString("name", "Không rõ tên") + " - " + e.getMessage());
                 // This product is skipped
            } catch (Exception e) {
                // Catch any other unexpected errors during processing
                 System.err.println("Lỗi không mong muốn khi lọc pin cho sản phẩm: " +
                    product.optString("name", "Không rõ tên") + " - " + e.getMessage());
                 // This product is skipped
            }
        }

        return results;
    }
    
    // Note: printResults is inherited from SearchEngine and can be used directly or overridden if needed.
    // The main method is no longer needed here as SearchManager orchestrates the search.
}

