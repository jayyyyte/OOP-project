import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebScraper {

    // --- IMPORTANT: Selectors need inspection and likely adjustment ---
    // These are *examples* based on a quick look (April 2025). They WILL change.
    // Use Browser Developer Tools (F12 -> Inspector) to find the correct ones.
    private static final String CATEGORY_URL = "https://cellphones.com.vn/mobile.html"; // Target category
    private static final String PRODUCT_ITEM_SELECTOR = "div.product-info-container.product-item"; // Selector for each product box on category page
    private static final String PRODUCT_LINK_SELECTOR = "a.product__link.button__link"; // Selector for the link within the product box
    private static final String NEXT_PAGE_SELECTOR = "a.action.next"; // Selector for the 'next page' button (if pagination exists)

    private static final String PRODUCT_NAME_SELECTOR = "h1.product__name"; // Example: Check multiple possibilities
    private static final String PRODUCT_PRICE_SELECTOR = ".block-box-price"; // Price element
    private static final String SHORT_DESC_SELECTOR = ".product-short-description"; // Short description (if present)
    private static final String SPEC_TABLE_SELECTOR = "#popup-tskt table tbody tr"; // Rows within the spec table (might need a click first)
    private static final String SPEC_KEY_SELECTOR = "th"; // Spec name (e.g., "Kích thước màn hình")
    private static final String SPEC_VALUE_SELECTOR = "td"; // Spec value (e.g., "6.1 inches")
    private static final String SPEC_BUTTON_SELECTOR = "div.btn-config-detail"; // Button to show full specs popup

    // --- Configuration ---
    private static final int MAX_PAGES_TO_SCRAPE = 3; // Limit number of pages to avoid overloading
    private static final int DELAY_BETWEEN_REQUESTS_MS = 2000; // Be polite: wait between page loads (milliseconds)
    private static final int WEBDRIVER_WAIT_TIMEOUT_SECONDS = 15; // Max time to wait for elements

    public static void main(String[] args) {
        // --- Setup WebDriver ---
        WebDriverManager.chromedriver().setup(); // Auto-manages ChromeDriver
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // Run Chrome without opening a visible window (optional)
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"); // Mimic a real browser
        options.addArguments("--lang=vi-VN"); // Set language preference

        WebDriver driver = null;
        List<Product> allProducts = new ArrayList<>();

        try {
            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WEBDRIVER_WAIT_TIMEOUT_SECONDS));

            String currentPageUrl = CATEGORY_URL;
            int pagesScraped = 0;

            // --- Loop through category pages ---
            while (currentPageUrl != null && pagesScraped < MAX_PAGES_TO_SCRAPE) {
                System.out.println("Scraping category page: " + currentPageUrl);
                driver.get(currentPageUrl);
                pause(); // Wait a bit after loading

                // Wait for product items to be present
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(PRODUCT_ITEM_SELECTOR)));

                List<WebElement> productElements = driver.findElements(By.cssSelector(PRODUCT_ITEM_SELECTOR));
                List<String> productUrls = new ArrayList<>();

                // --- Extract product URLs from the current page ---
                for (WebElement item : productElements) {
                    try {
                        WebElement linkElement = item.findElement(By.cssSelector(PRODUCT_LINK_SELECTOR));
                        String productUrl = linkElement.getAttribute("href");
                        if (productUrl != null && !productUrl.isEmpty()) {
                            productUrls.add(productUrl);
                            System.out.println("Found product link: " + productUrl);
                        }
                    } catch (NoSuchElementException e) {
                        System.err.println("Could not find product link within an item. Structure might have changed.");
                    }
                }

                // --- Scrape details for each product URL found ---
                for (String url : productUrls) {
                    Product product = scrapeProductDetails(driver, wait, url);
                    if (product != null) {
                        allProducts.add(product);
                    }
                    pause(); // Wait between scraping individual product pages
                }

                // --- Find the next page link ---
                try {
                    WebElement nextPageButton = driver.findElement(By.cssSelector(NEXT_PAGE_SELECTOR));
                    currentPageUrl = nextPageButton.getAttribute("href");
                    pagesScraped++;
                    System.out.println("Found next page: " + currentPageUrl);
                } catch (NoSuchElementException e) {
                    System.out.println("No 'next page' button found. Reached the end or selector is wrong.");
                    currentPageUrl = null; // Stop pagination
                }
            }

        } catch (TimeoutException e) {
            System.err.println("Timeout waiting for element: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred during scraping: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit(); // Close the browser window and end the WebDriver session
            }
        }

        // --- Save collected data to JSON ---
        if (!allProducts.isEmpty()) {
            JsonUtils.saveProductsToJson(allProducts, "cellphones_smartphones.json");
        } else {
            System.out.println("No products were scraped.");
        }
    }

    /**
     * Scrapes details from a single product page.
     */
    private static Product scrapeProductDetails(WebDriver driver, WebDriverWait wait, String productUrl) {
        System.out.println("  Scraping product: " + productUrl);
        driver.get(productUrl);
        pause(1000); // Small pause after page load

        Product product = new Product();
        product.setUrl(productUrl);

        try {
            // --- Get Product Name ---
            try {
                WebElement nameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(PRODUCT_NAME_SELECTOR)));
                product.setName(nameElement.getText().trim());
            } catch (TimeoutException | NoSuchElementException e) {
                System.err.println("    Could not find product name for " + productUrl);
            }

            // --- Get Product Price ---
            try {
                // Prices can be tricky (strikethrough old price, discount price) - adjust selector!
                WebElement priceElement = driver.findElement(By.cssSelector(PRODUCT_PRICE_SELECTOR));
                product.setPrice(priceElement.getText().trim());
            } catch (NoSuchElementException e) {
                System.err.println("    Could not find product price for " + productUrl);
            }

            // --- Get Short Description (Optional) ---
            try {
                WebElement descElement = driver.findElement(By.cssSelector(SHORT_DESC_SELECTOR));
                product.setShortDescription(descElement.getText().trim());
            } catch (NoSuchElementException e) {
                // Optional field, maybe log a warning if needed
                // System.out.println("    No short description found for " + productUrl);
            }


            // --- Get Specifications (May require clicking a button) ---
            Map<String, String> specs = new HashMap<>();
            try {
                // Try to click the "View Specs" button if it exists
                try {
                    WebElement specButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(SPEC_BUTTON_SELECTOR)));
                    // Use JavaScript click if normal click fails due to overlays etc.
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", specButton);
                    System.out.println("    Clicked specs button.");
                    pause(1500); // Wait for popup/content to load
                    // Important: Adjust SPEC_TABLE_SELECTOR to target the *modal/popup* content now
                } catch (TimeoutException | NoSuchElementException e) {
                    System.out.println("    Specs button not found or not clickable, looking for specs directly on page.");
                }

                // Find the spec table/list rows (wait for them to be visible)
                List<WebElement> specRows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(SPEC_TABLE_SELECTOR)));
                for (WebElement row : specRows) {
                    try {
                        String key = row.findElement(By.cssSelector(SPEC_KEY_SELECTOR)).getText().trim();
                        String value = row.findElement(By.cssSelector(SPEC_VALUE_SELECTOR)).getText().trim();
                        if (!key.isEmpty()) {
                            specs.put(key, value);
                        }
                    } catch (NoSuchElementException e) {
                        // Ignore rows that don't fit the key-value structure
                    }
                }
            } catch (TimeoutException | NoSuchElementException e) {
                System.err.println("    Could not find specifications table/rows for " + productUrl);
            }
            product.setSpecifications(specs);


            // --- TODO: Scrape Reviews & Ratings ---
            // Reviews are often loaded dynamically, maybe via scrolling or "load more" buttons.
            // This would require more complex Selenium logic:
            // 1. Find the review section/container.
            // 2. Find review elements (e.g., div.review-item).
            // 3. Extract text, rating stars.
            // 4. Potentially click "load more" and repeat until no more reviews load.
            // 5. Handle ratings (often an average score is displayed separately).
            product.setReviews(new ArrayList<>()); // Placeholder
            product.setRating(0.0); // Placeholder

            System.out.println("    Successfully scraped: " + product.getName());
            return product;

        } catch (Exception e) {
            System.err.println("    Error scraping details for " + productUrl + ": " + e.getMessage());
            // Don't stop the whole process, just skip this product
            return null;
        }
    }

    /**
     * Pauses execution for a specified duration.
     */
    private static void pause(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            System.err.println("Thread interrupted during pause.");
        }
    }

    /**
     * Pauses execution using the default delay.
     */
    private static void pause() {
        pause(DELAY_BETWEEN_REQUESTS_MS);
    }
}