package scraper.model;

import java.util.HashMap;
import java.util.Map;

public class Product extends AbstractProduct {
    private String name;
    private String productUrl;
    private String imageUrl;
    private double price;
    private String priceCurrency = "VND";
    private String description;
    private Map<String, String> specifications;
    private double overallRating;
    private int reviewCount;
    private Map<String, Object> categoryData;

    public static double parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return 0.0;
        }
        String cleanPrice = priceText.replaceAll("[^\\d.,]", "");
        cleanPrice = cleanPrice.replace(',', '.');
        int lastDotIndex = cleanPrice.lastIndexOf('.');
        if (lastDotIndex > -1 && cleanPrice.indexOf('.') != lastDotIndex) {
            StringBuilder sb = new StringBuilder(cleanPrice);
            for (int i = 0; i < lastDotIndex; i++) {
                if (sb.charAt(i) == '.') {
                    sb.setCharAt(i, '\0');
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

    @Override
    public void organizeSpecificationsIntoCategories() {
        Map<String, Object> techSpecs = new HashMap<>();
        Map<String, Object> displayInfo = new HashMap<>();
        Map<String, Object> cameraInfo = new HashMap<>();
        Map<String, Object> batteryInfo = new HashMap<>();
        Map<String, Object> connectivityInfo = new HashMap<>();

        for (Map.Entry<String, String> entry : specifications.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();
            if (key.contains("processor") || key.contains("cpu") || key.contains("ram") || key.contains("storage") ||
                    key.contains("memory") || key.contains("os") || key.contains("chip")) {
                techSpecs.put(entry.getKey(), value);
            } else if (key.contains("display") || key.contains("screen") || key.contains("resolution")) {
                displayInfo.put(entry.getKey(), value);
            } else if (key.contains("camera") || key.contains("selfie") || key.contains("video")) {
                cameraInfo.put(entry.getKey(), value);
            } else if (key.contains("battery") || key.contains("charging")) {
                batteryInfo.put(entry.getKey(), value);
            } else if (key.contains("wifi") || key.contains("bluetooth") || key.contains("5g") ||
                    key.contains("nfc") || key.contains("usb") || key.contains("port")) {
                connectivityInfo.put(entry.getKey(), value);
            }
        }

        if (!techSpecs.isEmpty()) categoryData.put("techSpecs", techSpecs);
        if (!displayInfo.isEmpty()) categoryData.put("displayInfo", displayInfo);
        if (!cameraInfo.isEmpty()) categoryData.put("cameraInfo", cameraInfo);
        if (!batteryInfo.isEmpty()) categoryData.put("batteryInfo", batteryInfo);
        if (!connectivityInfo.isEmpty()) categoryData.put("connectivityInfo", connectivityInfo);

        if (name != null && !name.isEmpty()) {
            String lowerName = name.toLowerCase();
            if (lowerName.contains("samsung")) categoryData.put("brand", "Samsung");
            else if (lowerName.contains("apple") || lowerName.contains("iphone")) categoryData.put("brand", "Apple");
            else if (lowerName.contains("xiaomi")) categoryData.put("brand", "Xiaomi");
            else if (lowerName.contains("oppo")) categoryData.put("brand", "Oppo");
            else if (lowerName.contains("vivo")) categoryData.put("brand", "Vivo");
            else if (lowerName.contains("nokia")) categoryData.put("brand", "Nokia");
            else if (lowerName.contains("sony")) categoryData.put("brand", "Sony");
            else if (lowerName.contains("google") || lowerName.contains("pixel")) categoryData.put("brand", "Google");
        }
    }

    @Override
    public String toString() {
        return String.format("Product{name='%s', price=%.2f %s, rating=%.1f, reviews=%d}",
                name, price, priceCurrency, overallRating, reviewCount);
    }
}