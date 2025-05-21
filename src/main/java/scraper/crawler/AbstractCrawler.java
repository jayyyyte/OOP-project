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

                    try {
                        List<WebElement> specTables = driver.findElements(By.cssSelector(config.getSpecsTable()));
                        for (WebElement table : specTables) {
                            List<WebElement> rows = table.findElements(By.cssSelector(config.getSpecsRow()));
                            for (WebElement row : rows) {
                                try {
                                    String label = safeGetText(row, config.getSpecsLabel());
                                    String value = safeGetText(row, config.getSpecsValue());
                                    if (!label.isEmpty() && !value.isEmpty()) {
                                        specs.put(label, value);
                                        specsFound = true;
                                    }
                                } catch (Exception e) {}
                            }
                        }
                    } catch (Exception e) {}

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

                    product.setSpecifications(specs);
                    product.organizeSpecificationsIntoCategories();

                    // Rating
                    double rating = 0.0;
                    for (String selector : config.getRating().split(",")) {
                        try {
                            String ratingText = safeGetText(driver, selector.trim());
                            if (ratingText.contains("/")) {
                                ratingText = ratingText.split("/")[0].trim();
                            }
                            ratingText = ratingText.replaceAll("[^0-9.,]", "").replace(",", ".");
                            if (!ratingText.isEmpty()) {
                                rating = Double.parseDouble(ratingText);
                                break;
                            }
                        } catch (Exception e) {}
                    }
                    product.setOverallRating(rating);

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