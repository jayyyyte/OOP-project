package scraper.crawler.smartphone;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import scraper.crawler.AbstractCrawler;
import scraper.model.AbstractProduct;
import scraper.model.Smartphone;
import scraper.factory.ProductFactory;
import scraper.factory.SmartphoneFactory;
import scraper.config.WebsiteConfig;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crawler implementation for HoangHaMobile website.
 * Scrapes smartphone product data from hoanghamobile.com
 */
public class HoangHaMobileCrawler extends AbstractCrawler {
    private static final String BASE_URL = "https://hoanghamobile.com/dien-thoai-di-dong";
    private final ProductFactory smartphoneFactory;
    private final WebsiteConfig config;

    // CSS selectors for the HoangHaMobile website
    private static final class Selectors {
        // List page selectors
        static final String PRODUCT_CONTAINER = "div.product-item";
        static final String PRODUCT_NAME = "div.info h3";
        static final String PRODUCT_PRICE = "div.price strong";
        static final String PRODUCT_IMAGE = "div.img img";
        static final String PRODUCT_URL = "div.info h3 a";

        // Detail page selectors
        static final String DESCRIPTION = "div.product-intro-desc";
        static final String SPECS_TABLE = "div.specs-special div.specs-special__item";
        static final String SPECS_LABEL = "div.specs-special__item__left";
        static final String SPECS_VALUE = "div.specs-special__item__right";
        static final String RATING = "div.product-rating__total span";
        static final String REVIEW_COUNT = "div.product-rating__total small";
        static final String REVIEWS_CONTAINER = "div.product-rating__comment-list";
        static final String REVIEW_ITEM = "div.product-rating__comment-item";
    }

    /**
     * Constructor for HoangHaMobileCrawler.
     *
     * @param config Configuration for the website
     */
    public HoangHaMobileCrawler(WebsiteConfig config) {
        this.config = config;
        this.smartphoneFactory = new SmartphoneFactory();
        setupWebDriver();
    }

    @Override
    public List<AbstractProduct> crawlProducts() {
        List<AbstractProduct> products = new ArrayList<>();

        try {
            List<String> productUrls = scrapeProductListPage(BASE_URL);
            System.out.println("Found " + productUrls.size() + " product URLs");

            // Limit the number of products to crawl if needed
            int limit = Math.min(productUrls.size(), config.getMaxProductsToScrape());

            for (int i = 0; i < limit; i++) {
                try {
                    AbstractProduct product = scrapeProductDetails(productUrls.get(i));
                    if (product != null) {
                        products.add(product);
                        System.out.println("Scraped product: " + product.getName());
                    }
                    delayForEthicalScraping(config.getDelayBetweenRequests());
                } catch (Exception e) {
                    handleCrawlingError("scraping product details", e);
                }
            }
        } catch (Exception e) {
            handleCrawlingError("crawling products", e);
        }

        return products;
    }

    @Override
    public List<String> scrapeProductListPage(String url) {
        List<String> productUrls = new ArrayList<>();

        try {
            driver.get(url);
            delayForEthicalScraping(DEFAULT_ETHICAL_DELAY);

            List<WebElement> productContainers = waitForElements(Selectors.PRODUCT_CONTAINER);

            for (WebElement container : productContainers) {
                try {
                    String productUrl = getAttributeFromElement(container, Selectors.PRODUCT_URL, "href");

                    if (isValidUrl(productUrl)) {
                        productUrls.add(productUrl);
                    }
                } catch (Exception e) {
                    // Continue to next product
                    System.err.println("Error extracting product URL: " + e.getMessage());
                }
            }

            // Check if there are additional pages to scrape
            int pageLimit = config.getMaxPagesToScrape();
            for (int page = 2; page <= pageLimit; page++) {
                String nextPageUrl = url + "?page=" + page;

                try {
                    driver.get(nextPageUrl);
                    delayForEthicalScraping(DEFAULT_ETHICAL_DELAY);

                    List<WebElement> nextPageProducts = waitForElements(Selectors.PRODUCT_CONTAINER);

                    if (nextPageProducts.isEmpty()) {
                        break; // No more products, exit pagination
                    }

                    for (WebElement container : nextPageProducts) {
                        String productUrl = getAttributeFromElement(container, Selectors.PRODUCT_URL, "href");

                        if (isValidUrl(productUrl)) {
                            productUrls.add(productUrl);
                        }
                    }

                    delayForEthicalScraping(DEFAULT_ETHICAL_DELAY);
                } catch (Exception e) {
                    handleCrawlingError("scraping page " + page, e);
                    break; // Stop pagination on error
                }
            }

        } catch (Exception e) {
            handleCrawlingError("scraping product list page", e);
        }

        return productUrls;
    }

    @Override
    public AbstractProduct scrapeProductDetails(String url) {
        Smartphone smartphone = (Smartphone) smartphoneFactory.createProduct();

        try {
            driver.get(url);
            delayForEthicalScraping(DEFAULT_ETHICAL_DELAY);

            // Extract product name
            String name = getTextFromElement("h1.product-name");
            smartphone.setName(name);

            // Extract product URL
            smartphone.setProductUrl(url);

            // Extract product image
            String imageUrl = getAttributeFromElement("div.product-gallery__item img", "src");
            if (imageUrl.isEmpty()) {
                imageUrl = getAttributeFromElement("div.product-gallery__item img", "data-src");
            }
            smartphone.setImageUrl(imageUrl);

            // Extract price
            String priceText = getTextFromElement("div.product-price--latest strong");
            smartphone.setPrice(parsePrice(priceText));

            // Extract description
            String description = getTextFromElement(Selectors.DESCRIPTION);
            smartphone.setDescription(description);

            // Extract specifications
            extractSpecifications(smartphone);

            // Extract rating and reviews
            extractRatingsAndReviews(smartphone);

            return smartphone;

        } catch (Exception e) {
            handleCrawlingError("scraping product details", e);
            return null;
        }
    }

    /**
     * Extracts product specifications from the detail page.
     *
     * @param smartphone Smartphone object to populate with specifications
     */
    private void extractSpecifications(Smartphone smartphone) {
        Map<String, String> specs = new HashMap<>();

        try {
            List<WebElement> specRows = driver.findElements(By.cssSelector(Selectors.SPECS_TABLE));

            for (WebElement row : specRows) {
                String label = getTextFromElement(row, Selectors.SPECS_LABEL);
                String value = getTextFromElement(row, Selectors.SPECS_VALUE);

                if (!label.isEmpty() && !value.isEmpty()) {
                    specs.put(label, value);

                    // Extract specific smartphone attributes
                    String labelLower = label.toLowerCase();
                    if (labelLower.contains("màn hình") || labelLower.contains("screen")) {
                        smartphone.setScreenSize(extractScreenSize(value));
                        smartphone.setScreenResolution(extractScreenResolution(value));
                    } else if (labelLower.contains("cpu") || labelLower.contains("processor") || labelLower.contains("chip")) {
                        smartphone.setProcessor(value);
                    } else if (labelLower.contains("ram")) {
                        smartphone.setRamSize(extractRamSize(value));
                    } else if (labelLower.contains("bộ nhớ") || labelLower.contains("storage")) {
                        smartphone.setStorageSize(extractStorageSize(value));
                    } else if (labelLower.contains("camera sau") || labelLower.contains("rear camera")) {
                        smartphone.setRearCamera(value);
                    } else if (labelLower.contains("camera trước") || labelLower.contains("front camera")) {
                        smartphone.setFrontCamera(value);
                    } else if (labelLower.contains("pin") || labelLower.contains("battery")) {
                        smartphone.setBatteryCapacity(extractBatteryCapacity(value));
                    } else if (labelLower.contains("hệ điều hành") || labelLower.contains("os")) {
                        smartphone.setOperatingSystem(value);
                    }
                }
            }

            smartphone.setSpecifications(specs);

        } catch (Exception e) {
            System.err.println("Error extracting specifications: " + e.getMessage());
        }
    }

    /**
     * Extracts ratings and reviews data.
     *
     * @param smartphone Smartphone object to populate with ratings and reviews
     */
    private void extractRatingsAndReviews(Smartphone smartphone) {
        try {
            // Extract rating
            String ratingText = getTextFromElement(Selectors.RATING);
            if (!ratingText.isEmpty()) {
                double rating = extractNumericValue(ratingText, 0.0);
                smartphone.setOverallRating(rating);
            }

            // Extract review count
            String countText = getTextFromElement(Selectors.REVIEW_COUNT);
            if (!countText.isEmpty()) {
                int count = (int) extractNumericValue(countText, 0);
                smartphone.setReviewCount(count);
            }

            // Extract reviews if there are any
            if (smartphone.getReviewCount() > 0) {
                extractReviews(smartphone);
            }

        } catch (Exception e) {
            System.err.println("Error extracting ratings and reviews: " + e.getMessage());
        }
    }

    /**
     * Extracts individual review items.
     *
     * @param smartphone Smartphone object to populate with reviews
     */
    private void extractReviews(Smartphone smartphone) {
        try {
            List<WebElement> reviewItems = driver.findElements(By.cssSelector(Selectors.REVIEW_ITEM));
            List<Review> reviews = new ArrayList<>();

            // Limit to 10 reviews for efficiency
            int reviewsToProcess = Math.min(reviewItems.size(), 10);

            for (int i = 0; i < reviewsToProcess; i++) {
                WebElement item = reviewItems.get(i);

                String author = getTextFromElement(item, "span.comment-author");
                String content = getTextFromElement(item, "div.comment-content");
                String dateText = getTextFromElement(item, "div.comment-time");

                if (!content.isEmpty()) {
                    Review review = new Review();
                    review.setAuthor(author.isEmpty() ? "Anonymous" : author);
                    review.setContent(content);
                    review.setDate(dateText);

                    // Try to extract rating for individual review
                    try {
                        List<WebElement> stars = item.findElements(By.cssSelector("div.comment-rating i.fas"));
                        review.setRating(stars.size());
                    } catch (Exception e) {
                        // Rating is optional, continue
                    }

                    reviews.add(review);
                }
            }

            smartphone.setReviews(reviews);

        } catch (Exception e) {
            System.err.println("Error extracting individual reviews: " + e.getMessage());
        }
    }

    /**
     * Extracts screen size from specification text.
     *
     * @param value Specification text
     * @return Extracted screen size in inches
     */
    private double extractScreenSize(String value) {
        try {
            Pattern pattern = Pattern.compile("(\\d+([.,]\\d+)?)\\s*inch");
            Matcher matcher = pattern.matcher(value.toLowerCase());

            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1).replace(',', '.'));
            }
        } catch (Exception e) {
            // Continue with default
        }
        return 0.0;
    }

    /**
     * Extracts screen resolution from specification text.
     *
     * @param value Specification text
     * @return Extracted screen resolution
     */
    private String extractScreenResolution(String value) {
        try {
            Pattern pattern = Pattern.compile("(\\d+\\s*[xX]\\s*\\d+)");
            Matcher matcher = pattern.matcher(value);

            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // Continue with default
        }
        return "";
    }

    /**
     * Extracts RAM size from specification text.
     *
     * @param value Specification text
     * @return Extracted RAM size in GB
     */
    private int extractRamSize(String value) {
        try {
            Pattern pattern = Pattern.compile("(\\d+)\\s*GB");
            Matcher matcher = pattern.matcher(value.toUpperCase());

            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // Continue with default
        }
        return 0;
    }

    /**
     * Extracts storage size from specification text.
     *
     * @param value Specification text
     * @return Extracted storage size in GB
     */
    private int extractStorageSize(String value) {
        try {
            // Try to match GB pattern first
            Pattern gbPattern = Pattern.compile("(\\d+)\\s*GB");
            Matcher gbMatcher = gbPattern.matcher(value.toUpperCase());

            if (gbMatcher.find()) {
                return Integer.parseInt(gbMatcher.group(1));
            }

            // Try to match TB pattern and convert to GB
            Pattern tbPattern = Pattern.compile("(\\d+)\\s*TB");
            Matcher tbMatcher = tbPattern.matcher(value.toUpperCase());

            if (tbMatcher.find()) {
                return Integer.parseInt(tbMatcher.group(1)) * 1024;
            }
        } catch (Exception e) {
            // Continue with default
        }
        return 0;
    }

    /**
     * Extracts battery capacity from specification text.
     *
     * @param value Specification text
     * @return Extracted battery capacity in mAh
     */
    private int extractBatteryCapacity(String value) {
        try {
            Pattern pattern = Pattern.compile("(\\d+)\\s*mAh");
            Matcher matcher = pattern.matcher(value);

            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // Continue with default
        }
        return 0;
    }

    @Override
    public String getWebsiteName() {
        return "HoangHaMobile";
    }
}