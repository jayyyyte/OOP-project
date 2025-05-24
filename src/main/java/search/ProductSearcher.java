package search;

import filter.BatterySearchEngine;
import filter.BrandFilterEngine;
import filter.KeywordSearchEngine;
import filter.PriceRangeFilterEngine;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lớp này đóng gói toàn bộ logic tìm kiếm và các bộ lọc
 * cho một loại sản phẩm cụ thể (ví dụ: Điện thoại, Laptop).
 * Nó chứa các SearchEngine và các cấu hình riêng biệt (như ngưỡng giá rẻ, pin trâu, hãng)
 * cho loại sản phẩm đó.
 */
public class ProductSearcher {
    private final BatterySearchEngine batterySearchEngine;
    private final KeywordSearchEngine keywordSearchEngine;
    private final PriceRangeFilterEngine priceRangeFilterEngine;
    private final BrandFilterEngine brandFilterEngine;

    private final Set<String> supportedBrandsKeywords;
    private final Set<String> generalProductTypeKeywords;

    private final double maxCheapPrice;
    private final int minStrongBattery;

    /**
     * Khởi tạo một ProductSearcher cho một loại sản phẩm.
     *
     * @param dataSource Đường dẫn đến file JSON chứa dữ liệu sản phẩm.
     * @param supportedBrandsKeywords Tập hợp các từ khóa hãng hỗ trợ cho loại sản phẩm này.
     * @param generalProductTypeKeywords Tập hợp các từ khóa chung chỉ loại sản phẩm này (ví dụ: "điện thoại", "laptop").
     * @param maxCheapPrice Ngưỡng giá tối đa để coi là "giá rẻ" cho loại sản phẩm này.
     * @param minStrongBattery Ngưỡng pin tối thiểu để coi là "pin trâu" cho loại sản phẩm này.
     * @throws IOException Nếu có lỗi khi đọc file dữ liệu.
     * @throws JSONException Nếu có lỗi khi phân tích cú pháp JSON.
     */
    public ProductSearcher(String dataSource,
                           Set<String> supportedBrandsKeywords,
                           Set<String> generalProductTypeKeywords,
                           double maxCheapPrice,
                           int minStrongBattery) throws IOException, JSONException {
        // Khởi tạo tất cả các engine với dataSource cụ thể cho loại sản phẩm này
        this.batterySearchEngine = new BatterySearchEngine(dataSource);
        this.keywordSearchEngine = new KeywordSearchEngine(dataSource);
        this.priceRangeFilterEngine = new PriceRangeFilterEngine(dataSource);
        this.brandFilterEngine = new BrandFilterEngine(dataSource);

        this.supportedBrandsKeywords = supportedBrandsKeywords;
        this.generalProductTypeKeywords = generalProductTypeKeywords;
        this.maxCheapPrice = maxCheapPrice;
        this.minStrongBattery = minStrongBattery;
    }

    /**
     * Thực hiện tìm kiếm sản phẩm dựa trên truy vấn người dùng.
     * Logic này được điều chỉnh để ưu tiên các bộ lọc chuyên biệt
     * và bỏ qua các từ khóa loại sản phẩm chung khi có bộ lọc khác được kích hoạt.
     *
     * @param query Chuỗi truy vấn từ người dùng.
     * @return Danh sách các JSONObject biểu thị sản phẩm khớp với truy vấn.
     */
    public List<JSONObject> search(String query) {
        String lowerCaseQuery = query.toLowerCase();

        boolean lookingForCheap = lowerCaseQuery.contains("giá rẻ");
        boolean lookingForBattery = lowerCaseQuery.contains("pin trâu");

        String detectedBrandKeyword = null;
        List<String> sortedBrands = supportedBrandsKeywords.stream()
            .sorted((b1, b2) -> Integer.compare(b2.length(), b1.length()))
            .collect(Collectors.toList());

        for (String brandKeyword : sortedBrands) {
            if (lowerCaseQuery.contains(brandKeyword)) {
                detectedBrandKeyword = brandKeyword;
                break;
            }
        }
        boolean lookingForBrand = detectedBrandKeyword != null;

        // Biến cờ để kiểm tra xem có bất kỳ bộ lọc đặc biệt nào được kích hoạt không
        boolean anySpecialFilterActive = lookingForCheap || lookingForBattery || lookingForBrand;

        // Chuẩn bị remainingKeywords
        String remainingKeywords = lowerCaseQuery;
        if (lookingForCheap) {
            remainingKeywords = remainingKeywords.replace("giá rẻ", "");
        }
        if (lookingForBattery) {
            remainingKeywords = remainingKeywords.replace("pin trâu", "");
        }
        if (lookingForBrand) {
            remainingKeywords = remainingKeywords.replace(detectedBrandKeyword, "");
        }

        // Loại bỏ tất cả các từ khóa loại sản phẩm chung nếu có bất kỳ bộ lọc đặc biệt nào được kích hoạt
        if (anySpecialFilterActive) {
            for (String generalKeyword : generalProductTypeKeywords) {
                remainingKeywords = remainingKeywords.replace(generalKeyword, "");
            }
        }
        remainingKeywords = remainingKeywords.replaceAll("\\s+", " ").trim();

        List<List<JSONObject>> listsToCombine = new ArrayList<>();

        // Case 1: KHÔNG CÓ bất kỳ tiêu chí đặc biệt nào được phát hiện.
        // Dựa hoàn toàn vào KeywordSearchEngine với query gốc.
        if (!anySpecialFilterActive) {
            if (query.trim().isEmpty()) {
                return new ArrayList<>(); // Query rỗng thì trả về rỗng
            }
            Map<String, Object> keywordCriteria = Map.of("keyword", query);
            return keywordSearchEngine.search(keywordCriteria);
        }

        // Case 2: CÓ ÍT NHẤT MỘT tiêu chí đặc biệt được phát hiện.
        // Ưu tiên các bộ lọc chuyên biệt.
        
        if (lookingForCheap) {
            Map<String, Object> priceCriteria = Map.of("minPrice", 0.0, "maxPrice", this.maxCheapPrice);
            List<JSONObject> priceFilteredList = priceRangeFilterEngine.search(priceCriteria);
            if (priceFilteredList != null && !priceFilteredList.isEmpty()) {
                listsToCombine.add(priceFilteredList);
            } else {
                return new ArrayList<>(); // Không có sản phẩm giá rẻ nào, trả về rỗng ngay
            }
        }

        if (lookingForBattery) {
            Map<String, Object> batteryCriteria = Map.of("minBattery", this.minStrongBattery);
            List<JSONObject> batteryFilteredList = batterySearchEngine.search(batteryCriteria);
            if (batteryFilteredList != null && !batteryFilteredList.isEmpty()) {
                listsToCombine.add(batteryFilteredList);
            } else {
                return new ArrayList<>(); // Không có sản phẩm pin trâu nào, trả về rỗng ngay
            }
        }

        if (lookingForBrand) {
            Map<String, Object> brandCriteria = Map.of("brand", detectedBrandKeyword);
            List<JSONObject> brandFilteredList = brandFilterEngine.search(brandCriteria);
            if (brandFilteredList != null && !brandFilteredList.isEmpty()) {
                listsToCombine.add(brandFilteredList);
            } else {
                return new ArrayList<>(); // Không có sản phẩm của hãng đó, trả về rỗng ngay
            }
        }

        // Chỉ gọi KeywordSearchEngine nếu remainingKeywords KHÔNG rỗng
        // (và nó đã được làm sạch các từ khóa loại sản phẩm chung nếu có bộ lọc đặc biệt).
        if (!remainingKeywords.isEmpty()) {
            Map<String, Object> keywordCriteria = Map.of("keyword", remainingKeywords);
            List<JSONObject> keywordResults = keywordSearchEngine.search(keywordCriteria);
            if (keywordResults != null && !keywordResults.isEmpty()) {
                listsToCombine.add(keywordResults);
            } else {
                // Nếu có từ khóa chung quan trọng nhưng không tìm thấy sản phẩm nào khớp, trả về rỗng
                return new ArrayList<>();
            }
        }

        // Tính phép giao của các danh sách đã thu thập
        if (listsToCombine.isEmpty()) {
            return new ArrayList<>();
        }

        List<JSONObject> finalResults = new ArrayList<>(listsToCombine.get(0));

        for (int i = 1; i < listsToCombine.size(); i++) {
            finalResults = intersectLists(finalResults, listsToCombine.get(i));
            if (finalResults.isEmpty()) {
                return new ArrayList<>();
            }
        }

        return finalResults;
    }

    /**
     * Hàm trợ giúp để tính phép giao của hai danh sách các JSONObject.
     * So sánh dựa trên "productUrl" để xác định các sản phẩm trùng lặp.
     *
     * @param list1 Danh sách JSONObject thứ nhất.
     * @param list2 Danh sách JSONObject thứ hai.
     * @return Một danh sách mới chứa các JSONObject có mặt trong cả hai danh sách đầu vào.
     */
    private List<JSONObject> intersectLists(List<JSONObject> list1, List<JSONObject> list2) {
        if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> list2Urls = list2.stream()
                                     .map(obj -> obj.optString("productUrl", ""))
                                     .filter(url -> !url.isEmpty())
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

    /**
     * In kết quả tìm kiếm ra console.
     *
     * @param results Danh sách các JSONObject để in.
     */
    public void printResults(List<JSONObject> results) {
        if (results.isEmpty()) {
            System.out.println("Không tìm thấy sản phẩm nào phù hợp với yêu cầu của bạn.");
            return;
        }
        System.out.println("Kết quả tìm kiếm (" + results.size() + " sản phẩm):");
        for (JSONObject product : results) {
            System.out.println("- Tên: " + product.optString("name", "N/A"));
            System.out.println("  Giá: " + product.optDouble("price", 0.0) + " VND");
            System.out.println("  URL: " + product.optString("productUrl", "N/A"));
            System.out.println("-----");
        }
    }
}