package main.crawler;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Tạo instance của FptShopCrawler
        FptShopCrawler crawler = new FptShopCrawler();
        
        // QUAN TRỌNG: Thêm proxy vào crawler (thay thế bằng proxy thực của bạn)
        // Ví dụ: crawler.addProxy("103.126.12.196", 8080, null, null);
        // Nếu proxy có xác thực: crawler.addProxy("103.126.12.196", 8080, "username", "password");
        
        // Kiểm tra proxy trước khi sử dụng
        // boolean proxyWorks = crawler.testProxy("103.126.12.196", 8080);
        // System.out.println("Proxy status: " + (proxyWorks ? "Working" : "Not working"));
        
        // Tạo instance của CrawlerManager
        CrawlerManager manager = new CrawlerManager(crawler);
        
        try {
            // Crawl danh mục điện thoại
            // Giới hạn số lượng sản phẩm crawl để tránh bị chặn
            manager.crawlAndSavePhones(
                "https://fptshop.com.vn/dien-thoai", 
                "fptshop_phones.txt"
            );
            
            // Các URL khác có thể thử nếu trang chính không hoạt động
            // manager.crawlAndSavePhones("https://fptshop.com.vn/dien-thoai/apple-iphone", "iphone_phones.txt");
            // manager.crawlAndSavePhones("https://fptshop.com.vn/dien-thoai/samsung", "samsung_phones.txt");
            
        } catch (Exception e) {
            System.err.println("Error in main execution: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Crawler finished execution");
    }
}