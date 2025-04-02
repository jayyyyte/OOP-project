package main.crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CrawlerManager {
    private FptShopCrawler crawler;
    
    public CrawlerManager(FptShopCrawler crawler) {
        this.crawler = crawler;
    }
    
    public CrawlerManager() {
        this.crawler = new FptShopCrawler();
    }
    
    public void crawlAndSavePhones(String url, String outputFile) {
        System.out.println("Starting crawler for: " + url);
        
        List<Phone> phones = crawler.crawlPhones(url);
        
        if (!phones.isEmpty()) {
            saveToFile(phones, outputFile);
            System.out.println("Saved " + phones.size() + " phones to " + outputFile);
            
            // Tạo thêm phiên bản JSON cho dữ liệu
            String jsonFile = outputFile.replace(".txt", ".json");
            saveToJson(phones, jsonFile);
            System.out.println("Saved JSON data to " + jsonFile);
        } else {
            System.out.println("No phones found to save.");
        }
    }
    
    private void saveToFile(List<Phone> phones, String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            for (Phone phone : phones) {
                writer.println(phone.toString());
                writer.println("----------------------------------------");
            }
        } catch (IOException e) {
            System.err.println("Error saving to file: " + e.getMessage());
        }
    }
    
    // Xuất dữ liệu dạng JSON
    public void saveToJson(List<Phone> phones, String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("[");
            
            for (int i = 0; i < phones.size(); i++) {
                Phone phone = phones.get(i);
                writer.println("  {");
                writer.println("    \"name\": \"" + escapeJson(phone.getName()) + "\",");
                writer.println("    \"price\": " + phone.getPrice() + ",");
                writer.println("    \"imageUrl\": \"" + escapeJson(phone.getImageUrl()) + "\",");
                writer.println("    \"productUrl\": \"" + escapeJson(phone.getProductUrl()) + "\",");
                writer.println("    \"specifications\": [");
                
                List<String> specs = phone.getSpecifications();
                for (int j = 0; j < specs.size(); j++) {
                    writer.print("      \"" + escapeJson(specs.get(j)) + "\"");
                    if (j < specs.size() - 1) {
                        writer.println(",");
                    } else {
                        writer.println();
                    }
                }
                
                writer.println("    ]");
                if (i < phones.size() - 1) {
                    writer.println("  },");
                } else {
                    writer.println("  }");
                }
            }
            
            writer.println("]");
        } catch (IOException e) {
            System.err.println("Error saving to JSON file: " + e.getMessage());
        }
    }
    
    // Escape các ký tự đặc biệt trong JSON
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}