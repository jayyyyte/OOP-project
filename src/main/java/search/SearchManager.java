package search;

import org.json.JSONObject;

import filter.BrandFilterEngine;

import java.util.Set;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays; // Import Arrays for List conversion
import java.util.stream.Collectors;

import filter.*;
// Assume these engine classes exist and are initialized elsewhere
// And BrandFilterEngine is the version that checks the "name" field.
// class BatterySearchEngine { ... }
// class KeywordSearchEngine { ... }
// class PriceRangeFilterEngine { ... }
// class BrandFilterEngine { ... }

// Lớp quản lý các engine tìm kiếm và phối hợp tìm kiếm
class SearchManager {
    private final BatterySearchEngine batterySearchEngine;
    private final KeywordSearchEngine keywordSearchEngine;
    private final PriceRangeFilterEngine priceRangeFilterEngine;
    private final BrandFilterEngine brandFilterEngine;

    // NEW: Danh sách các hãng được hỗ trợ tìm kiếm dựa trên từ khóa trong truy vấn
    private static final Set<String> SUPPORTED_BRANDS_KEYWORDS = new HashSet<>(
        Arrays.asList("samsung", "iphone", "apple", "xiaomi", "oppo", "vivo") // Add more brands as needed
    );

    public SearchManager(String dataSource) throws IOException {
        // Khởi tạo tất cả các engine
        // Assuming engines handle JSONException internally or propagate it correctly
        this.batterySearchEngine = new BatterySearchEngine(dataSource);
        this.keywordSearchEngine = new KeywordSearchEngine(dataSource);
        this.priceRangeFilterEngine = new PriceRangeFilterEngine(dataSource);
        this.brandFilterEngine = new BrandFilterEngine(dataSource); // Use the BrandFilterEngine that checks "name"
    }

    public List<JSONObject> searchProducts(String query) {
        String lowerCaseQuery = query.toLowerCase();

        // 1. Xác định các tiêu chí đặc biệt (giá rẻ, pin trâu, VÀ HÃNG)
        boolean lookingForCheap = lowerCaseQuery.contains("giá rẻ");
        boolean lookingForBattery = lowerCaseQuery.contains("pin trâu");

        // NEW: Xác định hãng nào (nếu có) được nhắc đến trong truy vấn
        String detectedBrandKeyword = null;
        for (String brandKeyword : SUPPORTED_BRANDS_KEYWORDS) {
            if (lowerCaseQuery.contains(brandKeyword)) {
                detectedBrandKeyword = brandKeyword;
                // Giả định chỉ quan tâm đến hãng đầu tiên được phát hiện hoặc hãng rõ ràng nhất.
                // Với các truy vấn phức tạp hơn (ví dụ: "iphone samsung"), cần logic xử lý tinh vi hơn.
                break; // Tìm thấy hãng đầu tiên, dừng lại
            }
        }
        boolean lookingForBrand = detectedBrandKeyword != null; // Có đang tìm theo hãng hay không

        // 2. Kiểm tra xem có từ khóa "thông thường" nào khác ngoài các tiêu chí đặc biệt không
        // Loại bỏ các cụm tiêu chí đã xác định ra khỏi truy vấn
        String queryWithoutCriteria = lowerCaseQuery.replace("giá rẻ", "").replace("pin trâu", "");
        // NEW: Loại bỏ cả từ khóa hãng đã phát hiện (nếu có)
        if (detectedBrandKeyword != null) {
            queryWithoutCriteria = queryWithoutCriteria.replace(detectedBrandKeyword, "");
        }
        queryWithoutCriteria = queryWithoutCriteria.trim(); // Trim leading/trailing spaces after replacement

        boolean hasOtherKeywords = !queryWithoutCriteria.isEmpty();


        // 3. Xử lý các trường hợp dựa trên sự hiện diện của tiêu chí và từ khóa
        List<List<JSONObject>> listsToCombine = new ArrayList<>();

        // Case 1: Chỉ có từ khóa (hoặc truy vấn rỗng) - Không có tiêu chí đặc biệt nào
        // NEW: Điều kiện giờ là KHÔNG có giá rẻ, KHÔNG có pin trâu, VÀ KHÔNG có hãng nào được phát hiện
        if (!lookingForCheap && !lookingForBattery && !lookingForBrand) {
            // Trả về trực tiếp kết quả từ tìm kiếm từ khóa thông thường
            // Vẫn dùng query gốc để keyword engine có ngữ cảnh đầy đủ
            Map<String, Object> keywordCriteria = Map.of("keyword", query);
            return keywordSearchEngine.search(keywordCriteria);
        }

        // Case 2: Có ít nhất một tiêu chí đặc biệt (có thể kèm từ khóa khác)
        // Thu thập các danh sách kết quả cần kết hợp (tính phép giao)

        // Thêm danh sách từ bộ lọc giá nếu tiêu chí "giá rẻ" được phát hiện
        if (lookingForCheap) {
            Map<String, Object> priceCriteria = Map.of("minPrice", 0.0, "maxPrice", 20000.0); // Sử dụng lại ngưỡng giá gốc
            List<JSONObject> priceFilteredList = priceRangeFilterEngine.search(priceCriteria);
             if (priceFilteredList != null && !priceFilteredList.isEmpty()) {
                listsToCombine.add(priceFilteredList);
            }
        }

        // Thêm danh sách từ bộ lọc pin nếu tiêu chí "pin trâu" được phát hiện
        if (lookingForBattery) {
            Map<String, Object> batteryCriteria = Map.of("minBattery", 4000);
            List<JSONObject> batteryFilteredList = batterySearchEngine.search(batteryCriteria);
             if (batteryFilteredList != null && !batteryFilteredList.isEmpty()) {
                listsToCombine.add(batteryFilteredList);
            }
        }

        // NEW: Thêm danh sách từ bộ lọc thương hiệu nếu MỘT hãng được phát hiện
        if (lookingForBrand) {
             // Truyền từ khóa hãng đã phát hiện cho BrandFilterEngine
            Map<String, Object> brandCriteria = Map.of("brand", detectedBrandKeyword);
            List<JSONObject> brandFilteredList = brandFilterEngine.search(brandCriteria);
             if (brandFilteredList != null && !brandFilteredList.isEmpty()) {
                listsToCombine.add(brandFilteredList);
            }
        }

        // Nếu có từ khóa thông thường khác, thêm kết quả tìm kiếm từ khóa vào danh sách cần kết hợp
        // Vẫn dùng query gốc cho keyword engine để tìm kiếm đầy đủ các từ còn lại (ví dụ: "điện thoại")
        if (hasOtherKeywords) {
            // Note: Using the original query here allows the keyword engine to search
            // for terms like "điện thoại" which might appear alongside the specific criteria.
            // The intersection will then filter these keyword results based on the specific criteria matches.
            Map<String, Object> keywordCriteria = Map.of("keyword", query);
            List<JSONObject> keywordResults = keywordSearchEngine.search(keywordCriteria);
             if (keywordResults != null && !keywordResults.isEmpty()) {
                listsToCombine.add(keywordResults);
            }
        }
        // Note: Nếu query chỉ chứa các tiêu chí đặc biệt đã liệt kê, hasOtherKeywords = false.

        // 4. Tính phép giao của các danh sách đã thu thập
        if (listsToCombine.isEmpty()) {
            // Trường hợp này xảy ra nếu các engine trả về null hoặc danh sách rỗng cho
            // tất cả các tiêu chí/từ khóa được phát hiện HOẶC nếu query chỉ chứa các từ
            // là tiêu chí nhưng các engine không tìm thấy gì.
            return new ArrayList<>();
        }

        // Bắt đầu với danh sách đầu tiên
        List<JSONObject> finalResults = new ArrayList<>(listsToCombine.get(0));

        // Tính phép giao với các danh sách còn lại
        for (int i = 1; i < listsToCombine.size(); i++) {
            finalResults = intersectLists(finalResults, listsToCombine.get(i)); // Sử dụng hàm intersectLists dùng productUrl
            if (finalResults.isEmpty()) {
                 // Nếu kết quả giao là rỗng ở bất kỳ bước nào, thì kết quả cuối cùng là rỗng
                 return new ArrayList<>();
            }
        }

        return finalResults;
    }

    // Hàm trợ giúp để tính phép giao của hai danh sách các JSONObject, sử dụng "productUrl"
    // Hàm này không cần thay đổi.
    private List<JSONObject> intersectLists(List<JSONObject> list1, List<JSONObject> list2) {
        if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> list2Urls = list2.stream()
                                     .map(obj -> obj.optString("productUrl", ""))
                                     .filter(url -> !url.isEmpty()) // Only add non-empty URLs
                                     .collect(Collectors.toSet());

        List<JSONObject> result = new ArrayList<>();
        for (JSONObject obj : list1) {
            String productUrl = obj.optString("productUrl", "");
            if (!productUrl.isEmpty() && list2Urls.contains(productUrl)) {
                result.add(obj);
            }
        }
        return result;
    }


    public void printResults(List<JSONObject> results) {
        // Assuming KeywordSearchEngine has a printResults method that's suitable
        // for displaying the final intersected results.
        keywordSearchEngine.printResults(results);
    }

    public static void main(String[] args) {
        String dataSource = "products.json"; // Path to your JSON data file
        try {
            // SearchManager now requires BrandFilterEngine to be initialized
            // Ensure BrandFilterEngine is the version that checks the "name" field
            SearchManager searchManager = new SearchManager(dataSource);
            Scanner scanner = new Scanner(System.in);
            System.out.print("Nhập yêu cầu tìm kiếm (ví dụ: iphone pin trâu giá rẻ): ");
            String query = scanner.nextLine();

            List<JSONObject> searchResults = searchManager.searchProducts(query);
            searchManager.printResults(searchResults);

            scanner.close();
        } catch (IOException e) {
            System.err.println("Error loading data source or during search: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) { // Catching a general Exception to be safe for JSON issues etc.
             System.err.println("An unexpected error occurred: " + e.getMessage());
             e.printStackTrace();
        }
    }
}

// Remember to use the BrandFilterEngine implementation that checks the "name" field.