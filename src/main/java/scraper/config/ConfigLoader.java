package scraper.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    public static WebsiteConfig loadConfig(String fileName) {
        Properties props = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config/" + fileName)) {
            if (input == null) {
                throw new IOException("Config file not found: " + fileName);
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading config: " + e.getMessage());
        }

        return new WebsiteConfig(
                props.getProperty("url"),
                props.getProperty("targetCategory"),
                props.getProperty("productContainer"),
                props.getProperty("name"),
                props.getProperty("price"),
                props.getProperty("image"),
                props.getProperty("detailUrl"),
                props.getProperty("description"),
                props.getProperty("specsTable"),
                props.getProperty("specsRow"),
                props.getProperty("specsLabel"),
                props.getProperty("specsValue"),
                props.getProperty("altSpecsRow"),
                props.getProperty("rating"),
                props.getProperty("reviewCount"),
                props.getProperty("reviewContainer"),
                props.getProperty("reviewAuthor"),
                props.getProperty("reviewText"),
                props.getProperty("reviewRating"),
                props.getProperty("reviewDate")
        );
    }
}