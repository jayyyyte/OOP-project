package search;

import filter.BatterySearchEngine;
import filter.BrandFilterEngine;
import filter.KeywordSearchEngine;
import filter.PriceRangeFilterEngine;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*
    encapsulate all search logics and filters for a type of product
    (include SearchEngine and separate specs for that product type)
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

    private final String jsonFilePath;
    private final List<JSONObject> products = new ArrayList<>();

    /*
       Init a ProductSearcher for a product type
     */
    public ProductSearcher(String dataSource,
                         Set<String> supportedBrandsKeywords,
                         Set<String> generalProductTypeKeywords,
                         double maxCheapPrice,
                         int minStrongBattery) throws IOException, JSONException {
        // Init all engines with datasource for the product type
        this.batterySearchEngine = new BatterySearchEngine(dataSource);
        this.keywordSearchEngine = new KeywordSearchEngine(dataSource);
        this.priceRangeFilterEngine = new PriceRangeFilterEngine(dataSource);
        this.brandFilterEngine = new BrandFilterEngine(dataSource);

        this.supportedBrandsKeywords = supportedBrandsKeywords;
        this.generalProductTypeKeywords = generalProductTypeKeywords;
        this.maxCheapPrice = maxCheapPrice;
        this.minStrongBattery = minStrongBattery;
        this.jsonFilePath = dataSource;

        loadProducts();
    }

   // Load products' data from JSON files
    private void loadProducts() {
        try {
            InputStream is = getClass().getResourceAsStream(jsonFilePath);
            if (is == null) {
                throw new IOException("Could not find resource: " + jsonFilePath);
            }
            String content = new String(is.readAllBytes());
            JSONArray jsonArray = new JSONArray(content);
            for (int i = 0; i < jsonArray.length(); i++) {
                products.add(jsonArray.getJSONObject(i));
            }
        } catch (IOException | JSONException e) {
            System.err.println("Error loading products from " + jsonFilePath + ": " + e.getMessage());
        }
    }

    // implement search logic
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

        boolean anySpecialFilterActive = lookingForCheap || lookingForBattery || lookingForBrand;

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

        if (anySpecialFilterActive) {
            for (String generalKeyword : generalProductTypeKeywords) {
                remainingKeywords = remainingKeywords.replace(generalKeyword, "");
            }
        }
        remainingKeywords = remainingKeywords.replaceAll("\\s+", " ").trim();

        List<List<JSONObject>> listsToCombine = new ArrayList<>();

        // Case 1: no criteria found
        // Dựa hoàn toàn vào KeywordSearchEngine với query gốc.
        if (!anySpecialFilterActive) {
            if (query.trim().isEmpty()) {
                return new ArrayList<>(); // Query rỗng thì trả về rỗng
            }
            Map<String, Object> keywordCriteria = Map.of("keyword", query);
            return keywordSearchEngine.search(keywordCriteria);
        }

        // Case 2: at least 1 criteria found
        // Ưu tiên các bộ lọc chuyên biệt.
        if (lookingForCheap) {
            Map<String, Object> priceCriteria = Map.of("minPrice", 0.0, "maxPrice", this.maxCheapPrice);
            List<JSONObject> priceFilteredList = priceRangeFilterEngine.search(priceCriteria);
            if (priceFilteredList != null && !priceFilteredList.isEmpty()) {
                listsToCombine.add(priceFilteredList);
            } else {
                return new ArrayList<>(); // no cheap product -> return null
            }
        }

        if (lookingForBattery) {
            Map<String, Object> batteryCriteria = Map.of("minBattery", this.minStrongBattery);
            List<JSONObject> batteryFilteredList = batterySearchEngine.search(batteryCriteria);
            if (batteryFilteredList != null && !batteryFilteredList.isEmpty()) {
                listsToCombine.add(batteryFilteredList);
            } else {
                return new ArrayList<>(); // no strong battery -> return null
            }
        }

        if (lookingForBrand) {
            Map<String, Object> brandCriteria = Map.of("brand", detectedBrandKeyword);
            List<JSONObject> brandFilteredList = brandFilterEngine.search(brandCriteria);
            if (brandFilteredList != null && !brandFilteredList.isEmpty()) {
                listsToCombine.add(brandFilteredList);
            } else {
                return new ArrayList<>(); // no brand -> return null
            }
        }

        // only call KeywordSearchEngine if remainingKeywords not null
        // (và nó đã được làm sạch các từ khóa loại sản phẩm chung nếu có bộ lọc đặc biệt).
        if (!remainingKeywords.isEmpty()) {
            Map<String, Object> keywordCriteria = Map.of("keyword", remainingKeywords);
            List<JSONObject> keywordResults = keywordSearchEngine.search(keywordCriteria);
            if (keywordResults != null && !keywordResults.isEmpty()) {
                listsToCombine.add(keywordResults);
            } else {
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

    //Hàm trợ giúp để tính phép giao của hai danh sách các JSONObject
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

    public void printResults(List<JSONObject> results) {
        if (results.isEmpty()) {
            System.out.println("Không tìm thấy sản phẩm nào phù hợp với yêu cầu của bạn.");
            return;
        }
        System.out.println("Kết quả tìm kiếm (" + results.size() + " sản phẩm):");
        for (JSONObject product : results) {
            System.out.println("- Tên: " + product.optString("name", "N/A"));
            double rawPrice = product.optDouble("price", 0.0);
         // Nhân thêm 1000 để từ "27990.0" thành "27990000"
            long displayPrice = (long) (rawPrice * 1000);
         // In ra
            System.out.println("  Giá: " + displayPrice + " VND");
            System.out.println("  URL: " + product.optString("productUrl", "N/A"));
            System.out.println("-----");
        }
    }
}