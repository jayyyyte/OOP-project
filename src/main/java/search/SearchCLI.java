package search;

import org.json.JSONObject;
import java.util.List;
import java.util.Scanner;

// Command Line Interface for search functionality.
public class SearchCLI {
    private final SearchManager searchManager;

    public SearchCLI() {
        try {
            this.searchManager = new SearchManager();
        } catch (Exception e) {
            System.err.println("Error initializing SearchManager: " + e.getMessage());
            throw new RuntimeException("Failed to initialize SearchManager", e);
        }
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nNhập yêu cầu tìm kiếm ('exit' để thoát): ");
            String query = scanner.nextLine().trim();

            if (query.equalsIgnoreCase("exit")) {
                System.out.println("Thoát chương trình tìm kiếm.");
                break;
            }

            if (query.isEmpty()) {
                System.out.println("Vui lòng nhập từ khóa tìm kiếm.");
                continue;
            }

            try {
                // Xác định loại sản phẩm từ truy vấn
                ProductType detectedType = searchManager.determineProductType(query);
                
                // Thực hiện tìm kiếm
                List<JSONObject> results = searchManager.search(query);
                
                // In kết quả với loại sản phẩm đã xác định
                searchManager.printResults(results, detectedType != null ? detectedType : ProductType.PHONE);
            } catch (Exception e) {
                System.err.println("Lỗi khi tìm kiếm: " + e.getMessage());
            }
        }

        scanner.close();
    }

    public static void main(String[] args) {
        try {
            SearchCLI cli = new SearchCLI();
            cli.start();
        } catch (Exception e) {
            System.err.println("Lỗi khởi động chương trình: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 