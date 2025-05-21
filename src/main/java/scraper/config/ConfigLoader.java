package scraper.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    public static WebsiteConfig loadConfig(String fileName) {
        Properties props = new Properties();
        String resourcePath = "config/" + fileName;

        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IOException("Config file not found at classpath: " + resourcePath + ". Ensure it is in src/main/resources/config/");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading config file '" + fileName + "': " + e.getMessage(), e);
        }

        // Validate required properties
        String url = props.getProperty("url");
        String productContainer = props.getProperty("productContainer");
        String name = props.getProperty("name");
        if (url == null || productContainer == null || name == null) {
            throw new IllegalStateException("Missing required properties in " + fileName + ": url, productContainer, and name are mandatory");
        }

        return new WebsiteConfig(
                url,
                props.getProperty("targetCategory", "Unknown"),
                productContainer,
                name,
                props.getProperty("price", ""),
                props.getProperty("image", ""),
                props.getProperty("detailUrl", ""),
                props.getProperty("description", ""),
                props.getProperty("specsTable", ""),
                props.getProperty("specsRow", ""),
                props.getProperty("specsLabel", ""),
                props.getProperty("specsValue", ""),
                props.getProperty("altSpecsRow", ""),
                props.getProperty("rating", ""),
                props.getProperty("reviewCount", ""),
                props.getProperty("reviewContainer", ""),
                props.getProperty("reviewAuthor", ""),
                props.getProperty("reviewText", ""),
                props.getProperty("reviewRating", ""),
                props.getProperty("reviewDate", "")
        );
    }
}