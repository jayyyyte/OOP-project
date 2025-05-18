package search;

import model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.Normalizer;

public class BasicSearchEngine implements SearchEngine {

    // Pattern to extract a number for price comparison.
    // It looks for "dưới", "duoi", "max", "upper", "less than", "under" followed by a number.
    // And "trên", "tren", "min", "lower", "greater than", "over" followed by a number.
    // Allows for "k", "tr", "trieu" suffixes for thousands/millions.
    private static final Pattern PRICE_LESS_THAN_PATTERN = Pattern.compile(
        "(?:dưới|duoi|max|upper|less than|under)\\s*(\\d[\\d.,]*)\\s*(k|tr|trieu)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_GREATER_THAN_PATTERN = Pattern.compile(
        "(?:trên|tren|min|lower|greater than|over)\\s*(\\d[\\d.,]*)\\s*(k|tr|trieu)?", Pattern.CASE_INSENSITIVE);

    // Helper to remove accents from Vietnamese text for more lenient matching
    private String removeAccents(String text) {
        if (text == null) return null;
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replaceAll("đ", "d").replaceAll("Đ", "D");
    }


    private double parsePriceLimit(Matcher matcher) {
        if (matcher.find()) {
            try {
                String valueStr = matcher.group(1).replace(".", "").replace(",", ""); // Remove thousand separators for parsing
                double value = Double.parseDouble(valueStr);
                String suffix = matcher.group(2);
                if (suffix != null) {
                    if ("k".equalsIgnoreCase(suffix)) {
                        value *= 1000;
                    } else if ("tr".equalsIgnoreCase(suffix) || "trieu".equalsIgnoreCase(suffix)) {
                        value *= 1000000;
                    }
                }
                return value;
            } catch (NumberFormatException e) {
                // Could not parse number
                System.err.println("Could not parse price from query: " + matcher.group(0));
            }
        }
        return -1; // Indicates no valid price limit found or parsing error
    }

    @Override
    public List<Product> search(String query, List<Product> products) {
        List<Product> results = new ArrayList<>();
        String normalizedQuery = removeAccents(query.toLowerCase());

        String textSearchQuery = normalizedQuery; // Query part for text matching

        double maxPrice = Double.POSITIVE_INFINITY;
        double minPrice = 0;

        // Extract price conditions from the query
        Matcher lessThanMatcher = PRICE_LESS_THAN_PATTERN.matcher(normalizedQuery);
        double parsedMaxPrice = parsePriceLimit(lessThanMatcher);
        if (parsedMaxPrice != -1) {
            maxPrice = parsedMaxPrice;
            textSearchQuery = textSearchQuery.replace(lessThanMatcher.group(0), "").trim(); // Remove matched part
        }

        Matcher greaterThanMatcher = PRICE_GREATER_THAN_PATTERN.matcher(normalizedQuery);
        double parsedMinPrice = parsePriceLimit(greaterThanMatcher);
        if (parsedMinPrice != -1) {
            minPrice = parsedMinPrice;
            textSearchQuery = textSearchQuery.replace(greaterThanMatcher.group(0), "").trim(); // Remove matched part
        }
        
        // If both "dưới X" and "trên Y" are used, and X < Y, it's an impossible range.
        // However, current parsing handles them independently. For combined "between X and Y", more complex regex is needed.
        // This implementation assumes "dưới X" and "trên Y" can co-exist.

        String[] keywords = textSearchQuery.isEmpty() ? new String[0] : textSearchQuery.split("\\s+");

        for (Product p : products) {
            boolean nameMatches = false;
            String normalizedProductName = removeAccents(p.getName().toLowerCase());

            if (keywords.length == 0) { // If only price filters were present, or empty query
                nameMatches = true; // All products match by name if no keywords
            } else {
                // OR logic: if any keyword matches the product name
                for (String keyword : keywords) {
                    if (!keyword.isEmpty() && normalizedProductName.contains(keyword)) {
                        nameMatches = true;
                        break;
                    }
                }
                // To use AND logic (all keywords must match):
                // nameMatches = true;
                // for (String keyword : keywords) {
                //     if (!keyword.isEmpty() && !normalizedProductName.contains(keyword)) {
                //         nameMatches = false;
                //         break;
                //     }
                // }
            }

            boolean priceMatches = (p.getPrice() <= maxPrice && p.getPrice() >= minPrice);

            if (nameMatches && priceMatches) {
                results.add(p);
            }
        }
        return results;
    }
}