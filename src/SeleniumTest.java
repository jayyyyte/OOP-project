package main.crawler;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumTest {
    public static void main(String[] args) {
        try {
            // Đường dẫn đến ChromeDriver - thay đổi theo vị trí thực tế trên máy của bạn
        	System.setProperty("webdriver.chrome.driver", "E:\\\\OOP-project\\\\drivers\\\\chromedriver.exe");
            
            // Thiết lập các tùy chọn cho Chrome (tùy chọn)
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            
            // Khởi tạo WebDriver
            System.out.println("Đang khởi tạo Chrome WebDriver...");
            WebDriver driver = new ChromeDriver(options);
            
            // Mở trang web
            System.out.println("Đang mở trang web...");
            driver.get("https://www.google.com");
            
            // Lấy thông tin trang
            String title = driver.getTitle();
            System.out.println("Tiêu đề trang: " + title);
            
            // Tạm dừng để có thể thấy trình duyệt (tùy chọn)
            Thread.sleep(9000);
            
            // Đóng trình duyệt
            System.out.println("Đóng trình duyệt...");
            driver.quit();
            
            System.out.println("Test hoàn thành thành công!");
        } catch (Exception e) {
            System.out.println("Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
    }
}