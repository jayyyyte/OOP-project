package scraper.model;

import java.util.Map;
import java.util.HashMap;

public class Smartphone extends AbstractProduct {
    private String operatingSystem;
    private String chipset;
    private String ram;
    private String storage;
    private String screenSize;
    private String screenResolution;
    private String mainCamera;
    private String selfieCamera;
    private String battery;

    public Smartphone() {
        super();
    }

    // Getters and setters for smartphone-specific attributes
    public String getOperatingSystem() {return operatingSystem;}
    public void setOperatingSystem(String operatingSystem) {this.operatingSystem = operatingSystem;}
    public String getChipset() {return chipset;}
    public void setChipset(String chipset) {this.chipset = chipset;}
    public String getRam() {return ram;}
    public void setRam(String ram) {this.ram = ram;}
    public String getStorage() {return storage;}
    public void setStorage(String storage) {this.storage = storage;}
    public String getScreenSize() {return screenSize;}
    public void setScreenSize(String screenSize) {this.screenSize = screenSize;}
    public String getScreenResolution() {return screenResolution;}
    public void setScreenResolution(String screenResolution) {this.screenResolution = screenResolution;}
    public String getMainCamera() {return mainCamera;}
    public void setMainCamera(String mainCamera) {this.mainCamera = mainCamera;}
    public String getSelfieCamera() {return selfieCamera;}
    public void setSelfieCamera(String selfieCamera) {this.selfieCamera = selfieCamera;}
    public String getBattery() {return battery;}
    public void setBattery(String battery) {this.battery = battery;}

    public Smartphone(String name, String productUrl, String imageUrl, double price, String priceCurrency,
                      String description, Map<String, String> specifications, double overallRating,
                      int reviewCount, Map<String, Object> categoryData) {
        super("Smartphone");
        this.name = name;
        this.productUrl = productUrl;
        this.imageUrl = imageUrl;
        this.price = price;
        this.priceCurrency = priceCurrency;
        this.description = description;
        this.specifications = specifications;
        this.overallRating = overallRating;
        this.reviewCount = reviewCount;
        this.categoryData = categoryData;
    }

    public void organizeSpecificationsIntoCategories() {
        Map<String, Object> techSpecs = new HashMap<>();
        Map<String, Object> displayInfo = new HashMap<>();
        Map<String, Object> cameraInfo = new HashMap<>();
        Map<String, Object> batteryInfo = new HashMap<>();
        Map<String, Object> connectivityInfo = new HashMap<>();
        Map<String, Object> physicalInfo = new HashMap<>();

        // Extract key specifications from the general specifications map
        for (Map.Entry<String, String> entry : getSpecifications().entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();

            // Sort specifications into smartphone categories
            if (key.contains("processor") || key.contains("cpu") || key.contains("ram") ||
                    key.contains("storage") || key.contains("memory") || key.contains("os") ||
                    key.contains("chip") || key.contains("system")) {

                techSpecs.put(entry.getKey(), value);
                // Also update specific fields for quick access
                if (key.contains("os") || key.contains("system")) {
                    this.operatingSystem = value;
                } else if (key.contains("processor") || key.contains("chip") || key.contains("cpu")) {
                    this.chipset = value;
                } else if (key.contains("ram")) {
                    this.ram = value;
                } else if (key.contains("storage") || key.contains("memory")) {
                    this.storage = value;
                }

            } else if (key.contains("display") || key.contains("screen") || key.contains("resolution")) {
                displayInfo.put(entry.getKey(), value);
                // Update specific fields
                if (key.contains("size")) {
                    this.screenSize = value;
                } else if (key.contains("resolution")) {
                    this.screenResolution = value;
                }

            } else if (key.contains("camera") || key.contains("selfie") || key.contains("video")) {
                cameraInfo.put(entry.getKey(), value);
                // Update specific fields
                if (key.contains("selfie") || key.contains("front")) {
                    this.selfieCamera = value;
                } else if (key.contains("camera") && !key.contains("selfie") && !key.contains("front")) {
                    this.mainCamera = value;
                }

            } else if (key.contains("battery") || key.contains("charging")) {
                batteryInfo.put(entry.getKey(), value);
                if (key.contains("battery") && !key.contains("charging")) {
                    this.battery = value;
                }

            } else if (key.contains("wifi") || key.contains("bluetooth") || key.contains("5g") ||
                    key.contains("nfc") || key.contains("usb") || key.contains("port")) {
                connectivityInfo.put(entry.getKey(), value);

            } else if (key.contains("dimension") || key.contains("weight") ||
                    key.contains("size") || key.contains("material")) {
                physicalInfo.put(entry.getKey(), value);
            }
        }

        // Add non-empty categories to categoryData
        if (!techSpecs.isEmpty()) addCategoryData("techSpecs", techSpecs);
        if (!displayInfo.isEmpty()) addCategoryData("displayInfo", displayInfo);
        if (!cameraInfo.isEmpty()) addCategoryData("cameraInfo", cameraInfo);
        if (!batteryInfo.isEmpty()) addCategoryData("batteryInfo", batteryInfo);
        if (!connectivityInfo.isEmpty()) addCategoryData("connectivityInfo", connectivityInfo);
        if (!physicalInfo.isEmpty()) addCategoryData("physicalInfo", physicalInfo);

        // Extract brand for better search organization
        if (getName() != null && !getName().isEmpty()) {
            String lowerName = getName().toLowerCase();
            if (lowerName.contains("samsung")) {
                addCategoryData("brand", "Samsung");
            } else if (lowerName.contains("apple") || lowerName.contains("iphone")) {
                addCategoryData("brand", "Apple");
            } else if (lowerName.contains("xiaomi")) {
                addCategoryData("brand", "Xiaomi");
            } else if (lowerName.contains("oppo")) {
                addCategoryData("brand", "Oppo");
            } else if (lowerName.contains("vivo")) {
                addCategoryData("brand", "Vivo");
            } else if (lowerName.contains("nokia")) {
                addCategoryData("brand", "Nokia");
            } else if (lowerName.contains("sony")) {
                addCategoryData("brand", "Sony");
            } else if (lowerName.contains("google") || lowerName.contains("pixel")) {
                addCategoryData("brand", "Google");
            }
        }
    }
}