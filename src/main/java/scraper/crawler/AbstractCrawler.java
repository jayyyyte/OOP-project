package scraper.crawler;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scraper.model.AbstractProduct;

/**
 * Abstract base class for all web crawlers in the application.
 * Provides common crawler functionality and utilities.
 */
public abstract class AbstractCrawler implements Crawler {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final int DEFAULT_WAIT_TIMEOUT = 10;
    protected static final int DEFAULT_ETHICAL_DELAY = 500;

    /**
     * Initialize the web driver with default configurations.
     */
    protected void setupWebDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_TIMEOUT));
    }

    /**
     * Cleanly close the web driver and release resources.
     */
    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            System.out.println("WebDriver closed");
        }
    }

    /**
     * Wait for elements to be present on the page.
     *
     * @param selector CSS selector to locate elements
     * @return List of WebElements found
     */
    protected List<WebElement> waitForElements(String selector) {
        try {
            wait.until(d -> !d.findElements(By.cssSelector(selector)).isEmpty());
            return driver.findElements(By.cssSelector(selector));
        } catch (TimeoutException e) {
            System.err.println("Timeout waiting for elements: " + selector);
            return new ArrayList<>();
        }
    }

    /**
     * Get text content from an element.
     *
     * @param parent Parent WebElement
     * @param selector CSS selector to locate child element
     * @return Text content of the element or empty string if not found
     */
    protected String getTextFromElement(WebElement parent, String selector) {
        try {
            WebElement element = parent.findElement(By.cssSelector(selector));
            return element.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get text content from an element using the driver.
     *
     * @param selector CSS selector to locate element
     * @return Text content of the element or empty string if not found
     */
    protected String getTextFromElement(String selector) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            return element.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get attribute value from an element.
     *
     * @param parent Parent WebElement
     * @param selector CSS selector to locate child element
     * @param attribute Name of the attribute to retrieve
     * @return Attribute value or empty string if not found
     */
    protected String getAttributeFromElement(WebElement parent, String selector, String attribute) {
        try {
            WebElement element = parent.findElement(By.cssSelector(selector));
            String value = element.getAttribute(attribute);
            return value != null ? value.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get attribute value from an element using the driver.
     *
     * @param selector CSS selector to locate element
     * @param attribute Name of the attribute to retrieve
     * @return Attribute value or empty string if not found
     */
    protected String getAttributeFromElement(String selector, String attribute) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            String value = element.getAttribute(attribute);
            return value != null ? value.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Extract text content from multiple elements.
     *
     * @param selector CSS selector to locate elements
     * @return Combined text content from all elements with line breaks
     */
    protected String getTextFromElements(String selector) {
        try {
            StringBuilder sb = new StringBuilder();
            List<WebElement> elements = driver.findElements(By.cssSelector(selector));

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

    /**
     * Scroll to an element on the page.
     *
     * @param element WebElement to scroll to
     */
    protected void scrollToElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            delayForEthicalScraping(300);
        } catch (Exception e) {
            System.err.println("Failed to scroll to element: " + e.getMessage());
        }
    }

    /**
     * Click an element safely, trying JavaScript click if regular click fails.
     *
     * @param element WebElement to click
     * @return true if click was successful, false otherwise
     */
    protected boolean safeClick(WebElement element) {
        try {
            scrollToElement(element);
            element.click();
            return true;
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    /**
     * Check if a URL is valid (starts with http:// or https://).
     *
     * @param url URL to check
     * @return true if URL is valid, false otherwise
     */
    protected boolean isValidUrl(String url) {
        return url != null && !url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /**
     * Introduce a delay to respect website resources and robots.txt guidelines.
     *
     * @param milliseconds Delay duration in milliseconds
     */
    protected void delayForEthicalScraping(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Extract numeric values from text using regex.
     *
     * @param text Text to search for numbers
     * @param defaultValue Default value if no number is found
     * @return Extracted numeric value or defaultValue if none found
     */
    protected double extractNumericValue(String text, double defaultValue) {
        if (text == null || text.isEmpty()) {
            return defaultValue;
        }

        try {
            Pattern pattern = Pattern.compile("\\d+([.,]\\d+)?");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                String number = matcher.group().replace(',', '.');
                return Double.parseDouble(number);
            }
        } catch (Exception e) {
            System.err.println("Failed to extract numeric value: " + e.getMessage());
        }

        return defaultValue;
    }

    /**
     * Parse price string to double value.
     *
     * @param priceText Price as string
     * @return Parsed price as double
     */
    protected double parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return 0.0;
        }

        // Remove all non-numeric characters except decimal point and comma
        String cleanPrice = priceText.replaceAll("[^\\d.,]", "");

        // Replace comma with dot if used as decimal separator
        cleanPrice = cleanPrice.replace(',', '.');

        // If multiple dots remain, keep only the last one (assuming it's the decimal separator)
        int lastDotIndex = cleanPrice.lastIndexOf('.');
        if (lastDotIndex > -1 && cleanPrice.indexOf('.') != lastDotIndex) {
            StringBuilder sb = new StringBuilder(cleanPrice);
            for (int i = 0; i < lastDotIndex; i++) {
                if (sb.charAt(i) == '.') {
                    sb.setCharAt(i, '\0'); // Replace with a null character to be removed
                }
            }
            cleanPrice = sb.toString().replace("\0", "");
        }

        try {
            return Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            System.err.println("Could not parse price: " + priceText);
            return 0.0;
        }
    }

    /**
     * Default error handling method for crawling operations.
     *
     * @param operation Description of the operation that failed
     * @param e Exception that was thrown
     */
    protected void handleCrawlingError(String operation, Exception e) {
        System.err.println("Error during " + operation + ": " + e.getMessage());
        if (e instanceof WebDriverException) {
            System.err.println("WebDriver error. Attempting to recover...");
        }
    }

    /**
     * Abstract method to scrape a product list page.
     *
     * @param url URL of the product list page
     * @return List of product URLs found on the page
     */
    public abstract List<String> scrapeProductListPage(String url);

    /**
     * Abstract method to scrape product details.
     *
     * @param url URL of the product detail page
     * @return Product object with scraped details
     */
    public abstract AbstractProduct scrapeProductDetails(String url);
}