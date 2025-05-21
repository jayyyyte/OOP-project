package scraper.crawler.laptop;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import scraper.crawler.AbstractCrawler;
import scraper.model.Product;
import scraper.model.Laptop;
import scraper.factory.ProductFactory;
import scraper.factory.LaptopFactory;
import scraper.config.WebsiteConfig;
import scraper.util.CrawlerUtils;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crawler implementation for CellphoneS website specifically for laptop products
 */
public class CellphoneSLCrawler extends AbstractCrawler {
    private static final String BASE_URL = "https://cellphones.com.vn/laptop-tablet.html";

    // Selector configuration - centralized for easy maintenance
    private static final class Selectors {
        // List page selectors
        static final String PRODUCT_CONTAINER = "div.product-info";
        static final String PRODUCT_NAME = "h3";
        static final String PRODUCT_PRICE = "p.product__price--show";
        static final String PRODUCT_IMAGE = "div.product__image img";
        static final String PRODUCT_URL = "a";

        // Detail page selectors
        static final String[] DESCRIPTION_SELECTORS = {
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

        // Specs selectors
        static final String SPECS_BUTTON = "div.specifications-button a, button.show-specifications, a.show-configuration";
        static final String SPECS_TABLE = "table.box-content__table, div.specifications-content table, div.technical-content table";
        static final String SPECS_ROW = "tr";
        static final String SPECS_LABEL = "td:first-child, th:first-child";
        static final String SPECS_VALUE = "td:last-child, th:last-child";
        static final String SPECS_CONTAINER = "div.technical-content, div.box01-item";
        static final String SPECS_ALT_ROW = "p, div.fs-dt-item";

        // Rating and review selectors
        static final String RATING = "div.seller-overview-rating, div.rating-overview strong";
        static final String REVIEW_COUNT = "div.seller-overview-rating + a, div.rating-overview + a";
        static final String REVIEWS_CONTAINER = "div.comment-list, div.list-comment";
        static final String REVIEW_ITEM = "div.comment-item, div.item-comment";
        static final String REVIEW_AUTHOR = "div.comment-user-name strong";
        static final String REVIEW_TEXT = "div.comment-content";
        static final String REVIEW_DATE = "div.comment-time";
    }

    private ProductFactory productFactory;

    public CellphoneSLCrawler(WebsiteConfig config) {
        super(config);
        this.productFactory = new LaptopFactory();
    }

    @Override
    public List<Product> crawlProductList() {
        List<Product> products = new ArrayList<>();

        try {
            driver.get(baseUrl);
            CrawlerUtils.delayForEthicalScraping(1000);

            List<WebElement> productContainers = waitForElements(Selectors.PRODUCT_CONTAINER);
            System.out.println("Found " + productContainers.size() + " laptop products on the page");

            for (WebElement container : productContainers) {
                Product product = extractBasicProductInfo(container);
                if (product != null) {
                    products.add(product);
                    CrawlerUtils.delayForEthicalScraping(150);
                }
            }

        } catch (Exception e) {
            System.err.println("Error crawling laptop product list: " + e.getMessage());
        }

        return products;
    }

    @Override
    public void crawlProductDetails(Product product) {
        try {
            System.out.println("Crawling details for laptop: " + product.getName());
            driver.get(product.getProductUrl());
            CrawlerUtils.delayForEthicalScraping(1500);

            // Extract product description
            extractProductDescription(product);

            // Extract specifications
            extractSpecifications(product);

            // Extract ratings and reviews
            extractRatingsAndReviews(product);

            // Organize specifications into categories
            product.organizeSpecificationsIntoCategories();

            // Extract laptop-specific attributes if applicable
            if (product instanceof Laptop) {
                ((Laptop) product).extractSpecificAttributes();
            }

        } catch (Exception e) {
            System.err.println("Error crawling laptop details: " + e.getMessage());
        }
    }

    private Product extractBasicProductInfo(WebElement container) {
        try {
            // Extract product name
            String name = getTextFromElement(container, Selectors.PRODUCT_NAME);
            if (name.isEmpty()) return null;

            // Extract product URL
            String url = getAttributeFromElement(container, Selectors.PRODUCT_URL, "href");
            if (!CrawlerUtils.isValidUrl(url)) return null;

            // Extract price
            String priceText = getTextFromElement(container, Selectors.PRODUCT_PRICE);
            double price = Product.parsePrice(priceText);

            // Create product using factory
            Product product = productFactory.createProduct(name, url, price);

            // Extract image URL
            String imageUrl = getAttributeFromElement(container, Selectors.PRODUCT_IMAGE, "src");
            if (imageUrl.isEmpty()) {
                imageUrl = getAttributeFromElement(container, Selectors.PRODUCT_IMAGE, "data-src");
            }
            product.setImageUrl(imageUrl);

            System.out.println("Extracted basic info for laptop: " + product.getName());
            return product;

        } catch (Exception e) {
            System.err.println("Error extracting laptop basic info: " + e.getMessage());
            return null;
        }
    }

    private void extractProductDescription(Product product) {
        String description = "";

        // Try multiple selectors for description
        for (String selector : Selectors.DESCRIPTION_SELECTORS) {
            try {
                if (selector.equals("meta[name='description']")) {
                    description = getAttributeFromElement(driver, selector, "content");
                } else {
                    description = getTextFromElements(driver, selector);
                }

                if (!description.isEmpty()) {
                    System.out.println("Found laptop description using: " + selector);
                    break;
                }
            } catch (Exception e) {
                // Try next selector
            }
        }

        product.setDescription(description.isEmpty() ? "No description available" : description);
    }

    private void extractSpecifications(Product product) {
        Map<String, String> specs = new HashMap<>();
        boolean specsFound = false;

        // First try to click specifications button if available
        try {
            clickSpecsButton();
            CrawlerUtils.delayForEthicalScraping(500);
        } catch (Exception e) {
            // Continue even if button isn't found or clickable
        }

        // Extract from specs table
        try {
            List<WebElement> specTables = driver.findElements(By.cssSelector(Selectors.SPECS_TABLE));

            for (WebElement table : specTables) {
                List<WebElement> rows = table.findElements(By.cssSelector(Selectors.SPECS_ROW));

                for (WebElement row : rows) {
                    try {
                        String label = getTextFromElement(row, Selectors.SPECS_LABEL);
                        String value = getTextFromElement(row, Selectors.SPECS_VALUE);

                        if (!label.isEmpty() && !value.isEmpty()) {
                            specs.put(label, value);
                            specsFound = true;
                        }
                    } catch (Exception e) {
                        // Skip this row
                    }
                }
            }
        } catch (Exception e) {
            // Try alternative method
        }

        // If no specs found in tables, try alternative format
        if (!specsFound) {
            try {
                List<WebElement> specContainers = driver.findElements(By.cssSelector(Selectors.SPECS_CONTAINER));

                for (WebElement container : specContainers) {
                    List<WebElement> rows = container.findElements(By.cssSelector(Selectors.SPECS_ALT_ROW));

                    for (WebElement row : rows) {
                        String text = row.getText().trim();

                        // Parse "Label: Value" format
                        if (text.contains(":")) {
                            String[] parts = text.split(":", 2);
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
                // Continue without specs
            }
        }

        if (specsFound) {
            System.out.println("Found " + specs.size() + " laptop specifications");
        } else {
            System.out.println("No laptop specifications found");
        }

        product.setSpecifications(specs);
    }

    private void extractRatingsAndReviews(Product product) {
        // Extract rating
        try {
            String ratingText = getTextFromElement(driver, Selectors.RATING);

            if (!ratingText.isEmpty()) {
                // Clean up rating text and extract number
                if (ratingText.contains("/")) {
                    ratingText = ratingText.split("/")[0].trim();
                }

                ratingText = ratingText.replaceAll("[^0-9.,]", "").replace(",", ".");

                if (!ratingText.isEmpty()) {
                    double rating = Double.parseDouble(ratingText);
                    product.setOverallRating(rating);
                    System.out.println("Found laptop rating: " + rating);
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting laptop rating: " + e.getMessage());
        }

        // Extract review count
        try {
            String countText = getTextFromElement(driver, Selectors.REVIEW_COUNT);

            if (!countText.isEmpty()) {
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(countText);

                if (matcher.find()) {
                    int reviewCount = Integer.parseInt(matcher.group());
                    product.setReviewCount(reviewCount);
                    System.out.println("Found laptop review count: " + reviewCount);

                    // Extract reviews if count > 0
                    if (reviewCount > 0) {
                        extractReviews(product);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting laptop review count: " + e.getMessage());
        }
    }

    private void extractReviews(Product product) {
        List<Map<String, String>> reviews = new ArrayList<>();

        try {
            // Find review container first
            WebElement reviewsContainer = null;
            for (String containerSelector : Selectors.REVIEWS_CONTAINER.split(",")) {
                try {
                    reviewsContainer = driver.findElement(By.cssSelector(containerSelector.trim()));
                    if (reviewsContainer != null) break;
                } catch (Exception e) {
                    // Try next selector
                }
            }

            if (reviewsContainer == null) {
                System.out.println("No review container found for laptop");
                return;
            }

            // Find review items
            List<WebElement> reviewItems = new ArrayList<>();
            for (String itemSelector : Selectors.REVIEW_ITEM.split(",")) {
                try {
                    reviewItems = reviewsContainer.findElements(By.cssSelector(itemSelector.trim()));
                    if (!reviewItems.isEmpty()) break;
                } catch (Exception e) {
                    // Try next selector
                }
            }

            System.out.println("Found " + reviewItems.size() + " laptop review items");

            // Process reviews (limit to 10 for performance)
            int reviewsToProcess = Math.min(reviewItems.size(), 10);

            for (int i = 0; i < reviewsToProcess; i++) {
                WebElement item = reviewItems.get(i);
                Map<String, String> review = new HashMap<>();

                // Extract author
                String author = getTextFromElement(item, Selectors.REVIEW_AUTHOR);
                review.put("author", author.isEmpty() ? "Anonymous" : author);

                // Extract review content
                String content = getTextFromElement(item, Selectors.REVIEW_TEXT);
                if (!content.isEmpty()) {
                    review.put("content", content);
                    reviews.add(review);
                }

                // Extract date if available
                String date = getTextFromElement(item, Selectors.REVIEW_DATE);
                if (!date.isEmpty()) {
                    review.put("date", date);
                }
            }

            if (!reviews.isEmpty()) {
                product.addCategoryData("reviews", reviews);
                System.out.println("Added " + reviews.size() + " laptop reviews");
            }

        } catch (Exception e) {
            System.err.println("Error extracting laptop reviews: " + e.getMessage());
        }
    }

    private void clickSpecsButton() {
        List<WebElement> buttons = driver.findElements(By.cssSelector(Selectors.SPECS_BUTTON));

        for (WebElement button : buttons) {
            try {
                String text = button.getText().toLowerCase();
                if (text.contains("cấu hình") || text.contains("thông số") ||
                        button.getAttribute("textContent").toLowerCase().contains("cấu hình")) {

                    // Scroll to button
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
                    CrawlerUtils.delayForEthicalScraping(300);

                    // Try regular click
                    try {
                        button.click();
                        System.out.println("Clicked laptop specs button");
                        return;
                    } catch (Exception e) {
                        // Try JavaScript click
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                        System.out.println("Clicked laptop specs button via JavaScript");
                        return;
                    }
                }
            } catch (Exception e) {
                // Try next button
            }
        }
    }

    // Helper methods for element interaction
    private List<WebElement> waitForElements(String selector) {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(selector)));
            return driver.findElements(By.cssSelector(selector));
        } catch (TimeoutException e) {
            System.err.println("Timeout waiting for elements: " + selector);
            return new ArrayList<>();
        }
    }

    private String getTextFromElement(WebElement parent, String selector) {
        try {
            WebElement element = parent.findElement(By.cssSelector(selector));
            return element.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String getTextFromElement(WebDriver driver, String selector) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            return element.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String getTextFromElements(WebDriver driver, String selector) {
        try {
            List<WebElement> elements = driver.findElements(By.cssSelector(selector));
            StringBuilder sb = new StringBuilder();

            for (WebElement element : elements) {
                String text = element.getText().trim();
                if (!text.isEmpty()) {
                    sb.append(text).append("\n\n");
                }
            }

            return sb.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String getAttributeFromElement(WebElement parent, String selector, String attribute) {
        try {
            WebElement element = parent.findElement(By.cssSelector(selector));
            String value = element.getAttribute(attribute);
            return value != null ? value.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String getAttributeFromElement(WebDriver driver, String selector, String attribute) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            String value = element.getAttribute(attribute);
            return value != null ? value.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }
}