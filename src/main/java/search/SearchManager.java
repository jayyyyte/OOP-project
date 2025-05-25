package search;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Lớp SearchManager là điểm vào chính của ứng dụng tìm kiếm.
 * Nó quản lý việc khởi tạo và điều phối các ProductSearcher cho từng loại sản phẩm,
 * xác định loại sản phẩm từ truy vấn của người dùng và in kết quả.
 */
public class SearchManager {
    // Map để lưu trữ các ProductSearcher cho từng loại sản phẩm
    private final Map<ProductType, ProductSearcher> productSearchers;

    // Các định nghĩa cấu hình cho ĐIỆN THOẠI
    private static final Set<String> PHONE_BRANDS = new HashSet<>(
        Arrays.asList("samsung", "iphone", "apple", "xiaomi", "oppo", "vivo", "realme", "nokia", "huawei", "lg", "sony")
    );
    private static final Set<String> PHONE_GENERAL_KEYWORDS = new HashSet<>(
        Arrays.asList("điện thoại", "smartphone", "di động", "dế", "phone")
    );
    // Điều chỉnh ngưỡng giá và pin phù hợp với dữ liệu điện thoại của bạn
    private static final double PHONE_MAX_CHEAP_PRICE = 15000.0; // Ví dụ: dưới 15 triệu VND
    private static final int PHONE_MIN_STRONG_BATTERY = 4000; // Ví dụ: 4000 mAh

    // Các định nghĩa cấu hình cho LAPTOP
    private static final Set<String> LAPTOP_BRANDS = new HashSet<>(
        Arrays.asList("dell", "hp", "asus", "lenovo", "acer", "msi", "apple", "microsoft", "gigabyte", "razer")
    );
    private static final Set<String> LAPTOP_GENERAL_KEYWORDS = new HashSet<>(
        Arrays.asList("laptop", "máy tính xách tay", "notebook", "pc")
    );
    // Điều chỉnh ngưỡng giá và pin phù hợp với dữ liệu laptop của bạn
    private static final double LAPTOP_MAX_CHEAP_PRICE = 20000.0; // Ví dụ: dưới 20 triệu VND
    private static final int LAPTOP_MIN_STRONG_BATTERY = 40000; // Ví dụ: 40000 mWh hoặc 40 Wh

    /**
     * Constructor của SearchManager.
     * Khởi tạo các ProductSearcher cho tất cả các loại sản phẩm được hỗ trợ.
     *
     * @throws IOException Nếu có lỗi khi tải dữ liệu từ các file JSON.
     * @throws JSONException Nếu có lỗi khi phân tích cú pháp JSON.
     */
    public SearchManager() throws IOException, JSONException {
        productSearchers = new HashMap<>();

        // Khởi tạo ProductSearcher cho điện thoại
        productSearchers.put(ProductType.PHONE,
            new ProductSearcher("smartphones.json",
                                PHONE_BRANDS,
                                PHONE_GENERAL_KEYWORDS,
                                PHONE_MAX_CHEAP_PRICE,
                                PHONE_MIN_STRONG_BATTERY)
        );

        // Khởi tạo ProductSearcher cho laptop
        productSearchers.put(ProductType.LAPTOP,
            new ProductSearcher("laptops.json",
                                LAPTOP_BRANDS,
                                LAPTOP_GENERAL_KEYWORDS,
                                LAPTOP_MAX_CHEAP_PRICE,
                                LAPTOP_MIN_STRONG_BATTERY)
        );
    }

    /**
     * Phương thức tìm kiếm chính của SearchManager.
     * Xác định loại sản phẩm dựa trên truy vấn và giao nhiệm vụ cho ProductSearcher phù hợp.
     *
     * @param query Chuỗi truy vấn từ người dùng.
     * @return Danh sách các JSONObject biểu thị sản phẩm khớp với truy vấn.
     */
    public List<JSONObject> search(String query) {
        // 1. Xác định loại sản phẩm mà người dùng muốn tìm kiếm
        ProductType targetProductType = determineProductType(query);

        if (targetProductType == null) {
            // Nếu không thể xác định loại sản phẩm rõ ràng, bạn có thể thiết lập một hành vi mặc định.
            // Ví dụ: tìm kiếm điện thoại, hoặc trả về một danh sách rỗng để người dùng nhập lại.
            System.out.println("Không thể xác định loại sản phẩm rõ ràng từ truy vấn. " +
                               "Mặc định tìm kiếm trong danh mục điện thoại.");
            targetProductType = ProductType.PHONE; // Mặc định về điện thoại nếu không rõ ràng
            // Hoặc có thể: return new ArrayList<>(); để yêu cầu người dùng rõ ràng hơn.
        }

        // 2. Lấy ProductSearcher phù hợp và thực hiện tìm kiếm
        ProductSearcher currentSearcher = productSearchers.get(targetProductType);
        if (currentSearcher == null) {
            System.err.println("Lỗi nội bộ: Không tìm thấy Searcher cho loại sản phẩm " + targetProductType);
            return new ArrayList<>();
        }

        // Gọi phương thức search của ProductSearcher để thực hiện tìm kiếm chi tiết
        return currentSearcher.search(query);
    }

    /**
     * Xác định loại sản phẩm (điện thoại, laptop) dựa trên các từ khóa trong truy vấn.
     * Ưu tiên từ khóa loại sản phẩm chung, sau đó đến từ khóa hãng.
     *
     * @param query Chuỗi truy vấn cần phân tích.
     * @return ProductType được xác định, hoặc null nếu không thể xác định rõ ràng.
     */
    private ProductType determineProductType(String query) {
        String lowerCaseQuery = query.toLowerCase();

        // Ưu tiên kiểm tra các từ khóa loại sản phẩm chung
        for (String keyword : LAPTOP_GENERAL_KEYWORDS) {
            if (lowerCaseQuery.contains(keyword)) {
                return ProductType.LAPTOP;
            }
        }
        for (String keyword : PHONE_GENERAL_KEYWORDS) {
            if (lowerCaseQuery.contains(keyword)) {
                return ProductType.PHONE;
            }
        }

        // Nếu không có từ khóa loại sản phẩm chung, kiểm tra các từ khóa hãng
        // (Thứ tự kiểm tra có thể quan trọng nếu có hãng trùng tên hoặc là con của hãng khác,
        // ví dụ: Apple có cả iPhone và Macbook)
        for (String brand : LAPTOP_BRANDS) {
            if (lowerCaseQuery.contains(brand)) {
                return ProductType.LAPTOP;
            }
        }
        for (String brand : PHONE_BRANDS) {
            if (lowerCaseQuery.contains(brand)) {
                return ProductType.PHONE;
            }
        }

        return null; // Không thể xác định loại sản phẩm
    }

    /**
     * In kết quả tìm kiếm ra console.
     * Phương thức này sẽ ủy quyền việc in cho ProductSearcher tương ứng
     * để đảm bảo định dạng hiển thị phù hợp với loại sản phẩm.
     *
     * @param results Danh sách các JSONObject biểu thị sản phẩm.
     * @param productType Loại sản phẩm mà kết quả này thuộc về (để chọn trình in phù hợp).
     */
    public void printResults(List<JSONObject> results, ProductType productType) {
        // Lấy ProductSearcher phù hợp để sử dụng phương thức in của nó
        ProductSearcher currentSearcher = productSearchers.get(productType);
        if (currentSearcher != null) {
            currentSearcher.printResults(results);
        } else {
            // Trường hợp không tìm thấy searcher (rất hiếm khi xảy ra nếu logic determineProductType tốt)
            System.out.println("Không tìm thấy trình in kết quả cho loại sản phẩm " + productType + ". In theo định dạng chung:");
            if (results.isEmpty()) {
                System.out.println("Không tìm thấy sản phẩm nào phù hợp với yêu cầu của bạn.");
                return;
            }
            System.out.println("Kết quả tìm kiếm (" + results.size() + " sản phẩm):");
            for (JSONObject product : results) {
                System.out.println("- Tên: " + product.optString("name", "N/A"));
                double rawPrice = product.optDouble("price", 0.0);
                long displayPrice = (long) (rawPrice * 1000);
                System.out.println("  Giá: " + displayPrice + " VND");
                JSONObject specifications = product.optJSONObject("specifications");
                if (specifications != null) {
                    System.out.println("  Pin: " + specifications.optInt("battery", 0) + " (N/A đơn vị)");
                } else {
                    System.out.println("  Pin: N/A");
                }
                System.out.println("  URL: " + product.optString("productUrl", "N/A"));
                System.out.println("-----");
            }
        }
    }

    /**
     * Điểm bắt đầu của chương trình.
     *
     * @param args Đối số dòng lệnh.
     */
    public static void main(String[] args) {
        try {
            SearchManager searchManager = new SearchManager();
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Nhập yêu cầu tìm kiếm ('exit' để thoát): ");
                String query = scanner.nextLine();

                if (query.equalsIgnoreCase("exit")) {
                    System.out.println("Thoát chương trình tìm kiếm.");
                    break;
                }

                ProductType detectedType = searchManager.determineProductType(query); // Lấy loại sản phẩm trước khi tìm kiếm
                List<JSONObject> searchResults = searchManager.search(query);

                // Truyền detectedType để printResults có thể chọn đúng ProductSearcher để in
                searchManager.printResults(searchResults, detectedType != null ? detectedType : ProductType.PHONE);
                // Nếu determineProductType trả về null, chúng ta mặc định dùng ProductType.PHONE để in,
                // hoặc bạn có thể tạo một ProductSearcher generic chỉ để in.
            }

            scanner.close();
        } catch (IOException e) {
            System.err.println("Lỗi khi tải dữ liệu hoặc trong quá trình tìm kiếm: " + e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            System.err.println("Lỗi phân tích cú pháp JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }
    }
}