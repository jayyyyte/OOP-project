package search;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;

//Lớp quản lý các engine tìm kiếm và phối hợp tìm kiếm
class SearchManager {

 private final KeywordSearchEngine keywordSearchEngine;
 private final PriceRangeFilterEngine priceRangeFilterEngine;

 public SearchManager(String dataSource) throws IOException {
     this.keywordSearchEngine = new KeywordSearchEngine(dataSource);
     this.priceRangeFilterEngine = new PriceRangeFilterEngine(dataSource);
 }

 public List<JSONObject> searchProducts(String query) {
	    List<JSONObject> results = new ArrayList<>();

	    if (query.toLowerCase().contains("giá rẻ")) {
	        // Nếu truy vấn chứa "giá rẻ", trả về tất cả sản phẩm dưới ngưỡng giá
	        Map<String, Object> priceCriteria = Map.of("minPrice", 0.0, "maxPrice", 10000.0);
	        return priceRangeFilterEngine.search(priceCriteria);
	    } else {
	        // Nếu không phải tìm kiếm "giá rẻ", thực hiện tìm kiếm theo từ khóa thông thường
	        Map<String, Object> keywordCriteria = Map.of("keyword", query);
	        results = keywordSearchEngine.search(keywordCriteria);
	    }

	    // Thêm logic lọc pin trâu (vẫn giữ nguyên nếu cần)
	    if (query.toLowerCase().contains("pin trâu")) {
	        List<JSONObject> tempResults = new ArrayList<>();
	        for (JSONObject product : results) {
	            if (product.has("specifications") && product.getJSONObject("specifications").has("batteryCapacity")) {
	                int battery = product.getJSONObject("specifications").getInt("batteryCapacity");
	                if (battery >= 5000) {
	                    tempResults.add(product);
	                }
	            }
	        }
	        results = tempResults;
	    }

	    return results;
	}

 public void printResults(List<JSONObject> results) {
     keywordSearchEngine.printResults(results);
 }

 public static void main(String[] args) {
     String dataSource = "products.json";
     try {
         SearchManager searchManager = new SearchManager(dataSource);
         Scanner scanner = new Scanner(System.in);
         System.out.print("Nhập yêu cầu tìm kiếm (ví dụ: điện thoại pin trâu giá rẻ): ");
         String query = scanner.nextLine();

         List<JSONObject> searchResults = searchManager.searchProducts(query);
         searchManager.printResults(searchResults);

         scanner.close();
     } catch (IOException e) {
         e.printStackTrace();
     }
 }
}