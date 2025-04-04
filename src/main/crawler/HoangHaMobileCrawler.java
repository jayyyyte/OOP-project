package main.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HoangHaMobileCrawler {
    private WebDriver driver;
    private WebDriverWait wait;

    public HoangHaMobileCrawler() {
        // Cấu hình ChromeDriver
        System.setProperty("webdriver.chrome.driver", "E:/OOP-project/drivers/chromedriver.exe");
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Chạy ẩn trình duyệt
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        
        this.driver = new ChromeDriver(options);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public List<Phone> crawlPhoneList(String url) {
        List<Phone> phones = new ArrayList<>();
        
        try {
            driver.get(url);
            
            // Chờ cho danh sách sản phẩm load xong
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".listproduct")));
            
            List<WebElement> productElements = driver.findElements(By.cssSelector(".item.product"));
            
            for (WebElement productElement : productElements) {
                Phone phone = new Phone();
                
                // Lấy tên sản phẩm
                WebElement nameElement = productElement.findElement(By.cssSelector(".product-name a"));
                phone.setName(nameElement.getText());
                phone.setProductUrl(nameElement.getAttribute("href"));
                
                // Lấy giá sản phẩm
                try {
                    WebElement priceElement = productElement.findElement(By.cssSelector(".price"));
                    phone.setPrice(priceElement.getText());
                } catch (Exception e) {
                    phone.setPrice("N/A");
                }
                
                // Lấy giá gốc (nếu có)
                try {
                    WebElement originalPriceElement = productElement.findElement(By.cssSelector(".old-price"));
                    phone.setOriginalPrice(originalPriceElement.getText());
                } catch (Exception e) {
                    phone.setOriginalPrice("N/A");
                }
                
                // Lấy phần trăm giảm giá (nếu có)
                try {
                    WebElement discountElement = productElement.findElement(By.cssSelector(".product-label.sale-label"));
                    phone.setDiscount(discountElement.getText());
                } catch (Exception e) {
                    phone.setDiscount("N/A");
                }
                
                // Lấy hình ảnh
                try {
                    WebElement imageElement = productElement.findElement(By.cssSelector(".product-image img"));
                    phone.setImageUrl(imageElement.getAttribute("src"));
                } catch (Exception e) {
                    phone.setImageUrl("N/A");
                }
                
                phones.add(phone);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return phones;
    }

    public Phone crawlPhoneDetails(String productUrl) {
        Phone phone = new Phone();
        
        try {
            driver.get(productUrl);
            
            // Chờ cho trang chi tiết load xong
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-info-main")));
            
            // Lấy tên sản phẩm
            WebElement nameElement = driver.findElement(By.cssSelector(".page-title span"));
            phone.setName(nameElement.getText());
            
            // Lấy giá
            try {
                WebElement priceElement = driver.findElement(By.cssSelector(".price-final"));
                phone.setPrice(priceElement.getText());
            } catch (Exception e) {
                phone.setPrice("N/A");
            }
            
            // Lấy giá gốc
            try {
                WebElement originalPriceElement = driver.findElement(By.cssSelector(".price-old"));
                phone.setOriginalPrice(originalPriceElement.getText());
            } catch (Exception e) {
                phone.setOriginalPrice("N/A");
            }
            
            // Lấy phần trăm giảm giá
            try {
                WebElement discountElement = driver.findElement(By.cssSelector(".product-sale"));
                phone.setDiscount(discountElement.getText());
            } catch (Exception e) {
                phone.setDiscount("N/A");
            }
            
            // Lấy mô tả
            try {
                WebElement descriptionElement = driver.findElement(By.cssSelector(".product-specs"));
                phone.setDescription(descriptionElement.getText());
            } catch (Exception e) {
                phone.setDescription("N/A");
            }
            
            // Lấy thông số kỹ thuật
            try {
                WebElement specsElement = driver.findElement(By.cssSelector(".product-params"));
                phone.setSpecifications(specsElement.getText());
            } catch (Exception e) {
                phone.setSpecifications("N/A");
            }
            
            // Lấy hình ảnh
            try {
                WebElement imageElement = driver.findElement(By.cssSelector(".product-image-gallery img"));
                phone.setImageUrl(imageElement.getAttribute("src"));
            } catch (Exception e) {
                phone.setImageUrl("N/A");
            }
            
            // Lấy đánh giá
            try {
                WebElement ratingElement = driver.findElement(By.cssSelector(".rating-result"));
                phone.setRating(ratingElement.getAttribute("title"));
            } catch (Exception e) {
                phone.setRating("N/A");
            }
            
            // Lấy số lượng đánh giá
            try {
                WebElement reviewCountElement = driver.findElement(By.cssSelector(".action.view"));
                phone.setReviewCount(reviewCountElement.getText().replaceAll("\\D+", ""));
            } catch (Exception e) {
                phone.setReviewCount("0");
            }
            
            phone.setProductUrl(productUrl);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return phone;
    }

    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}