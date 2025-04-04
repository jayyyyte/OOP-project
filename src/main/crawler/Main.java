package main.crawler;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        CrawlerManager crawlerManager = new CrawlerManager();
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.println("=== CRAWLER ĐIỆN THOẠI HOÀNG HÀ MOBILE ===");
            System.out.println("1. Crawl danh sách sản phẩm và xuất CSV");
            System.out.println("2. Crawl chi tiết sản phẩm");
            System.out.print("Chọn chức năng (1/2): ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (choice) {
                case 1:
                    handleCrawlList(scanner, crawlerManager);
                    break;
                case 2:
                    handleCrawlDetail(scanner, crawlerManager);
                    break;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
        } finally {
            crawlerManager.closeCrawlers();
            scanner.close();
        }
    }

    private static void handleCrawlList(Scanner scanner, CrawlerManager crawlerManager) {
        System.out.print("Nhập URL danh mục (VD: https://hoanghamobile.com/dien-thoai): ");
        String url = scanner.nextLine();
        
        System.out.println("Đang crawl dữ liệu...");
        List<Phone> phones = crawlerManager.crawlPhoneListFromHoangHaMobile(url);
        
        System.out.println("Đã crawl được " + phones.size() + " sản phẩm");
        System.out.print("Nhập đường dẫn file CSV để lưu (VD: D:/phones.csv): ");
        String csvPath = scanner.nextLine();
        
        CsvExporter.exportPhonesToCsv(phones, csvPath);
    }

    private static void handleCrawlDetail(Scanner scanner, CrawlerManager crawlerManager) {
        System.out.print("Nhập URL sản phẩm chi tiết: ");
        String url = scanner.nextLine();
        
        System.out.println("Đang crawl thông tin chi tiết...");
        Phone phone = crawlerManager.crawlPhoneDetailsFromHoangHaMobile(url);
        
        System.out.println("\nTHÔNG TIN SẢN PHẨM:");
        System.out.println("Tên: " + phone.getName());
        System.out.println("Giá: " + phone.getPrice());
        System.out.println("Giá gốc: " + phone.getOriginalPrice());
        System.out.println("Giảm giá: " + phone.getDiscount());
        System.out.println("Đánh giá: " + phone.getRating() + " (" + phone.getReviewCount() + " đánh giá)");
        System.out.println("URL: " + phone.getProductUrl());
    }
}