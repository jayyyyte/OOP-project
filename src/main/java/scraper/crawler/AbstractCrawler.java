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

import java.net.URI;
import java.net.URISyntaxException;
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
                    if (detailUrl != null && !detailUrl.isEmpty()) {
                        String resolvedUrl = resolveUrl(detailUrl);
                        product.setProductUrl(resolvedUrl);
                        detailUrls.add(resolvedUrl);
                    } else {
                        System.err.println("No detail URL found for product: " + product.getName());
                        product.setProductUrl(null);
                        detailUrls.add(null);
                    }

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
                String productUrl = product.getProductUrl();
                if (productUrl == null || productUrl.isEmpty() || !productUrl.startsWith("http")) continue;

                try {
                    driver.get(productUrl);
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

                    // SPECIFICATIONS
                    Map<String, String> specs = new HashMap<>();
                    boolean specsFound = false;

                    // APPROACH 1: Try to click "Xem cấu hình chi tiết" button and extract from expanded section
                    // APPROACH 2: Extract specifications from tables
                    // APPROACH 3: Look for alternate specs format (non-table formats)
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

                    // Update status with the collected specifications
                    if (specsFound) {
                        System.out.println("Successfully extracted " + specs.size() + " specifications for: " + product.getName());
                    } else {
                        System.err.println("No specifications found for: " + product.getName());
                    }

                    product.setSpecifications(specs);
                    product.organizeSpecificationsIntoCategories();

                    // RATING

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
// REVIEWS COUNT
                    boolean reviewCountFound = false;

                    // Approach 1: Try direct selectors
                    try {
                        for (String selector : config.getReviewCount().split(",")) {
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
                            for (String selector : config.getReviewContainer().split(",")) {
                                try {
                                    WebElement reviewSection = driver.findElement(By.cssSelector(selector.trim()));
                                    String REVIEW_ITEM_SELECTOR = "div.comment-item, div.item-comment";
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
                    	                    String classes = star.getDomAttribute("class");
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

    protected String resolveUrl(String url) {
        if (url == null || url.isEmpty()) {
            System.err.println("Invalid URL input: " + url);
            return null;
        }
        if (url.startsWith("http")) {
            return url; // Already absolute
        }
        try {
            URI baseUri = new URI(config.getUrl());
            URI resolvedUri = baseUri.resolve(url);
            String resolved = resolvedUri.toString();
            System.out.println("Resolved URL: " + url + " -> " + resolved);
            return resolved;
        } catch (URISyntaxException e) {
            System.err.println("Error resolving URL " + url + ": " + e.getMessage());
            return null;
        }
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
            return element.findElement(By.cssSelector(selector)).getDomAttribute(attribute);
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