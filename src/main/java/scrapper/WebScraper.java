package scrapper;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Selectors relative to the PRODUCT_CONTAINER_SELECTOR
    private static final String NAME_SELECTOR = "h3";
    private static final String PRICE_SELECTOR = "p.product__price--show";
    private static final String IMAGE_SELECTOR = "div.product__image img";
    private static final String DETAIL_URL_SELECTOR = "a";

    // Selectors for the product detail page
    private static final String DETAIL_DESCRIPTION_SELECTOR = "div.desktop";
    private static final String DETAIL_SPECS_SELECTORS = "ul.technical-content li";

    // Updated selectors for specifications
    private static final String SPECS_BUTTON_SELECTOR = "div.specifications-button a, button.show-specifications, a.show-configuration";
    private static final String SPECS_TABLE_SELECTOR = "table.box-content__table, div.specifications-content table, div.technical-content table";
    private static final String SPECS_ROW_SELECTOR = "tr";
    private static final String SPECS_LABEL_SELECTOR = "td:first-child, th:first-child";
    private static final String SPECS_VALUE_SELECTOR = "td:last-child, th:last-child";

    // Alternative selectors for specifications in case the table format is different
    private static final String ALT_SPECS_CONTAINER_SELECTOR = "div.technical-content, div.box01-item";
    private static final String ALT_SPECS_ROW_SELECTOR = "p, div.fs-dt-item";

    // Rating and Review selectors - updated with correct selectors for the website
    private static final String DETAIL_RATING_SELECTOR = "div.seller-overview-rating, div.rating-overview strong";
    private static final String DETAIL_REVIEW_COUNT_SELECTOR = "div.seller-overview-rating + a, div.rating-overview + a";

    // Additional selectors for review content
    private static final String REVIEWS_CONTAINER_SELECTOR = "div.comment-list, div.list-comment";
    private static final String REVIEW_ITEM_SELECTOR = "div.comment-item, div.item-comment";
    private static final String REVIEW_AUTHOR_SELECTOR = "div.comment-user-name strong";
    private static final String REVIEW_TEXT_SELECTOR = "div.comment-content";
    private static final String REVIEW_DATE_SELECTOR = "div.comment-time";

    public WebScraper() {
        WebDriverManager.chromedriver().setup();
        // Optional: Run headless (without opening a visible browser window)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        this.driver = new ChromeDriver(options);
        // this.driver = new ChromeDriver(); // if you want visible browser
        this.driver.manage().window().maximize();
        // Set a default wait time for elements to appear
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

                    // Extract Detail URL
                    WebElement detailLinkElement = container.findElement(By.cssSelector(DETAIL_URL_SELECTOR));
                    String detailUrl = detailLinkElement.getAttribute("href");
                    product.setProductUrl(detailUrl);
                    detailUrls.add(detailUrl);

                    // Extract Price
                    try {
                        WebElement priceElement = container.findElement(By.cssSelector(PRICE_SELECTOR));
                        product.setPrice(Product.parsePrice(priceElement.getText()));
                    } catch (NoSuchElementException pEx) {
                        System.err.println("Price not found for product: " + product.getName());
                        product.setPrice(0.0);
                    }

                    // Extract Image URL
                    try {
                        WebElement imageElement = container.findElement(By.cssSelector(IMAGE_SELECTOR));
                        String imgSrc = imageElement.getAttribute("src");
                        if (imgSrc == null || imgSrc.isEmpty()) {
                            imgSrc = imageElement.getAttribute("data-src"); // For lazy-loaded images
                        }
                        product.setImageUrl(imgSrc);
                    } catch (NoSuchElementException imgEx) {
                        System.err.println("Image not found for product: " + product.getName());
                        product.setImageUrl(null);
                    }

                    productList.add(product);
                    System.out.println("Scraped basic info for: " + product.getName());

                    // Ethical scraping: Add a small delay
                    Thread.sleep(150);

                } catch (NoSuchElementException e) {
                    System.err.println("Could not find an element within a product container. Skipping item. Error: " + e.getMessage());
                } catch (StaleElementReferenceException e) {
                    System.err.println("Element became stale. Skipping item. Error: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Scraping interrupted.");
                    break;
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

                    // Add a delay to ensure page loads completely
                    Thread.sleep(1000);

                    // Extract Description - Try multiple selectors
                    boolean descriptionFound = false;
                    String[] descriptionSelectors = {
                        "div.block-technical-content",
                        "div.block-content",
                        "div.product-description",
                        "div.product-detail-description",
                        "div.product__content",
                        "section.product-specs",
                        "div.product-description-content",
                            "div.desktop",
                            "div.ksp-content.p-2.mb-2",
                            "div.product__desc",
                            "meta[name='description']"
                    };

                    for (String selector : descriptionSelectors) {
                        try {
                            if (selector.equals("meta[name='description']")) {
                                // Special case for meta description
                                WebElement metaDesc = driver.findElement(By.cssSelector(selector));
                                String content = metaDesc.getAttribute("content");
                                if (content != null && !content.isEmpty()) {
                                    product.setDescription(content);
                                    System.out.println("Found description using meta tag");
                                    descriptionFound = true;
                                    break;
                                }
                                continue;
                            }

                            List<WebElement> descElements = driver.findElements(By.cssSelector(selector));
                            if (!descElements.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                for (WebElement element : descElements) {
                                    String text = element.getText().trim();
                                    if (!text.isEmpty()) {
                                        sb.append(text).append("\n\n");
                                    }
                                }

                                if (sb.length() > 0) {
                                    product.setDescription(sb.toString().trim());
                                    System.out.println("Found description using selector: " + selector);
                                    descriptionFound = true;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            // Try next selector
                        }
                    }

                    // If still not found, try a more generic approach
                    if (!descriptionFound) {
                        try {
                            // Try to find any div with substantial text content in the main product area
                            List<WebElement> contentDivs = driver.findElements(By.cssSelector("div.product-detail div, div.product-container div"));

                            for (WebElement div : contentDivs) {
                                String text = div.getText().trim();
                                // Look for divs with substantial text (more than 100 chars)
                                if (text.length() > 100) {
                                    product.setDescription(text);
                                    System.out.println("Found description using generic approach, length: " + text.length());
                                    descriptionFound = true;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error in generic description approach: " + e.getMessage());
                        }
                    }

                    if (!descriptionFound) {
                        System.err.println("Description not found for: " + product.getName());
                        product.setDescription("No description available");
                    }

                    // Extract Specifications
                    Map<String, String> specs = new HashMap<>();
                    boolean specsFound = false;

                    // APPROACH 1: Try to click "Xem cấu hình chi tiết" button and extract from expanded section
                    try {
                        System.out.println("Looking for specifications button...");
                        // Attempt to find and click on the specifications button with all possible selectors
                        List<WebElement> specsButtons = driver.findElements(By.cssSelector(SPECS_BUTTON_SELECTOR));

                        boolean buttonClicked = false;
                        for (WebElement button : specsButtons) {
                            try {
                                if (button.isDisplayed() &&
                                        (button.getText().toLowerCase().contains("cấu hình") ||
                                                button.getText().toLowerCase().contains("thông số") ||
                                                button.getAttribute("textContent").toLowerCase().contains("cấu hình"))) {

                                    System.out.println("Found specs button: " + button.getText());
                                    // Scroll to the button
                                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
                                    Thread.sleep(300);

                                    // Try clicking normally first
                                    try {
                                        button.click();
                                        buttonClicked = true;
                                        System.out.println("Clicked specifications button");
                                    } catch (Exception e) {
                                        // If normal click fails, try JavaScript click
                                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                                        buttonClicked = true;
                                        System.out.println("Clicked specifications button via JavaScript");
                                    }

                                    // Wait for specs to load
                                    Thread.sleep(500);
                                    break;
                                }
                            } catch (Exception e) {
                                System.err.println("Error clicking a specs button: " + e.getMessage());
                            }
                        }

                        // If button not found or clicked, try JavaScript approach
                        if (!buttonClicked) {
                            System.out.println("Button not found/clicked normally, trying JavaScript approach");
                            // Find a more generic button that might open specs
                            List<WebElement> possibleButtons = driver.findElements(
                                    By.xpath("//button[contains(text(), 'cấu hình') or contains(text(), 'thông số')] | " +
                                            "//a[contains(text(), 'cấu hình') or contains(text(), 'thông số')]"));

                            for (WebElement btn : possibleButtons) {
                                try {
                                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                                    buttonClicked = true;
                                    System.out.println("Clicked button via JavaScript: " + btn.getText());
                                    Thread.sleep(500);
                                    break;
                                } catch (Exception e) {
                                    // Continue to next button
                                }
                            }
                        }

                        // If still no button found, look for elements with click handlers
                        if (!buttonClicked) {
                            System.out.println("No buttons found, looking for clickable elements with specs text");
                            List<WebElement> possibleElements = driver.findElements(
                                    By.xpath("//*[contains(text(), 'cấu hình chi tiết') or contains(text(), 'thông số kỹ thuật')]"));

                            for (WebElement element : possibleElements) {
                                try {
                                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                                    System.out.println("Clicked element: " + element.getText());
                                    Thread.sleep(500);
                                    break;
                                } catch (Exception e) {
                                    // Continue to next element
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error finding/clicking specs button: " + e.getMessage());
                    }

                    // APPROACH 2: Extract specifications from tables
                    System.out.println("Looking for specification tables...");
                    try {
                        List<WebElement> specTables = driver.findElements(By.cssSelector(SPECS_TABLE_SELECTOR));

                        for (WebElement table : specTables) {
                            try {
                                System.out.println("Found a specification table");
                                List<WebElement> rows = table.findElements(By.cssSelector(SPECS_ROW_SELECTOR));

                                for (WebElement row : rows) {
                                    try {
                                        WebElement labelElement = row.findElement(By.cssSelector(SPECS_LABEL_SELECTOR));
                                        WebElement valueElement = row.findElement(By.cssSelector(SPECS_VALUE_SELECTOR));

                                        String label = labelElement.getText().trim();
                                        String value = valueElement.getText().trim();

                                        if (!label.isEmpty() && !value.isEmpty()) {
                                            specs.put(label, value);
                                            specsFound = true;
                                        }
                                    } catch (Exception rowEx) {
                                        // Skip this row and continue to next
                                    }
                                }
                            } catch (Exception tableEx) {
                                System.err.println("Error processing a spec table: " + tableEx.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error finding specification tables: " + e.getMessage());
                    }

                    // APPROACH 3: Look for alternate specs format (non-table formats)
                    if (!specsFound) {
                        System.out.println("Table approach failed, trying alternative specs format...");
                        try {
                            List<WebElement> specContainers = driver.findElements(By.cssSelector(ALT_SPECS_CONTAINER_SELECTOR));

                            for (WebElement container : specContainers) {
                                List<WebElement> specRows = container.findElements(By.cssSelector(ALT_SPECS_ROW_SELECTOR));

                                for (WebElement row : specRows) {
                                    String rowText = row.getText().trim();

                                    // Try to parse spec rows in format "Label: Value"
                                    if (rowText.contains(":")) {
                                        String[] parts = rowText.split(":", 2);
                                        if (parts.length == 2) {
                                            String key = parts[0].trim();
                                            String value = parts[1].trim();
                                            if (!key.isEmpty() && !value.isEmpty()) {
                                                specs.put(key, value);
                                                specsFound = true;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error in alternative specs approach: " + e.getMessage());
                        }
                    }

                    // APPROACH 4: Try to find specs directly in the HTML
                    if (!specsFound) {
                        System.out.println("Previous approaches failed, trying direct HTML inspection...");
                        try {
                            // Get the page source
                            String pageSource = driver.getPageSource();

                            // Search for specification section HTML patterns
                            Pattern specPattern = Pattern.compile("<td[^>]*>(.*?)</td>\\s*<td[^>]*>(.*?)</td>");
                            Matcher matcher = specPattern.matcher(pageSource);

                            while (matcher.find()) {
                                String key = matcher.group(1).replaceAll("<[^>]*>", "").trim();
                                String value = matcher.group(2).replaceAll("<[^>]*>", "").trim();

                                if (!key.isEmpty() && !value.isEmpty()) {
                                    specs.put(key, value);
                                    specsFound = true;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error in direct HTML approach: " + e.getMessage());
                        }
                    }

                    // APPROACH 5: Look for specification elements by their text content
                    if (!specsFound) {
                        System.out.println("Trying to find specs by text content...");
                        try {
                            // Look for common spec categories in Vietnamese phones
                            String[] specCategories = {
                                    "Màn hình", "CPU", "RAM", "Bộ nhớ trong", "Camera sau",
                                    "Camera trước", "Pin", "Hệ điều hành", "Độ phân giải"
                            };

                            for (String category : specCategories) {
                                try {
                                    List<WebElement> elements = driver.findElements(
                                            By.xpath("//*[contains(text(), '" + category + "')]"));

                                    for (WebElement element : elements) {
                                        String text = element.getText().trim();
                                        if (text.contains(":")) {
                                            String[] parts = text.split(":", 2);
                                            if (parts.length == 2) {
                                                specs.put(parts[0].trim(), parts[1].trim());
                                                specsFound = true;
                                            }
                                        } else {
                                            // Try to find the value in a nearby element
                                            try {
                                                WebElement parent = element.findElement(By.xpath("./.."));
                                                WebElement valueElement = parent.findElement(
                                                        By.xpath(".//td[2] | .//span[2] | .//div[2]"));

                                                String value = valueElement.getText().trim();
                                                if (!value.isEmpty()) {
                                                    specs.put(category, value);
                                                    specsFound = true;
                                                }
                                            } catch (Exception nearbyEx) {
                                                // No nearby value element found
                                            }
                                        }
                                    }
                                } catch (Exception catEx) {
                                    // Continue to next category
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error in text content approach: " + e.getMessage());
                        }
                    }

                    // APPROACH 6: Try to extract using JavaScript
                    if (!specsFound) {
                        System.out.println("Trying JavaScript approach...");
                        try {
                            // Try to extract specs data via JavaScript
                            Object result = ((JavascriptExecutor) driver).executeScript(
                                    "const specs = {}; " +
                                            "document.querySelectorAll('table tr').forEach(row => { " +
                                            "  const cells = row.querySelectorAll('td, th'); " +
                                            "  if (cells.length >= 2) { " +
                                            "    const key = cells[0].textContent.trim(); " +
                                            "    const value = cells[1].textContent.trim(); " +
                                            "    if (key && value) specs[key] = value; " +
                                            "  } " +
                                            "}); " +
                                            "return specs;"
                            );

                            if (result instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> jsSpecs = (Map<String, String>) result;
                                specs.putAll(jsSpecs);
                                if (!jsSpecs.isEmpty()) {
                                    specsFound = true;
                                    System.out.println("Found " + jsSpecs.size() + " specifications via JavaScript");
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error in JavaScript approach: " + e.getMessage());
                        }
                    }

                    // Update the product with the collected specifications
                    if (specsFound) {
                        System.out.println("Successfully extracted " + specs.size() + " specifications for: " + product.getName());
                        specs.forEach((k, v) -> System.out.println("  - " + k + ": " + v));
                    } else {
                        System.err.println("No specifications found for: " + product.getName());
                    }

                    product.setSpecifications(specs);
                    product.organizeSpecificationsIntoCategories();

                    // Extract Rating - Multiple approaches for different page formats
                    boolean ratingFound = false;

                    // Approach 1: Direct rating display like "4.0/5"
                    try {
                        for (String selector : DETAIL_RATING_SELECTOR.split(",")) {
                            try {
                                WebElement ratingElement = driver.findElement(By.cssSelector(selector.trim()));
                                String ratingText = ratingElement.getText().trim();
                                System.out.println("Found rating text: " + ratingText);

                                // Extract number before "/5" if present
                                if (ratingText.contains("/")) {
                                    ratingText = ratingText.split("/")[0].trim();
                                }

                                // Remove any non-numeric chars except decimal point
                                ratingText = ratingText.replaceAll("[^0-9.,]", "").replace(",", ".");

                                if (!ratingText.isEmpty()) {
                                    double rating = Double.parseDouble(ratingText);
                                    product.setOverallRating(rating);
                                    System.out.println("Successfully parsed rating: " + rating);
                                    ratingFound = true;
                                    break;
                                }
                            } catch (Exception e) {
                                // Continue to next selector
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error in approach 1 for rating: " + e.getMessage());
                    }

                    // Approach 2: Count filled stars if approach 1 failed
                    if (!ratingFound) {
                        try {
                            // Try multiple possible star selectors
                            String[] starSelectors = {
                                "div.rating i.fa-star",
                                "div.rating span.fa",
                                "div.rating-stars i",
                                "div.rating-overview i.fas"
                            };

                            for (String selector : starSelectors) {
                                List<WebElement> starElements = driver.findElements(By.cssSelector(selector));
                                if (!starElements.isEmpty()) {
                                    int filledStars = 0;
                                    int totalStars = starElements.size();

                                    for (WebElement star : starElements) {
                                        String classes = star.getAttribute("class") + " " + star.getCssValue("color");
                                        if (classes.contains("fa-solid") || classes.contains("active") ||
                                            classes.contains("checked") || classes.contains("fas fa-star") ||
                                            classes.contains("rgb(255, 193, 7)") || classes.contains("#ffc107")) {
                                            filledStars++;
                                        }
                                    }

                                    if (totalStars > 0) {
                                        double rating = (double) filledStars;
                                        product.setOverallRating(rating);
                                        System.out.println("Found rating via stars: " + rating + " out of " + totalStars);
                                        ratingFound = true;
                                        break;
                                    }
                                }
                            }
                        } catch (Exception sEx) {
                            System.err.println("Rating not found using star method: " + sEx.getMessage());
                        }
                    }

                    // Approach 3: Look for text containing rating information
                    if (!ratingFound) {
                        try {
                            // Get page source and search for rating patterns
                            String pageSource = driver.getPageSource().toLowerCase();
                            Pattern ratingPattern = Pattern.compile("([0-9],[0-9]|[0-9]\\.[0-9])/5");
                            Matcher matcher = ratingPattern.matcher(pageSource);

                            if (matcher.find()) {
                                String ratingStr = matcher.group(1).replace(",", ".");
                                double rating = Double.parseDouble(ratingStr);
                                product.setOverallRating(rating);
                                System.out.println("Found rating via regex: " + rating);
                                ratingFound = true;
                            }
                        } catch (Exception e) {
                            System.err.println("Rating not found using regex: " + e.getMessage());
                        }
                    }

                    if (!ratingFound) {
                        System.err.println("Could not find rating for: " + product.getName());
                        product.setOverallRating(0.0);
                    }

                    // Extract Review Count - Multiple approaches
                    boolean reviewCountFound = false;

                    // Approach 1: Try direct selectors
                    try {
                        for (String selector : DETAIL_REVIEW_COUNT_SELECTOR.split(",")) {
                            try {
                                WebElement reviewCountElement = driver.findElement(By.cssSelector(selector.trim()));
                                String reviewCountText = reviewCountElement.getText().trim();
                                System.out.println("Found review count text: " + reviewCountText);

                                // Extract numbers only
                                Pattern pattern = Pattern.compile("\\d+");
                                Matcher matcher = pattern.matcher(reviewCountText);
                                if (matcher.find()) {
                                    int reviewCount = Integer.parseInt(matcher.group());
                                    product.setReviewCount(reviewCount);
                                    System.out.println("Successfully parsed review count: " + reviewCount);
                                    reviewCountFound = true;
                                    break;
                                }
                            } catch (Exception e) {
                                // Continue to next selector
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error in approach 1 for review count: " + e.getMessage());
                    }

                    // Approach 2: Look for review links with counts
                    if (!reviewCountFound) {
                        try {
                            String[] reviewLinkSelectors = {
                                "a:contains(đánh giá), a:contains(nhận xét), a:contains(review)",
                                "div.rating-overview + a",
                                "a.rating-link"
                            };

                            for (String xpathSelector : reviewLinkSelectors) {
                                try {
                                    List<WebElement> elements = driver.findElements(By.xpath(
                                        "//a[contains(text(),'đánh giá') or contains(text(),'nhận xét') or contains(text(),'review')]"));

                                    for (WebElement element : elements) {
                                        String text = element.getText().trim();
                                        Pattern pattern = Pattern.compile("\\d+");
                                        Matcher matcher = pattern.matcher(text);
                                        if (matcher.find()) {
                                            int reviewCount = Integer.parseInt(matcher.group());
                                            product.setReviewCount(reviewCount);
                                            System.out.println("Found review count via links: " + reviewCount);
                                            reviewCountFound = true;
                                            break;
                                        }
                                    }

                                    if (reviewCountFound) break;

                                } catch (Exception e) {
                                    // Try next selector
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error in approach 2 for review count: " + e.getMessage());
                        }
                    }

                    // Approach 3: Count actual review items
                    if (!reviewCountFound) {
                        try {
                            for (String selector : REVIEWS_CONTAINER_SELECTOR.split(",")) {
                                try {
                                    WebElement reviewSection = driver.findElement(By.cssSelector(selector.trim()));
                                    List<WebElement> reviewItems = reviewSection.findElements(By.cssSelector(REVIEW_ITEM_SELECTOR.split(",")[0]));
                                    if (!reviewItems.isEmpty()) {
                                        product.setReviewCount(reviewItems.size());
                                        System.out.println("Found review count by counting items: " + reviewItems.size());
                                        reviewCountFound = true;
                                        break;
                                    }
                                } catch (Exception e) {
                                    // Try next selector
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error in approach 3 for review count: " + e.getMessage());
                        }
                    }

                    // Approach 4: Check page source for review count patterns
                    if (!reviewCountFound) {
                        try {
                            String pageSource = driver.getPageSource();
                            Pattern pattern = Pattern.compile("(\\d+)\\s+đánh giá");
                            Matcher matcher = pattern.matcher(pageSource);
                            if (matcher.find()) {
                                int reviewCount = Integer.parseInt(matcher.group(1));
                                product.setReviewCount(reviewCount);
                                System.out.println("Found review count via regex: " + reviewCount);
                                reviewCountFound = true;
                            }
                        } catch (Exception e) {
                            System.err.println("Error in approach 4 for review count: " + e.getMessage());
                        }
                    }

                    if (!reviewCountFound) {
                        // Look at the blue link text in your screenshot that says "1 đánh giá"
                        try {
                            WebElement ratingLink = driver.findElement(By.cssSelector("a.text-primary, a.blue-text"));
                            String linkText = ratingLink.getText().trim();
                            Pattern pattern = Pattern.compile("(\\d+)");
                            Matcher matcher = pattern.matcher(linkText);
                            if (matcher.find()) {
                                int reviewCount = Integer.parseInt(matcher.group(1));
                                product.setReviewCount(reviewCount);
                                System.out.println("Found review count via blue link: " + reviewCount);
                                reviewCountFound = true;
                            }
                        } catch (Exception e) {
                            System.err.println("Error in final approach for review count: " + e.getMessage());
                        }
                    }

                    if (!reviewCountFound) {
                        System.err.println("Could not find review count for: " + product.getName());
                        product.setReviewCount(0);
                    }

                    // Collect actual reviews if needed (optional feature)
                    if (product.getReviewCount() > 0) {
                    	List<Map<String, String>> reviews = new ArrayList<>();
                    	try {
                    	    System.out.println("Looking for reviews...");

                    	    // Find review items directly - based on the example image format
                    	    List<WebElement> reviewItems = new ArrayList<>();

                    	    // Try multiple possible selectors for review containers
                    	    String[] reviewContainerSelectors = {
                    	        "div.comment-list",
                    	        "div.list-comment",
                    	        "div.review-list",
                    	        "div.ratings-reviews"
                    	    };

                    	    WebElement reviewContainer = null;
                    	    for (String selector : reviewContainerSelectors) {
                    	        try {
                    	            List<WebElement> containers = driver.findElements(By.cssSelector(selector));
                    	            if (!containers.isEmpty()) {
                    	                reviewContainer = containers.get(0);
                    	                System.out.println("Found review container using: " + selector);
                    	                break;
                    	            }
                    	        } catch (Exception ex) {
                    	            // Try next selector
                    	        }
                    	    }

                    	    // If container found, look for review items within it
                    	    if (reviewContainer != null) {
                    	        // Try various selectors for individual review items
                    	        String[] reviewItemSelectors = {
                    	            "div.comment-item",
                    	            "div.item-comment",
                    	            "div.review-item",
                    	            "> div" // Direct children
                    	        };

                    	        for (String selector : reviewItemSelectors) {
                    	            try {
                    	                List<WebElement> items = reviewContainer.findElements(By.cssSelector(selector));
                    	                if (!items.isEmpty()) {
                    	                    reviewItems = items;
                    	                    System.out.println("Found " + items.size() + " review items using: " + selector);
                    	                    break;
                    	                }
                    	            } catch (Exception ex) {
                    	                // Try next selector
                    	            }
                    	        }
                    	    } else {
                    	        // If no container found, try direct page search
                    	        System.out.println("No review container found, trying direct search...");
                    	        try {
                    	            reviewItems = driver.findElements(By.cssSelector("div[class*='comment-item'], div[class*='review-item']"));
                    	            if (reviewItems.isEmpty()) {
                    	                // Look for elements with user avatars/initials (as seen in the image)
                    	                reviewItems = driver.findElements(By.cssSelector("div:has(> div.avatar), div:has(> span.initial)"));
                    	            }
                    	        } catch (Exception ex) {
                    	            System.err.println("Error in direct review search: " + ex.getMessage());
                    	        }
                    	    }

                    	    System.out.println("Found " + reviewItems.size() + " potential review items");

                    	    // Process each review item (limit to 10 for efficiency)
                    	    int reviewsToProcess = Math.min(reviewItems.size(), 10);
                    	    for (int i = 0; i < reviewsToProcess; i++) {
                    	        WebElement reviewItem = reviewItems.get(i);
                    	        Map<String, String> review = new HashMap<>();

                    	        // Extract author name - look for strong element first (as in the example)
                    	        try {
                    	            WebElement authorElement = reviewItem.findElement(By.cssSelector("strong"));
                    	            String authorName = authorElement.getText().trim();
                    	            if (!authorName.isEmpty()) {
                    	                review.put("author", authorName);
                    	                System.out.println("Found reviewer name: " + authorName);
                    	            } else {
                    	                review.put("author", "Anonymous");
                    	            }
                    	        } catch (Exception e) {
                    	            // Try other author selectors
                    	            try {
                    	                List<WebElement> possibleNameElements = reviewItem.findElements(
                    	                    By.cssSelector("div.user-name, div.comment-user-name, h4, h5"));

                    	                for (WebElement element : possibleNameElements) {
                    	                    String text = element.getText().trim();
                    	                    if (!text.isEmpty() && text.length() < 50) { // Reasonable name length
                    	                        review.put("author", text);
                    	                        break;
                    	                    }
                    	                }

                    	                if (!review.containsKey("author")) {
                    	                    review.put("author", "Anonymous");
                    	                }
                    	            } catch (Exception e2) {
                    	                review.put("author", "Anonymous");
                    	            }
                    	        }

                    	        // Extract rating - look for star elements as in the example
                    	        try {
                    	            // Count filled stars (★) or look for rating text
                    	            List<WebElement> starElements = reviewItem.findElements(
                    	                By.cssSelector("i.fa-star, span.fa, i.fas"));

                    	            if (!starElements.isEmpty()) {
                    	                int filledStars = 0;
                    	                for (WebElement star : starElements) {
                    	                    String classes = star.getAttribute("class");
                    	                    if (classes.contains("fa-solid") || classes.contains("fas") ||
                    	                        classes.contains("active") || classes.contains("checked") ||
                    	                        !classes.contains("fa-star-o")) {
                    	                        filledStars++;
                    	                    }
                    	                }
                    	                if (filledStars > 0) {
                    	                    review.put("rating", String.valueOf(filledStars));
                    	                }
                    	            } else {
                    	                // Try to find rating in text
                    	                String fullText = reviewItem.getText();
                    	                Pattern starPattern = Pattern.compile("([1-5]) sao|([1-5])/5|\\★{1,5}");
                    	                Matcher starMatcher = starPattern.matcher(fullText);
                    	                if (starMatcher.find()) {
                    	                    String match = starMatcher.group(0);
                    	                    if (match.contains("★")) {
                    	                        review.put("rating", String.valueOf(match.length()));
                    	                    } else if (match.contains("/")) {
                    	                        review.put("rating", match.substring(0, 1));
                    	                    } else if (match.contains("sao")) {
                    	                        review.put("rating", match.substring(0, 1));
                    	                    }
                    	                }
                    	            }
                    	        } catch (Exception e) {
                    	            // Rating is optional
                    	        }

                    	        // Extract date - format in example is DD/MM/YYYY
                    	        try {
                    	            // Look for date format text
                    	            String fullText = reviewItem.getText();
                    	            Pattern datePattern = Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4}");
                    	            Matcher dateMatcher = datePattern.matcher(fullText);
                    	            if (dateMatcher.find()) {
                    	                review.put("date", dateMatcher.group(0));
                    	            } else {
                    	                // Try specific selectors
                    	                WebElement dateElement = reviewItem.findElement(
                    	                    By.cssSelector("div.comment-time, div.review-date, span.date"));
                    	                review.put("date", dateElement.getText().trim());
                    	            }
                    	        } catch (Exception e) {
                    	            // Date is optional
                    	        }

                    	        // Extract review content
                    	        try {
                    	            WebElement contentElement = reviewItem.findElement(
                    	                By.cssSelector("div.comment-content, div.review-text, p.review-content"));
                    	            String content = contentElement.getText().trim();
                    	            if (!content.isEmpty()) {
                    	                review.put("content", content);
                    	            }
                    	        } catch (Exception e) {
                    	            // If specific content element not found, extract text excluding author/date
                    	            try {
                    	                String fullText = reviewItem.getText();

                    	                // Remove author and date from text
                    	                if (review.containsKey("author")) {
                    	                    fullText = fullText.replace(review.get("author"), "");
                    	                }
                    	                if (review.containsKey("date")) {
                    	                    fullText = fullText.replace(review.get("date"), "");
                    	                }

                    	                // Remove common UI elements text
                    	                fullText = fullText.replaceAll("Đã mua tại CellphoneS", "");
                    	                fullText = fullText.replaceAll("\\d{1,2}/\\d{1,2}/\\d{4}", "");
                    	                fullText = fullText.replaceAll("\\★{1,5}", "");
                    	                fullText = fullText.replaceAll("\\d sao", "");

                    	                // Clean and trim result
                    	                fullText = fullText.trim();
                    	                if (!fullText.isEmpty()) {
                    	                    review.put("content", fullText);
                    	                }
                    	            } catch (Exception e2) {
                    	                // Content is important, but might be missing
                    	                review.put("content", "");
                    	            }
                    	        }

                    	        // Look for verification badges as in the example ("Đã mua tại CellphoneS")
                    	        try {
                    	            List<WebElement> badges = reviewItem.findElements(
                    	                By.cssSelector("div.badge, span.badge, div.verified-badge"));

                    	            if (!badges.isEmpty()) {
                    	                for (WebElement badge : badges) {
                    	                    String badgeText = badge.getText().trim();
                    	                    if (badgeText.contains("mua tại") || badgeText.contains("verified")) {
                    	                        review.put("verified_purchase", "true");
                    	                        review.put("purchase_location", badgeText);
                    	                        break;
                    	                    }
                    	                }
                    	            } else {
                    	                // Try to find verification text directly in the review
                    	                String reviewText = reviewItem.getText();
                    	                if (reviewText.contains("Đã mua tại")) {
                    	                    review.put("verified_purchase", "true");
                    	                }
                    	            }
                    	        } catch (Exception e) {
                    	            // Verification badge is optional
                    	        }

                    	        // Add the review if we have at least author or content
                    	        if (review.containsKey("author") || review.containsKey("content")) {
                    	            reviews.add(review);
                    	            System.out.println("Added review from: " + review.getOrDefault("author", "Anonymous"));
                    	        }
                    	    }

                    	    // Update review count and add reviews to product
                    	    if (!reviews.isEmpty()) {
                    	        if (product.getReviewCount() < reviews.size()) {
                    	            product.setReviewCount(reviews.size());
                    	        }
                    	        product.addCategoryData("reviews", reviews);
                    	        System.out.println("Added " + reviews.size() + " reviews to product");
                    	    }

                    	} catch (Exception e) {
                    	    System.err.println("Error extracting reviews: " + e.getMessage());
                    	}
                    }

                    System.out.println("Finished scraping details for: " + product.getName());
                    System.out.println("  Rating: " + product.getOverallRating() + ", Reviews: " + product.getReviewCount());

                    // Ethical scraping: Add a delay between detail page loads
                    Thread.sleep(500);

                } catch (TimeoutException e) {
                    System.err.println("Timeout waiting for elements on detail page: " + product.getProductUrl());
                } catch (NoSuchElementException e) {
                    System.err.println("Element not found on detail page: " + product.getProductUrl());
                } catch (Exception e) {
                    System.err.println("Error processing detail page " + product.getProductUrl() + ": " + e.getMessage());
                }
            }

        } catch (TimeoutException e) {
            System.err.println("Timeout waiting for initial product containers. Page might not have loaded correctly or selector is wrong.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during scraping: " + e.getMessage());
            e.printStackTrace();
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

            // Write the list to JSON
            if (products != null && !products.isEmpty()) {
                // Create a Gson object with pretty printing
                Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

                // Define the output file path
                String filePath = "products.json";

                System.out.println("Writing JSON file to: " + filePath);

                try (FileWriter writer = new FileWriter(filePath)) {
                    gson.toJson(products, writer);
                    System.out.println("Successfully wrote product data to " + filePath);
                } catch (IOException e) {
                    System.err.println("Error writing JSON file: " + e.getMessage());
                }
            } else {
                System.out.println("Product list is empty or null. No JSON file written.");
            }

            if (products != null && !products.isEmpty()) {
                System.out.println("\nSample product data:");
                System.out.println(products.get(0));

                if (products.get(0).getSpecifications() != null && !products.get(0).getSpecifications().isEmpty()) {
                    System.out.println("Specifications sample:");
                    products.get(0).getSpecifications().forEach((k, v) ->
                        System.out.println("  " + k + ": " + v)
                    );
                }

                if (products.get(0).getCategoryData() != null && !products.get(0).getCategoryData().isEmpty()) {
                    System.out.println("Category data sample:");
                    products.get(0).getCategoryData().forEach((k, v) ->
                        System.out.println("  " + k + ": " + v)
                    );
                }
            }

        } finally {
            scraper.quitDriver();
        }
    }
}