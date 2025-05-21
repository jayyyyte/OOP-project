package scraper.model;

import java.util.Map;
import java.util.HashMap;

public class Laptop extends AbstractProduct {
    private String processor;
    private String ram;
    private String storage;
    private String graphicsCard;
    private String displaySize;
    private String displayResolution;
    private String operatingSystem;
    private String batteryLife;
    private String weight;
    private String ports;

    public Laptop(){
        super();
    }

    public Laptop(String name, String productUrl, String imageUrl, double price, String priceCurrency,
                  String description, Map<String, String> specifications, double overallRating,
                  int reviewCount, Map<String, Object> categoryData) {
        super("Laptop");
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

    // Getters and setters for laptop-specific attributes
    public String getProcessor() {return processor;}
    public void setProcessor(String processor) {this.processor = processor;}
    public String getRam() {return ram;}
    public void setRam(String ram) {this.ram = ram;}
    public String getStorage() {return storage;}
    public void setStorage(String storage) {this.storage = storage;}
    public String getGraphicsCard() {return graphicsCard;}
    public void setGraphicsCard(String graphicsCard) {this.graphicsCard = graphicsCard;}
    public String getDisplaySize() {return displaySize;}
    public void setDisplaySize(String displaySize) {this.displaySize = displaySize;}
    public String getDisplayResolution() {return displayResolution;}
    public void setDisplayResolution(String displayResolution) {this.displayResolution = displayResolution;}
    public String getOperatingSystem() {return operatingSystem;}
    public void setOperatingSystem(String operatingSystem) {this.operatingSystem = operatingSystem;}
    public String getBatteryLife() {return batteryLife;}
    public void setBatteryLife(String batteryLife) {this.batteryLife = batteryLife;}
    public String getWeight() {return weight;}
    public void setWeight(String weight) {this.weight = weight;}
    public String getPorts() {return ports;}
    public void setPorts() {this.ports = ports;}

    @Override
    public void organizeSpecificationsIntoCategories() {
        Map<String, Object> processorInfo = new HashMap<>();
        Map<String, Object> memoryInfo = new HashMap<>();
        Map<String, Object> displayInfo = new HashMap<>();
        Map<String, Object> graphicsInfo = new HashMap<>();
        Map<String, Object> storageInfo = new HashMap<>();
        Map<String, Object> batteryInfo = new HashMap<>();
        Map<String, Object> connectivityInfo = new HashMap<>();
        Map<String, Object> physicalInfo = new HashMap<>();
        Map<String, Object> osInfo = new HashMap<>();

        // Extract key specifications from the general specifications map
        for (Map.Entry<String, String> entry : getSpecifications().entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();

            // Sort specifications into laptop categories
            if (key.contains("processor") || key.contains("cpu") || key.contains("chip")) {
                processorInfo.put(entry.getKey(), value);
                this.processor = value;

            } else if (key.contains("ram") || key.contains("memory") && !key.contains("storage")) {
                memoryInfo.put(entry.getKey(), value);
                this.ram = value;

            } else if (key.contains("storage") || key.contains("ssd") || key.contains("hdd") || key.contains("disk")) {
                storageInfo.put(entry.getKey(), value);
                this.storage = value;

            } else if (key.contains("display") || key.contains("screen") || key.contains("resolution") || key.contains("monitor")) {
                displayInfo.put(entry.getKey(), value);

                if (key.contains("size") || (key.contains("display") && key.contains("inch"))) {
                    this.displaySize = value;
                } else if (key.contains("resolution")) {
                    this.displayResolution = value;
                }

            } else if (key.contains("graphics") || key.contains("gpu") || key.contains("vga")) {
                graphicsInfo.put(entry.getKey(), value);
                this.graphicsCard = value;

            } else if (key.contains("battery") || key.contains("power")) {
                batteryInfo.put(entry.getKey(), value);
                this.batteryLife = value;

            } else if (key.contains("wifi") || key.contains("bluetooth") || key.contains("port") ||
                    key.contains("usb") || key.contains("hdmi") || key.contains("ethernet") ||
                    key.contains("thunderbolt")) {
                connectivityInfo.put(entry.getKey(), value);

                if (key.contains("port")) {
                    this.ports = value;
                }

            } else if (key.contains("weight") || key.contains("dimension") || key.contains("material") ||
                    key.contains("color") || key.contains("size") && !key.contains("display")) {
                physicalInfo.put(entry.getKey(), value);

                if (key.contains("weight")) {
                    this.weight = value;
                }

            } else if (key.contains("os") || key.contains("operating system") || key.contains("windows") ||
                    key.contains("macos") || key.contains("linux")) {
                osInfo.put(entry.getKey(), value);
                this.operatingSystem = value;
            }
        }

        // Add non-empty categories to categoryData
        if (!processorInfo.isEmpty()) addCategoryData("processorInfo", processorInfo);
        if (!memoryInfo.isEmpty()) addCategoryData("memoryInfo", memoryInfo);
        if (!storageInfo.isEmpty()) addCategoryData("storageInfo", storageInfo);
        if (!displayInfo.isEmpty()) addCategoryData("displayInfo", displayInfo);
        if (!graphicsInfo.isEmpty()) addCategoryData("graphicsInfo", graphicsInfo);
        if (!batteryInfo.isEmpty()) addCategoryData("batteryInfo", batteryInfo);
        if (!connectivityInfo.isEmpty()) addCategoryData("connectivityInfo", connectivityInfo);
        if (!physicalInfo.isEmpty()) addCategoryData("physicalInfo", physicalInfo);
        if (!osInfo.isEmpty()) addCategoryData("osInfo", osInfo);

        // Extract brand for better search organization
        if (getName() != null && !getName().isEmpty()) {
            String lowerName = getName().toLowerCase();
            if (lowerName.contains("dell")) {
                addCategoryData("brand", "Dell");
            } else if (lowerName.contains("hp")) {
                addCategoryData("brand", "HP");
            } else if (lowerName.contains("lenovo")) {
                addCategoryData("brand", "Lenovo");
            } else if (lowerName.contains("asus")) {
                addCategoryData("brand", "Asus");
            } else if (lowerName.contains("acer")) {
                addCategoryData("brand", "Acer");
            } else if (lowerName.contains("apple") || lowerName.contains("macbook")) {
                addCategoryData("brand", "Apple");
            } else if (lowerName.contains("msi")) {
                addCategoryData("brand", "MSI");
            } else if (lowerName.contains("samsung")) {
                addCategoryData("brand", "Samsung");
            } else if (lowerName.contains("huawei")) {
                addCategoryData("brand", "Huawei");
            } else if (lowerName.contains("gigabyte")) {
                addCategoryData("brand", "Gigabyte");
            } else if (lowerName.contains("microsoft") || lowerName.contains("surface")) {
                addCategoryData("brand", "Microsoft");
            }
        }
    }
}