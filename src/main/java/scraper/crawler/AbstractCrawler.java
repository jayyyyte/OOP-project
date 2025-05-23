package scraper.crawler;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import scraper.config.WebsiteConfig;
import scraper.factory.ProductFactory;
import scraper.model.Product;
import scraper.model.Review;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractCrawler implements Crawler {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected WebsiteConfig config;
    protected ProductFactory factory;

    public AbstractCrawler(WebsiteConfig config, ProductFactory factory) {
        this.config = config;
        this.factory = factory;
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        this.driver = new ChromeDriver(options);
        this.driver.manage().window().maximize();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Override
    public List<Product> crawl(int maxProducts) {
        List<Product> productList = new ArrayList<>();
        driver.get(config.getUrl());

        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(config.getProductContainer())));

            List<WebElement> productContainers = driver.findElements(By.cssSelector(config.getProductContainer()));
            System.out.println("Found " + productContainers.size() + " products on " + config.getUrl());

            List<String> detailUrls = new ArrayList<>();
            for (WebElement container : productContainers) {
                if (productList.size() >= maxProducts) break;

                try {
                    Product product = new Product();

                    product.setName(safeGetText(container, config.getName()));
                    String detailUrl = safeGetAttribute(container, config.getDetailUrl(), "href");
                    product.setProductUrl(detailUrl);
                    detailUrls.add(detailUrl);

                    try {
                        product.setPrice(Product.parsePrice(safeGetText(container, config.getPrice())));
                    } catch (Exception e) {
                        product.setPrice(0.0);
                    }

                    String imgSrc = safeGetAttribute(container, config.getImage(), "src");
                    if (imgSrc == null || imgSrc.isEmpty()) {
                        imgSrc = safeGetAttribute(container, config.getImage(), "data-src");
                    }
                    product.setImageUrl(imgSrc);

                    // Set the category from the config
                    product.getCategoryData().put("category", config.getTargetCategory());

                    productList.add(product);
                    Thread.sleep(150);
                } catch (Exception e) {
                    System.err.println("Error scraping product: " + e.getMessage());
                }
            }

            for (Product product : productList) {
                if (product.getProductUrl() == null) continue;

                try {
                    driver.get(product.getProductUrl());
                    Thread.sleep(1000);

                    // Description
                    String description = "No description available";
                    for (String selector : config.getDescription().split(",")) {
                        try {
                            List<WebElement> descElements = driver.findElements(By.cssSelector(selector.trim()));
                            if (!descElements.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                for (WebElement element : descElements) {
                                    String text = element.getText().trim();
                                    if (!text.isEmpty()) {
                                        sb.append(text).append("\n\n");
                                    }
                                }
                                if (sb.length() > 0) {
                                    description = sb.toString().trim();
                                    break;
                                }
                            }
                        } catch (Exception e) {}
                    }
                    product.setDescription(description);

                    // Specifications
                    Map<String, String> specs = new HashMap<>();
                    boolean specsFound = false;

                    // APPROACH 1: Try to click "Xem cấu hình chi tiết" button and extract from expanded section
/*                    try {
                        System.out.println("Looking for specifications button...");
                        // Attempt to find and click on the specifications button with all possible selectors
                        List<WebElement> specsButtons = driver.findElements(By.cssSelector(config.getSpecsButton()));

                        boolean buttonClicked = false;
                        for (WebElement button : specsButtons) {
                            try {
                                if (button.isDisplayed() &&
                                        (button.getText().toLowerCase().contains("cấu hình") ||
                                                button.getText().toLowerCase().contains("thông số") ||
                                                button.getDomAttribute("textContent").toLowerCase().contains("cấu hình"))) {

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
                        List<WebElement> specTables = driver.findElements(By.cssSelector(config.getSpecsTable()));

                        for (WebElement table : specTables) {
                            try {
                                System.out.println("Found a specification table");
                                List<WebElement> rows = table.findElements(By.cssSelector(config.getSpecsRow()));

                                for (WebElement row : rows) {
                                    try {
                                        WebElement labelElement = row.findElement(By.cssSelector(config.getSpecsLabel()));
                                        WebElement valueElement = row.findElement(By.cssSelector(config.getSpecsValue()));

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

                /*    // APPROACH 3: Look for alternate specs format (non-table formats)
                    if (!specsFound) {
                        System.out.println("Table approach failed, trying alternative specs format...");
                        try {
                            List<WebElement> specContainers = driver.findElements(By.cssSelector(config.getspe));

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
                */

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

                    // Rating
                    double rating = 0.0;
                    boolean ratingFound = false;

                    // Approach 1: Direct rating display like "4.0/5"
         /*           try {
                        for (String selector : config.getRating().split(",")) {
                            try {
                                WebElement ratingElement = driver.findElement(By.cssSelector(selector.trim()));
                                String ratingText = ratingElement.getText().trim();
                              
                                // Extract number before "/5" if present
                                if (ratingText.contains("/")) {
                                    ratingText = ratingText.split("/")[0].trim();
                                }

                                // Remove any non-numeric chars except decimal point
                                ratingText = ratingText.replaceAll("[^0-9.,]", "").replace(",", ".");

                                if (!ratingText.isEmpty()) {
                                    double rating1 = Double.parseDouble(ratingText);
                                    product.setOverallRating(rating1);

                                    ratingFound = true;
                                    break;
                                }
                            } catch (Exception e) {
                                // Continue to next selector
                            }
                        }
                    } catch (Exception e) {
              
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
                                        String classes = star.getDomAttribute("class") + " " + star.getCssValue("color");
                                        if (classes.contains("fa-solid") || classes.contains("active") ||
                                            classes.contains("checked") || classes.contains("fas fa-star") ||
                                            classes.contains("rgb(255, 193, 7)") || classes.contains("#ffc107")) {
                                            filledStars++;
                                        }
                                    }

                                    if (totalStars > 0) {
                                        double rating1 = (double) filledStars;
                                        product.setOverallRating(rating1);

                                        ratingFound = true;
                                        break;
                                    }
                                }
                            }
                        } catch (Exception sEx) {
 
                        }
                    }
*/
                    // Approach 3: Look for text containing rating information
                    if (!ratingFound) {
                        try {
                            // Get page source and search for rating patterns
                            String pageSource = driver.getPageSource().toLowerCase();
                            Pattern ratingPattern = Pattern.compile("([0-9],[0-9]|[0-9]\\.[0-9])/5");
                            Matcher matcher = ratingPattern.matcher(pageSource);

                            if (matcher.find()) {
                                String ratingStr = matcher.group(1).replace(",", ".");
                                double rating1 = Double.parseDouble(ratingStr);
                                product.setOverallRating(rating1);
         
                                ratingFound = true;
                            }
                        } catch (Exception e) {
             
                        }
                    }

                    if (!ratingFound) {
                        
                        product.setOverallRating(0.0);
                    }

                

                    // Review Count
                    int reviewCount = 0;
                    for (String selector : config.getReviewCount().split(",")) {
                        try {
                            String countText = safeGetText(driver, selector.trim());
                            Pattern pattern = Pattern.compile("\\d+");
                            Matcher matcher = pattern.matcher(countText);
                            if (matcher.find()) {
                                reviewCount = Integer.parseInt(matcher.group());
                                break;
                            }
                        } catch (Exception e) {}
                    }

                    // Reviews
                    List<Map<String, String>> reviews = new ArrayList<>();
                    if (reviewCount > 0) {
                        try {
                            List<WebElement> reviewItems = driver.findElements(By.cssSelector(config.getReviewContainer()));
                            for (WebElement item : reviewItems.subList(0, Math.min(reviewItems.size(), 10))) {
                                Map<String, String> review = new HashMap<>();
                                try {
                                    String author = safeGetText(item, config.getReviewAuthor());
                                    review.put("author", author.isEmpty() ? "Anonymous" : author);

                                    String content = safeGetText(item, config.getReviewText());
                                    review.put("content", content);

                                    String ratingText = safeGetAttribute(item, config.getReviewRating(), "data-rating");
                                    review.put("rating", ratingText.isEmpty() ? "0" : ratingText);

                                    String date = safeGetText(item, config.getReviewDate());
                                    review.put("date", date);

                                    String verified = item.getText().contains("Đã mua tại") ? "true" : "false";
                                    review.put("verified_purchase", verified);

                                    reviews.add(review);
                                } catch (Exception e) {}
                            }
                        } catch (Exception e) {}
                    }
                    product.setReviewCount(reviews.size());
                    product.addCategoryData("reviews", reviews);

                    Thread.sleep(500);
                } catch (Exception e) {
                    System.err.println("Error on detail page " + product.getProductUrl() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error crawling " + config.getUrl() + ": " + e.getMessage());
        }

        return productList;
    }

    protected String safeGetText(WebElement element, String selector) {
        try {
            return element.findElement(By.cssSelector(selector)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    protected String safeGetText(WebDriver driver, String selector) {
        try {
            return driver.findElement(By.cssSelector(selector)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    protected String safeGetAttribute(WebElement element, String selector, String attribute) {
        try {
            return element.findElement(By.cssSelector(selector)).getAttribute(attribute);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.quit();
            System.out.println("WebDriver closed for " + config.getUrl());
        }
    }
}