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
                    if (!specsFound) {
                        try {
                            List<WebElement> specRows = driver.findElements(By.cssSelector(config.getAltSpecsRow()));
                            for (WebElement row : specRows) {
                                String text = row.getText().trim();
                                if (text.contains(":")) {
                                    String[] parts = text.split(":", 2);
                                    if (parts.length == 2) {
                                        specs.put(parts[0].trim(), parts[1].trim());
                                        specsFound = true;
                                    }
                                }
                            }
                        } catch (Exception e) {}
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

                    // Update status with the collected specifications
                    if (specsFound) {
                        System.out.println("Successfully extracted " + specs.size() + " specifications for: " + product.getName());
                    } else {
                        System.err.println("No specifications found for: " + product.getName());
                    }

                    product.setSpecifications(specs);
                    product.organizeSpecificationsIntoCategories();

                    // RATING
                    //Approach 3: Look for text containing rating information
                    double rating = 0.0;
                    boolean ratingFound = false;
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

                    // REVIEW COUNT
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