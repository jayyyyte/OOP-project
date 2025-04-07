import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;

public class WebScraper {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://cellphones.com.vn/mobile.html";

    // Selector for the container of each product on the list page
    private static final String PRODUCT_CONTAINER_SELECTOR = "div.product-info";
// The container for each product is a <div> with class "product-info".

    // Selectors relative to the PRODUCT_CONTAINER_SELECTOR
    private static final String NAME_SELECTOR = "h3";
// Within "product-info", the product name is directly inside an <h3> tag.

    private static final String PRICE_SELECTOR = "p.product__price--show";
// The current price is in a <p> with class "product__price--show".

    private static final String IMAGE_SELECTOR = "div.product__image img";
// The image is inside a <div class="product__image">, and we target the <img> tag for the source.

    private static final String DETAIL_URL_SELECTOR = "a";
// The product link is an <a> tag wrapping the entire "product-info" div, no specific class needed.

    // Selectors for the product detail page
    private static final String DETAIL_DESCRIPTION_SELECTOR = "div.block-technical-content";
// The full description is in a <div> with class "block-technical-content" on the detail page.

    private static final String DETAIL_SPECS_TABLE_SELECTOR = "ul.box-product__charactestic-ul li";
// Specs are in a <ul> with class "box-product__charactestic-ul", and each spec is an <li>.

    private static final String DETAIL_RATING_SELECTOR = "span.rating__average";
// The average rating is in a <span> with class "rating__average" on the detail page.

    private static final String DETAIL_REVIEW_COUNT_SELECTOR = "span.rating__total";
// The review count is in a <span> with class "rating__total".

    public WebScraper() {
        WebDriverManager.chromedriver().setup();
        // Optional: Run headless (without opening a visible browser window)
        // ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new");
        // this.driver = new ChromeDriver(options);
        this.driver = new ChromeDriver();
        this.driver.manage().window().maximize();
        // Set a default wait time for elements to appear (adjust as needed)
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public List<Product> scrapeProducts() {
        List<Product> productList = new ArrayList<>();
        driver.get(BASE_URL);

        try {
            // Wait for the product containers to be present on the page
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(PRODUCT_CONTAINER_SELECTOR)));

            List<WebElement> productContainers = driver.findElements(By.cssSelector(PRODUCT_CONTAINER_SELECTOR));
            System.out.println("Found " + productContainers.size() + " product containers on the list page.");

            // --- Step 1: Scrape basic info and detail URLs from the list page ---
            List<String> detailUrls = new ArrayList<>();
            for (WebElement container : productContainers) {
                try {
                    Product product = new Product();

                    // Extract Name
                    WebElement nameElement = container.findElement(By.cssSelector(NAME_SELECTOR));
                    product.setName(nameElement.getText().trim());

                    // Extract Detail URL (often from the name link)
                    WebElement detailLinkElement = container.findElement(By.cssSelector(DETAIL_URL_SELECTOR)); // Or use nameElement if it's the link
                    String detailUrl = detailLinkElement.getAttribute("href");
                    product.setProductUrl(detailUrl);
                    detailUrls.add(detailUrl); // Store URL for later visit

                    // Extract Price
                    WebElement priceElement = container.findElement(By.cssSelector(PRICE_SELECTOR));
                    product.setPrice(Product.parsePrice(priceElement.getText())); // Use helper

                    // Extract Image URL
                    try { // Image might be optional or load lazily
                        WebElement imageElement = container.findElement(By.cssSelector(IMAGE_SELECTOR));
                        product.setImageUrl(imageElement.getAttribute("src"));
                    } catch (NoSuchElementException imgEx) {
                        System.err.println("Image not found for product: " + product.getName());
                        product.setImageUrl(null); // Or a default placeholder URL
                    }

                    productList.add(product);
                    System.out.println("Scraped basic info for: " + product.getName());

                    // --- !! ETHICAL SCRAPING: Add a small delay !! ---
                    Thread.sleep(150); // 100 milliseconds (adjust as needed)

                } catch (NoSuchElementException e) {
                    System.err.println("Could not find an element within a product container. Skipping item. Error: " + e.getMessage());
                } catch (StaleElementReferenceException e) {
                    System.err.println("Element became stale. Skipping item. Consider refreshing elements or page. Error: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    System.err.println("Scraping interrupted.");
                    break; // Exit loop if interrupted
                }
            }

            System.out.println("\n--- Starting detail page scraping ---");

            // --- Step 2: Visit each detail page to get more info ---
            for (Product product : productList) {
                if (product.getProductUrl() == null || product.getProductUrl().isEmpty()) {
                    System.err.println("Skipping detail page for " + product.getName() + " due to missing URL.");
                    continue;
                }

                try {
                    System.out.println("Navigating to detail page: " + product.getProductUrl());
                    driver.get(product.getProductUrl());

                    // --- !! Use WebDriverWait extensively here !! ---

                    // Extract Description
                    try {
                        WebElement descriptionElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(DETAIL_DESCRIPTION_SELECTOR)));
                        product.setDescription(descriptionElement.getText().trim());
                    } catch (TimeoutException | NoSuchElementException descEx) {
                        System.err.println("Description not found for: " + product.getName());
                        product.setDescription(null);
                    }

                    // Extract Specifications (Example: if they are in a table)
                    Map<String, String> specs = new HashMap<>();
                    try {
                        List<WebElement> specRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(DETAIL_SPECS_TABLE_SELECTOR)));
                        for (WebElement row : specRows) {
                            try {
                                List<WebElement> cells = row.findElements(By.tagName("td")); // Assuming 2 cells per row (Key, Value)
                                if (cells.size() >= 2) {
                                    String key = cells.get(0).getText().trim();
                                    String value = cells.get(1).getText().trim();
                                    if (!key.isEmpty()) {
                                        specs.put(key, value);
                                    }
                                }
                            } catch (Exception specRowEx) {
                                System.err.println("Error parsing spec row for " + product.getName());
                            }
                        }
                        product.setSpecifications(specs);
                    } catch (TimeoutException | NoSuchElementException specEx) {
                        System.err.println("Specifications table/rows not found for: " + product.getName());
                        product.setSpecifications(new HashMap<>()); // Set empty map
                    }

                    // Extract Rating and Review Count (Add similar try-catch blocks)
                    // try {
                    //      WebElement ratingElement = wait.until(... DETAIL_RATING_SELECTOR ...);
                    //      product.setOverallRating(Double.parseDouble(ratingElement.getText().replace(",", "."))); // Adjust parsing
                    // } catch(...) {}
                    // try {
                    //      WebElement reviewCountElement = wait.until(... DETAIL_REVIEW_COUNT_SELECTOR ...);
                    //      product.setReviewCount(Integer.parseInt(reviewCountElement.getText().replaceAll("[^\\d]", ""))); // Adjust parsing
                    // } catch(...) {}

                    System.out.println("Finished scraping details for: " + product.getName());

                    // --- !! ETHICAL SCRAPING: Add a delay between detail page loads !! ---
                    Thread.sleep(500); // 500 milliseconds (adjust)

                } catch (TimeoutException e) {
                    System.err.println("Timeout waiting for elements on detail page: " + product.getProductUrl() + " - Error: " + e.getMessage());
                } catch (NoSuchElementException e) {
                    System.err.println("Element not found on detail page: " + product.getProductUrl() + " - Error: " + e.getMessage());
                } catch (Exception e) { // Catch broader exceptions during detail page processing
                    System.err.println("Error processing detail page " + product.getProductUrl() + ": " + e.getMessage());
                }
            }

        } catch (TimeoutException e) {
            System.err.println("Timeout waiting for initial product containers on " + BASE_URL + ". Page might not have loaded correctly or selector is wrong.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during scraping: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }

        return productList;
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            System.out.println("WebDriver closed.");
        }
    }

    // --- Main Method for Testing ---
    public static void main(String[] args) {
        WebScraper scraper = new WebScraper();
        List<Product> products = null;
        try {
            products = scraper.scrapeProducts();
            System.out.println("\n------------------------------------");
            System.out.println("Total products scraped: " + (products != null ? products.size() : 0));
            System.out.println("------------------------------------");

            // Now you would typically pass 'products' to your JSON saving logic
            // Example: saveToJson(products, "cellphones_products.json");
            // --- Section to add: Writing the list to JSON ---
            if (products != null && !products.isEmpty()) {
                // Create a Gson object (use GsonBuilder for pretty printing)
                Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Makes the JSON readable

                // Define the output file path (relative to project root usually)
                String filePath = "products.json";

                System.out.println("Attempting to write JSON file to: " + filePath); // Debug output

                // Use try-with-resources for FileWriter to ensure it's closed
                try (FileWriter writer = new FileWriter(filePath)) {
                    // Convert the list directly to JSON and write it
                    gson.toJson(products, writer); // Efficiently writes directly to the writer
                    System.out.println("Successfully wrote product data to " + filePath);
                } catch (IOException e) {
                    System.err.println("Error writing JSON file '" + filePath + "': " + e.getMessage());
                    // Optionally print stack trace for more detail
                    // e.printStackTrace();
                } catch (Exception e) {
                    // Catch any other unexpected errors during JSON processing/writing
                    System.err.println("An unexpected error occurred during JSON writing: " + e.getMessage());
                    // e.printStackTrace();
                }
            } else {
                System.out.println("Product list is empty or null. No JSON file written.");
            }
            // --- End of section to add ---

            if (products != null && !products.isEmpty()) {
                System.out.println("\nSample product data:");
                System.out.println(products.get(0)); // Print details of the first product
                if (products.get(0).getSpecifications() != null) {
                    System.out.println("Specifications sample:");
                    products.get(0).getSpecifications().forEach((k, v) -> System.out.println("  " + k + ": " + v));
                }
            }

        } finally {
            scraper.quitDriver(); // Ensure the browser closes even if errors occur
        }
    }
}