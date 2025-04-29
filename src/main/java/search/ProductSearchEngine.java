package search;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProductSearchEngine {

    private List<JSONObject> products;

    public ProductSearchEngine(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
        this.products = parseJsonData(content);
    }

    private List<JSONObject> parseJsonData(String jsonData) {
        List<JSONObject> productList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonData);
        for (int i = 0; i < jsonArray.length(); i++) {
            productList.add(jsonArray.getJSONObject(i));
        }
        return productList;
    }

    public List<JSONObject> searchProducts(String keyword) {
        List<JSONObject> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return products; // Trả về tất cả nếu không có từ khóa
        }
        String lowerKeyword = keyword.toLowerCase();
        for (JSONObject product : products) {
            if (product.has("name") && product.getString("name").toLowerCase().contains(lowerKeyword)) {
                results.add(product);
            }
        }
        return results;
    }

    public static void main(String[] args) {
        String filePath = "products.json"; // Thay bằng đường dẫn file JSON của bạn
        try {
            ProductSearchEngine engine = new ProductSearchEngine(filePath);
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            System.out.print("Nhập từ khóa tìm kiếm: ");
            String keyword = scanner.nextLine();
            List<JSONObject> searchResults = engine.searchProducts(keyword);

            if (!searchResults.isEmpty()) {
                System.out.println("Kết quả tìm kiếm:");
                for (JSONObject result : searchResults) {
                    System.out.println("- Tên: " + result.getString("name"));
                    if (result.has("productUrl")) {
                        System.out.println("  URL: " + result.getString("productUrl"));
                    }
                    // In thêm các thông tin khác bạn muốn hiển thị
                }
            } else {
                System.out.println("Không tìm thấy sản phẩm nào phù hợp.");
            }
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}