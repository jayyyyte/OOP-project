package search;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// Abstract class định nghĩa cấu trúc chung cho các engine tìm kiếm
public abstract class SearchEngine {

    protected List<JSONObject> data;
    protected String dataSource;

    public SearchEngine(String dataSource) throws IOException {
        this.dataSource = dataSource;
        this.data = loadData(dataSource);
    }

    protected List<JSONObject> loadData(String dataSource) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(dataSource)), "UTF-8");
        List<JSONObject> productList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
            productList.add(jsonArray.getJSONObject(i));
        }
        return productList;
    }

    public abstract List<JSONObject> search(Map<String, Object> criteria);

    public void printResults(List<JSONObject> results) {
        if (!results.isEmpty()) {
            System.out.println("Kết quả tìm kiếm:");
            for (JSONObject result : results) {
                System.out.println("- Tên: " + result.getString("name"));
                if (result.has("productUrl")) {
                    System.out.println("  URL: " + result.getString("productUrl"));
                }
                if (result.has("description")) {
                    System.out.println("  Mô tả: " + result.getString("description"));
                }
                if (result.has("price")) {
                    System.out.println("  Giá: " + result.getDouble("price") + " " + result.optString("priceCurrency", "VND"));
                }
                // In thêm các thông tin khác bạn muốn hiển thị
                System.out.println("---");
            }
        } else {
            System.out.println("Không tìm thấy sản phẩm nào phù hợp.");
        }
    }

    protected Double getPrice(JSONObject product) {
        if (product.has("price") && !product.isNull("price")) {
            return product.getDouble("price");
        }
        return null;
    }
}