package scraper.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import scraper.model.AbstractProduct;

/**
 * Utility class for serializing objects to JSON format
 */
public class JsonSerializer {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Converts a single object to JSON string
     *
     * @param object The object to convert
     * @return JSON string representation
     */
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    /**
     * Saves a list of products to a JSON file
     *
     * @param products List of products to save
     * @param filePath Path to save the JSON file
     * @throws IOException If an I/O error occurs
     */
    public static void saveToFile(List<? extends AbstractProduct> products, String filePath) throws IOException {
        if (products == null || products.isEmpty()) {
            System.out.println("No products to save");
            return;
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(products, writer);
            System.out.println("Successfully saved " + products.size() + " products to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving to JSON: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Saves a single product to a JSON file
     *
     * @param product The product to save
     * @param filePath Path to save the JSON file
     * @throws IOException If an I/O error occurs
     */
    public static void saveToFile(AbstractProduct product, String filePath) throws IOException {
        if (product == null) {
            System.out.println("No product to save");
            return;
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(product, writer);
            System.out.println("Successfully saved product to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving to JSON: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Saves any object to a JSON file
     *
     * @param object The object to save
     * @param filePath Path to save the JSON file
     * @throws IOException If an I/O error occurs
     */
    public static void saveAnyObjectToFile(Object object, String filePath) throws IOException {
        if (object == null) {
            System.out.println("No object to save");
            return;
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(object, writer);
            System.out.println("Successfully saved object to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving to JSON: " + e.getMessage());
            throw e;
        }
    }
}