package scraper.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Utility class for common web scraping operations
 */
public class CrawlerUtils {

    /**
     * Wait for elements with the given CSS selector to be present
     *
     * @param driver The WebDriver instance
     * @param wait The WebDriverWait instance
     * @param selector CSS selector
     * @return List of WebElements found
     */
    public static List<WebElement> waitForElements(WebDriver driver, WebDriverWait wait, String selector) {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(selector)));
            return driver.findElements(By.cssSelector(selector));
        } catch (TimeoutException e) {
            System.err.println("Timeout waiting for elements: " + selector);
            return new ArrayList<>();
        }
    }

    /**
     * Wait for a single element with the given CSS selector to be present
     *
     * @param driver The WebDriver instance
     * @param wait The WebDriverWait instance
     * @param selector CSS selector
     * @return The WebElement if found, null otherwise
     */
    public static WebElement waitForElement(WebDriver driver, WebDriverWait wait, String selector) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
        } catch (TimeoutException e) {
            System.err.println("Timeout waiting for element: " + selector);
            return null;
        }
    }

    /**
     * Get text from an element with the given CSS selector
     *
     * @param parent Parent WebElement
     * @param selector CSS selector
     * @return Text content of the element, empty string if not found
     */
    public static String getTextFromElement(WebElement parent, String selector) {
        try {
            WebElement element = parent.findElement(By.cssSelector(selector));
            return element.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get text from an element with the given CSS selector
     *
     * @param driver WebDriver instance
     * @param selector CSS selector
     * @return Text content of the element, empty string if not found
     */
    public static String getTextFromElement(WebDriver driver, String selector) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            return element.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get text from all elements with the given CSS selector
     *
     * @param driver WebDriver instance
     * @param selector CSS selector
     * @return Combined text content of all elements, empty string if none found
     */
    public static String getTextFromElements(WebDriver driver, String selector) {
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

    /**
     * Get attribute value from an element with the given CSS selector
     *
     * @param parent Parent WebElement
     * @param selector CSS selector
     * @param attribute Attribute name
     * @return Attribute value, empty string if not found
     */
    public static String getAttributeFromElement(WebElement parent, String selector, String attribute) {
        try {
            WebElement element = parent.findElement(By.cssSelector(selector));
            String value = element.getAttribute(attribute);
            return value != null ? value.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get attribute value from an element with the given CSS selector
     *
     * @param driver WebDriver instance
     * @param selector CSS selector
     * @param attribute Attribute name
     * @return Attribute value, empty string if not found
     */
    public static String getAttributeFromElement(WebDriver driver, String selector, String attribute) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            String value = element.getAttribute(attribute);
            return value != null ? value.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Scroll to an element
     *
     * @param driver WebDriver instance
     * @param element Element to scroll to
     */
    public static void scrollToElement(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        } catch (Exception e) {
            System.err.println("Error scrolling to element: " + e.getMessage());
        }
    }

    /**
     * Click on an element, with fallback to JavaScript click
     *
     * @param driver WebDriver instance
     * @param element Element to click
     * @return true if click was successful, false otherwise
     */
    public static boolean clickElement(WebDriver driver, WebElement element) {
        try {
            scrollToElement(driver, element);
            delayForEthicalScraping(300);

            try {
                element.click();
                return true;
            } catch (Exception e) {
                // Try JavaScript click as fallback
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                    return true;
                } catch (Exception jsException) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a URL is valid
     *
     * @param url URL to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        return url != null && !url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /**
     * Delay execution to avoid overwhelming the target server
     *
     * @param milliseconds Delay in milliseconds
     */
    public static void delayForEthicalScraping(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Parse a price string into a double value
     *
     * @param priceText Price text to parse
     * @return Parsed price as double, 0.0 if parsing fails
     */
    public static double parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return 0.0;
        }

        // Remove all non-numeric characters except decimal point
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
     * Extract specifications from a specifications table
     *
     * @param driver WebDriver instance
     * @param tableSelector CSS selector for the specifications table
     * @param rowSelector CSS selector for table rows
     * @param labelSelector CSS selector for specification labels
     * @param valueSelector CSS selector for specification values
     * @return Map of specifications (label -> value)
     */
    public static Map<String, String> extractSpecificationsFromTable(WebDriver driver,
                                                                     String tableSelector,
                                                                     String rowSelector,
                                                                     String labelSelector,
                                                                     String valueSelector) {
        Map<String, String> specs = new HashMap<>();

        try {
            List<WebElement> specTables = driver.findElements(By.cssSelector(tableSelector));
            for (WebElement table : specTables) {
                List<WebElement> rows = table.findElements(By.cssSelector(rowSelector));

                for (WebElement row : rows) {
                    try {
                        String label = getTextFromElement(row, labelSelector);
                        String value = getTextFromElement(row, valueSelector);

                        if (!label.isEmpty() && !value.isEmpty()) {
                            specs.put(label, value);
                        }
                    } catch (Exception e) {
                        // Skip this row
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting specifications from table: " + e.getMessage());
        }

        return specs;
    }

    /**
     * Extract specifications from text content with "Label: Value" format
     *
     * @param driver WebDriver instance
     * @param containerSelector CSS selector for the container
     * @param itemSelector CSS selector for items containing specifications
     * @return Map of specifications (label -> value)
     */
    public static Map<String, String> extractSpecificationsFromText(WebDriver driver,
                                                                    String containerSelector,
                                                                    String itemSelector) {
        Map<String, String> specs = new HashMap<>();

        try {
            List<WebElement> containers = driver.findElements(By.cssSelector(containerSelector));

            for (WebElement container : containers) {
                List<WebElement> items = container.findElements(By.cssSelector(itemSelector));

                for (WebElement item : items) {
                    String text = item.getText().trim();

                    // Parse "Label: Value" format
                    if (text.contains(":")) {
                        String[] parts = text.split(":", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            if (!key.isEmpty() && !value.isEmpty()) {
                                specs.put(key, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting specifications from text: " + e.getMessage());
        }

        return specs;
    }

    /**
     * Extract data using regular expressions
     *
     * @param source Source text to search in
     * @param regex Regular expression pattern
     * @param groupIndex Group index to extract
     * @return Extracted data, empty string if not found
     */
    public static String extractWithRegex(String source, String regex, int groupIndex) {
        if (source == null || source.isEmpty()) {
            return "";
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(source);

            if (matcher.find() && matcher.groupCount() >= groupIndex) {
                return matcher.group(groupIndex);
            }
        } catch (Exception e) {
            System.err.println("Error extracting with regex: " + e.getMessage());
        }

        return "";
    }

    /**
     * Extract all matches of a regex pattern
     *
     * @param source Source text to search in
     * @param regex Regular expression pattern
     * @param groupIndex Group index to extract
     * @return List of extracted data
     */
    public static List<String> extractAllWithRegex(String source, String regex, int groupIndex) {
        List<String> results = new ArrayList<>();

        if (source == null || source.isEmpty()) {
            return results;
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(source);

            while (matcher.find() && matcher.groupCount() >= groupIndex) {
                results.add(matcher.group(groupIndex));
            }
        } catch (Exception e) {
            System.err.println("Error extracting with regex: " + e.getMessage());
        }

        return results;
    }
}