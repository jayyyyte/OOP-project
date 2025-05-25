package util;

import javafx.scene.image.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageCache {
    private static final String CACHE_DIR = "image_cache";
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static boolean isInitialized = false;

    public static void initialize() {
        if (isInitialized) return;
        
        try {
            // Create cache directory if it doesn't exist
            Path cachePath = Paths.get(CACHE_DIR);
            if (!Files.exists(cachePath)) {
                Files.createDirectories(cachePath);
            }
            System.out.println("ImageCache initialized. Cache directory: " + cachePath.toAbsolutePath());
            isInitialized = true;
        } catch (IOException e) {
            System.err.println("Error creating cache directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getFileExtension(String url) {
        // Extract the actual image URL from CDN URL if present
        String actualUrl = url;
        if (url.contains("/insecure/rs:fill:")) {
            // Extract the part after /plain/
            int plainIndex = url.indexOf("/plain/");
            if (plainIndex != -1) {
                actualUrl = url.substring(plainIndex + 7);
            }
        }
        
        int lastDot = actualUrl.lastIndexOf('.');
        if (lastDot != -1 && lastDot > actualUrl.lastIndexOf('/')) {
            return actualUrl.substring(lastDot);
        }
        return ".png"; // Default to PNG if no extension found
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<Image> getImage(String imageUrl) {
        if (!isInitialized) {
            initialize();
        }

        // Check if image is already in memory cache
        if (imageCache.containsKey(imageUrl)) {
            System.out.println("Image found in memory cache: " + imageUrl);
            return CompletableFuture.completedFuture(imageCache.get(imageUrl));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Extract original URL if it's a CDN URL
                String actualUrl = imageUrl;
                if (imageUrl.contains("/insecure/rs:fill:")) {
                    int plainIndex = imageUrl.indexOf("/plain/");
                    if (plainIndex != -1) {
                        actualUrl = imageUrl.substring(plainIndex + 7);
                    }
                }
                System.out.println("Using actual image URL: " + actualUrl);

                // Generate cache file name from SHA-256 hash of URL and preserve extension
                String ext = getFileExtension(actualUrl);
                String cacheFileName = sha256(actualUrl) + ext;
                Path cacheFilePath = Paths.get(CACHE_DIR, cacheFileName);
                System.out.println("Cache file path: " + cacheFilePath.toAbsolutePath());

                // Check if image exists in cache
                if (Files.exists(cacheFilePath)) {
                    System.out.println("Image found in disk cache: " + actualUrl);
                    Image image = new Image(cacheFilePath.toFile().toURI().toString(), true);
                    if (!image.isError()) {
                        imageCache.put(imageUrl, image);
                        return image;
                    }
                }

                System.out.println("Downloading image from URL: " + actualUrl);
                // Download and cache the image
                URL url = new URL(actualUrl);
                java.net.URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Referer", "https://cellphones.com.vn/");
                
                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(cacheFilePath.toFile())) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                System.out.println("Image downloaded successfully: " + actualUrl);
                // Load and cache the image
                Image image = new Image(cacheFilePath.toFile().toURI().toString(), true);
                if (!image.isError()) {
                    imageCache.put(imageUrl, image);
                    return image;
                } else {
                    System.err.println("Error loading image from cache: " + actualUrl);
                    return null;
                }

            } catch (Exception e) {
                System.err.println("Error loading image from URL: " + imageUrl);
                e.printStackTrace();
                return null;
            }
        }, executor);
    }

    public static void preloadImages(List<String> imageUrls) {
        if (!isInitialized) {
            initialize();
        }

        System.out.println("Starting to preload " + imageUrls.size() + " images...");
        for (String url : imageUrls) {
            if (url != null && !url.trim().isEmpty()) {
                getImage(url);
            } else {
                System.err.println("Skipping invalid image URL: " + url);
            }
        }
    }

    public static void clearCache() {
        try {
            Path cachePath = Paths.get(CACHE_DIR);
            if (Files.exists(cachePath)) {
                Files.walk(cachePath)
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
            imageCache.clear();
            System.out.println("Image cache cleared successfully");
        } catch (IOException e) {
            System.err.println("Error clearing cache: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 