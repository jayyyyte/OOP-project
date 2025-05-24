package scraper.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import scraper.model.Product;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class JsonSerializer {
    public static void saveToJson(List<Product> products, String filename) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(products, writer);
            System.out.println("Data saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing JSON: " + e.getMessage());
        }
    }
}