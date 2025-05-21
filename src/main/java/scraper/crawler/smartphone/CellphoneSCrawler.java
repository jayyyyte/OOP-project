package scraper.crawler.smartphone;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scraper.crawler.AbstractCrawler;
import scraper.factory.ProductFactory;
import scraper.factory.SmartphoneFactory;
import scraper.model.AbstractProduct;
import scraper.model.Smartphone;
import scraper.config.WebsiteConfig;

/**
 * Implementation of crawler for CellphoneS website specifically for smartphones.
 */
public class CellphoneSCrawler extends AbstractCrawler {

    private final WebsiteConfig config;
    private final ProductFactory productFactory;
    private static final String BASE_URL = "https://cellphones.com.vn/mobile.html";

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

    /**
     * Constructor that initializes the crawler with website config.
     *
     * @param config Configuration for the website to be crawled
     */
    public CellphoneSCrawler(WebsiteConfig config) {
        this.config = config;
        this.productFactory = new SmartphoneFactory();
        setupWebDriver();
    }

    /**
     * Constructor with default config.
     */
    public CellphoneSCrawler() {
        this.config = new WebsiteConfig();
        this.config.setBaseUrl(BASE_URL);
        this.productFactory = new SmartphoneFactory();
        setupWebDriver();
    }

    @Override
    public List<String> scrapeProductListPage(String url) {
        List<String> productUrls = new ArrayList<>();

        try {
            driver.get(url != null && !url.isEmpty() ? url : BASE_URL);
            List<WebElement> productContainers = waitForElements(Selectors.PRODUCT_CONTAINER);

            System.out.println("Found " + productContainers.size() + " products on the page");

            for (WebElement container : productContainers) {
                String productUrl = getAttributeFromElement(container, Selectors.PRODUCT_URL, "href");
                if (isValidUrl(productUrl)) {
                    productUrls.add(productUrl);
                }
                delayForEthicalScraping(150);
            }

        } catch (Exception e) {
            handleCrawlingError("scraping product list page", e);
        }

        return productUrls;
    }

    @Override
    public AbstractProduct scrapeProductDetails(String url) {
        if (!isValidUrl(url)) {
            System.err.println("Invalid URL provided: " + url);
            return null;
        }

        try {
            driver.get(url);
            delayForEthicalScraping(1000);

            // Create new smartphone product object
            Smartphone smartphone = (Smartphone) productFactory.createProduct();

            // Extract basic product info
            extractBasicProductInfo(smartphone);

            // Set product URL
            smartphone.setProductUrl(url);

            // Extract product description
            extractProductDescription(smartphone);

            // Extract specifications
            extractSpecifications(smartphone);

            // Extract ratings and reviews
            extractRatingsAndReviews(smartphone);

            System.out.println("Completed details for: " + smartphone.getName());

            return smartphone;

        } catch (Exception e) {
            handleCrawlingError("scraping product details", e);
            return null;
        }
    }

    private void extractBasicProductInfo(Smartphone smartphone) {
        try {
            // Extract product name
            String name = getTextFromElement("h1.product-name, h1.product-title, div.product-name");
            if (name.isEmpty()) {
                name = driver.getTitle().split("\\|")[0].trim();
            }
            smartphone.setName(name);

            // Extract price
            String priceText = getTextFromElement(Selectors.PRODUCT_PRICE);
            smartphone.setPrice(parsePrice(priceText));

            // Extract image URL
            String imageUrl = getAttributeFromElement("div.product-image img, div.swiper-slide img", "src");
            if (imageUrl.isEmpty()) {
                imageUrl = getAttributeFromElement("div.product-image img, div.swiper-slide img", "data-src");
            }
            smartphone.setImageUrl(imageUrl);

            System.out.println("Extracted basic info for: " + smartphone.getName());

        } catch (Exception e) {
            handleCrawlingError("extracting basic product info", e);
        }
    }

    private void extractProductDescription(Smartphone smartphone) {
        String description = "";

        // Try multiple selectors for description
        for (String selector : Selectors.DESCRIPTION_SELECTORS) {
            try {
                if (selector.equals("meta[name='description']")) {
                    description = getAttributeFromElement(selector, "content");
                } else {
                    description = getTextFromElements(selector);
                }

                if (!description.isEmpty()) {
                    System.out.println("Found description using: " + selector);
                    break;
                }
            } catch (Exception e) {
                // Try next selector
            }
        }

        // If still no description, try a more generic approach
        if (description.isEmpty()) {
            try {
                List<WebElement> contentDivs = driver.findElements(By.cssSelector("div.product-detail div, div.product-container div"));
                for (WebElement div : contentDivs) {
                    String text = div.getText().trim();
                    // Look for substantial text content
                    if (text.length() > 100) {
                        description = text;
                        break;
                    }
                }
            } catch (Exception e) {
                // Ignore and continue
            }
        }

        smartphone.setDescription(description.isEmpty() ? "No description available" : description);
    }

    private void extractSpecifications(Smartphone smartphone) {
        Map<String, String> specs = new HashMap<>();
        boolean specsFound = false;

        // First try to click specifications button if available
        try {
            clickSpecsButton();
            delayForEthicalScraping(500);
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

                            // Extract smartphone specific attributes
                            updateSmartphoneFromSpecs(smartphone, label.toLowerCase(), value);
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

                                    // Extract smartphone specific attributes
                                    updateSmartphoneFromSpecs(smartphone, key.toLowerCase(), value);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Try final method
            }
        }

        // Set specs to smartphone
        smartphone.setSpecifications(specs);

        if (specsFound) {
            System.out.println("Found " + specs.size() + " specifications");
        } else {
            System.out.println("No specifications found");
        }
    }

    private void updateSmartphoneFromSpecs(Smartphone smartphone, String key, String value) {
        if (key.contains("cpu") || key.contains("chip") || key.contains("processor")) {
            smartphone.setProcessor(value);
        } else if (key.contains("ram")) {
            smartphone.setRam(value);
        } else if (key.contains("storage") || key.contains("memory") || key.contains("rom")) {
            smartphone.setStorage(value);
        } else if (key.contains("os") || key.contains("operating system") || key.contains("hệ điều hành")) {
            smartphone.setOperatingSystem(value);
        } else if (key.contains("screen") || key.contains("display") || key.contains("màn hình")) {
            smartphone.setScreenSize(value);
        } else if (key.contains("camera") && key.contains("rear") || key.contains("back") || key.contains("sau")) {
            smartphone.setRearCamera(value);
        } else if (key.contains("camera") && key.contains("front") || key.contains("selfie") || key.contains("trước")) {
            smartphone.setFrontCamera(value);
        } else if (key.contains("battery") || key.contains("pin")) {
            smartphone.setBattery(value);
        }
    }

    private void extractRatingsAndReviews(Smartphone smartphone) {
        // Extract rating
        double rating = extractRating();
        smartphone.setOverallRating(rating);

        // Extract review count
        int reviewCount = extractReviewCount();
        smartphone.setReviewCount(reviewCount);

        // Extract actual reviews if count > 0
        if (reviewCount > 0) {
            List<Map<String, String>> reviews = extractReviews();
            if (!reviews.isEmpty()) {
                smartphone.addCategoryData("reviews", reviews);
                System.out.println("Added " + reviews.size() + " reviews");
            }
        }
    }

    private double extractRating() {
        double rating = 0.0;

        // Try direct rating selector
        try {
            String ratingText = getTextFromElement(Selectors.RATING);

            if (!ratingText.isEmpty()) {
                // Clean up rating text and extract number
                if (ratingText.contains("/")) {
                    ratingText = ratingText.split("/")[0].trim();
                }

                ratingText = ratingText.replaceAll("[^0-9.,]", "").replace(",", ".");

                if (!ratingText.isEmpty()) {
                    rating = Double.parseDouble(ratingText);
                    System.out.println("Found rating: " + rating);
                }
            }
        } catch (Exception e) {
            // Try alternative method
        }

        // If rating still 0, try to count stars
        if (rating == 0) {
            try {
                String[] starSelectors = {
                        "div.rating i.fa-star",
                        "div.rating span.fa",
                        "div.rating-stars i",
                        "div.rating-overview i.fas"
                };

                for (String selector : starSelectors) {
                    List<WebElement> stars = driver.findElements(By.cssSelector(selector));

                    if (!stars.isEmpty()) {
                        int filledStars = 0;

                        for (WebElement star : stars) {
                            String classes = star.getAttribute("class") + " " + star.getCssValue("color");
                            if (classes.contains("fa-solid") || classes.contains("active") ||
                                    classes.contains("checked") || classes.contains("fas fa-star") ||
                                    classes.contains("rgb(255, 193, 7)") || classes.contains("#ffc107")) {
                                filledStars++;
                            }
                        }

                        if (filledStars > 0) {
                            rating = filledStars;
                            System.out.println("Found rating via stars: " + rating);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // Try one more method
            }
        }

        return rating;
    }

    private int extractReviewCount() {
        int reviewCount = 0;

        // Try direct selector
        try {
            String countText = getTextFromElement(Selectors.REVIEW_COUNT);

            if (!countText.isEmpty()) {
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(countText);

                if (matcher.find()) {
                    reviewCount = Integer.parseInt(matcher.group());
                    System.out.println("Found review count: " + reviewCount);
                }
            }
        } catch (Exception e) {
            // Try alternative method
        }

        // Try counting actual review items
        if (reviewCount == 0) {
            try {
                List<WebElement> reviewItems = findReviewItems();
                if (!reviewItems.isEmpty()) {
                    reviewCount = reviewItems.size();
                    System.out.println("Found review count by counting: " + reviewCount);
                }
            } catch (Exception e) {
                // Try one more method
            }
        }

        return reviewCount;
    }

    private List<Map<String, String>> extractReviews() {
        List<Map<String, String>> reviews = new ArrayList<>();

        try {
            List<WebElement> reviewItems = findReviewItems();
            System.out.println("Found " + reviewItems.size() + " review items");

            // Limit to 10 reviews for efficiency
            int reviewsToProcess = Math.min(reviewItems.size(), 10);

            for (int i = 0; i < reviewsToProcess; i++) {
                WebElement item = reviewItems.get(i);
                Map<String, String> review = extractSingleReview(item);

                if (!review.isEmpty()) {
                    reviews.add(review);
                }
            }

        } catch (Exception e) {
            System.err.println("Error extracting reviews: " + e.getMessage());
        }

        return reviews;
    }

    private List<WebElement> findReviewItems() {
        List<WebElement> reviewItems = new ArrayList<>();

        // Try to find review container first
        for (String containerSelector : Selectors.REVIEWS_CONTAINER.split(",")) {
            try {
                WebElement container = driver.findElement(By.cssSelector(containerSelector.trim()));

                // Find review items within container
                for (String itemSelector : Selectors.REVIEW_ITEM.split(",")) {
                    try {
                        List<WebElement> items = container.findElements(By.cssSelector(itemSelector.trim()));
                        if (!items.isEmpty()) {
                            return items;
                        }
                    } catch (Exception e) {
                        // Try next selector
                    }
                }
            } catch (Exception e) {
                // Try next container selector
            }
        }

        // If no container found, try direct search
        for (String itemSelector : Selectors.REVIEW_ITEM.split(",")) {
            try {
                List<WebElement> items = driver.findElements(By.cssSelector(itemSelector.trim()));
                if (!items.isEmpty()) {
                    return items;
                }
            } catch (Exception e) {
                // Try next selector
            }
        }

        return reviewItems;
    }

    private Map<String, String> extractSingleReview(WebElement reviewItem) {
        Map<String, String> review = new HashMap<>();

        // Extract author
        try {
            String author = getTextFromElement(reviewItem, "strong");
            review.put("author", author.isEmpty() ? "Anonymous" : author);
        } catch (Exception e) {
            review.put("author", "Anonymous");
        }

        // Extract review content
        try {
            String content = getTextFromElement(reviewItem, Selectors.REVIEW_TEXT);
            if (!content.isEmpty()) {
                review.put("content", content);
            }
        } catch (Exception e) {
            // Try to extract content from full text
            try {
                String fullText = reviewItem.getText();

                // Remove author if known
                if (review.containsKey("author")) {
                    fullText = fullText.replace(review.get("author"), "");
                }

                // Remove common UI text
                fullText = fullText.replaceAll("Đã mua tại CellphoneS", "");
                fullText = fullText.replaceAll("\\d{1,2}/\\d{1,2}/\\d{4}", "");
                fullText = fullText.replaceAll("\\★{1,5}", "");
                fullText = fullText.replaceAll("\\d sao", "");

                fullText = fullText.trim();
                if (!fullText.isEmpty()) {
                    review.put("content", fullText);
                }
            } catch (Exception ex) {
                // No content found
            }
        }

        // Extract date
        try {
            String date = getTextFromElement(reviewItem, Selectors.REVIEW_DATE);
            if (!date.isEmpty()) {
                review.put("date", date);
            }
        } catch (