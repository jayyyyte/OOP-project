package main.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class FptShopCrawler {
    // Danh sách User-Agent đa dạng
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Safari/605.1.15",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (iPad; CPU OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/123.0.0.0 Mobile/15E148 Safari/604.1"
    };
    
    // Danh sách Referer để luân phiên sử dụng
    private static final String[] REFERERS = {
        "https://www.google.com/",
        "https://www.bing.com/",
        "https://www.facebook.com/",
        "https://www.youtube.com/",
        "https://vn.yahoo.com/"
    };
    
    // Timeout cho việc kết nối
    private static final int TIMEOUT = 30000; // 30 seconds
    
    // Tạo một random object để sinh số ngẫu nhiên
    private static final Random random = new Random();
    
    // Danh sách các proxy có thể sử dụng
    private List<ProxyInfo> proxyList;
    
    // Constructor mặc định
    public FptShopCrawler() {
        this.proxyList = new ArrayList<>();
        // Thêm proxy mẫu - cần thay thế bằng proxy thực của bạn
        // this.proxyList.add(new ProxyInfo("103.126.12.196", 8080, null, null));
    }
    
    // Constructor cho phép truyền danh sách proxy
    public FptShopCrawler(List<ProxyInfo> proxyList) {
        this.proxyList = proxyList;
    }
    
    // Thêm một proxy vào danh sách
    public void addProxy(String host, int port, String username, String password) {
        this.proxyList.add(new ProxyInfo(host, port, username, password));
    }
    
    // Phương thức chính để crawl các điện thoại từ một URL
    public List<Phone> crawlPhones(String url) {
        List<Phone> phones = new ArrayList<>();
        
        try {
            // Chọn ngẫu nhiên một User-Agent và Referer
            String userAgent = getRandomUserAgent();
            String referer = getRandomReferer();
            
            System.out.println("Connecting to: " + url);
            System.out.println("Using User-Agent: " + userAgent);
            
            // Lấy proxy để sử dụng (nếu có)
            ProxyInfo proxy = getRandomProxy();
            
            // Thiết lập kết nối với các header giống trình duyệt thật
            Document doc;
            if (proxy != null) {
                System.out.println("Using proxy: " + proxy.getHost() + ":" + proxy.getPort());
                
                if (proxy.hasAuthentication()) {
                    // Sử dụng proxy có xác thực
                    String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                        (proxy.getUsername() + ":" + proxy.getPassword()).getBytes());
                    
                    doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Sec-Fetch-Dest", "document")
                        .header("Sec-Fetch-Mode", "navigate")
                        .header("Sec-Fetch-Site", "none")
                        .header("Sec-Fetch-User", "?1")
                        .header("Cache-Control", "max-age=0")
                        .header("Proxy-Authorization", authHeader)
                        .referrer(referer)
                        .proxy(proxy.getHost(), proxy.getPort())
                        .timeout(TIMEOUT)
                        .followRedirects(true)
                        .get();
                } else {
                    // Sử dụng proxy không có xác thực
                    doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Sec-Fetch-Dest", "document")
                        .header("Sec-Fetch-Mode", "navigate")
                        .header("Sec-Fetch-Site", "none")
                        .header("Sec-Fetch-User", "?1")
                        .header("Cache-Control", "max-age=0")
                        .referrer(referer)
                        .proxy(proxy.getHost(), proxy.getPort())
                        .timeout(TIMEOUT)
                        .followRedirects(true)
                        .get();
                }
            } else {
                // Không sử dụng proxy
                doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Cache-Control", "max-age=0")
                    .referrer(referer)
                    .cookie("cookieConsent", "true") // Cookie chấp nhận điều khoản
                    .timeout(TIMEOUT)
                    .followRedirects(true)
                    .get();
            }
            
            System.out.println("Connected successfully to: " + url);
            System.out.println("Page title: " + doc.title());
            
            // Kiểm tra xem có bị chặn không
            if (doc.title().contains("blocked") || doc.body().text().contains("Sorry, you have been blocked")) {
                System.err.println("Access blocked by the website. Try using a different proxy or wait before retrying.");
                return phones;
            }
            
            // Debug page structure
            if (doc.body().text().length() < 1000) {
                System.out.println("Page content seems minimal, might be blocked. Content: " + doc.body().text());
            }
            
            // Trích xuất danh sách sản phẩm (thử nhiều selector khác nhau)
            Elements productElements = doc.select("div.cdt-product");
            
            if (productElements.isEmpty()) {
                System.out.println("No products found with selector 'div.cdt-product'. Trying alternative selectors...");
                
                // Thử các selector khác
                productElements = doc.select("div.product-item");
                
                if (productElements.isEmpty()) {
                    productElements = doc.select("div.card-phone");
                }
                
                if (productElements.isEmpty()) {
                    productElements = doc.select("div.card-product");
                }
                
                if (productElements.isEmpty()) {
                    // Thử các selector chung hơn
                    productElements = doc.select("div[class*=product]");
                }
                
                if (productElements.isEmpty()) {
                    System.out.println("Failed to find products. HTML structure sample: " + 
                                      doc.body().text().substring(0, Math.min(500, doc.body().text().length())));
                    return phones;
                }
            }
            
            System.out.println("Found " + productElements.size() + " products");
            
            int count = 0;
            for (Element productElement : productElements) {
                if (count >= 10) {  // Giới hạn số lượng sản phẩm để tránh bị chặn
                    System.out.println("Reached product crawl limit (10 products). Stopping to avoid being blocked.");
                    break;
                }
                
                Phone phone = extractPhoneInfo(productElement);
                if (phone != null) {
                    phones.add(phone);
                    count++;
                    
                    // Thêm độ trễ ngẫu nhiên giữa các yêu cầu để tránh bị chặn
                    int delay = 2000 + random.nextInt(3000);  // 2-5 giây
                    System.out.println("Waiting for " + delay + "ms before next request");
                    Thread.sleep(delay);
                }
            }
            
            System.out.println("Crawled " + phones.size() + " phones successfully.");
            
        } catch (IOException e) {
            System.err.println("Error crawling " + url + ": " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread sleep interrupted: " + e.getMessage());
        }
        
        return phones;
    }
    
    // Trích xuất thông tin điện thoại từ một phần tử HTML
    private Phone extractPhoneInfo(Element productElement) {
        try {
            // Trích xuất tên sản phẩm (thử nhiều selector)
            String name = "";
            if (!productElement.select("h3.cdt-product__name").isEmpty()) {
                name = productElement.select("h3.cdt-product__name").text();
            } else if (!productElement.select("h3.card-name").isEmpty()) {
                name = productElement.select("h3.card-name").text();
            } else if (!productElement.select("h3").isEmpty()) {
                name = productElement.select("h3").first().text();
            } else if (!productElement.select("[class*=name]").isEmpty()) {
                name = productElement.select("[class*=name]").first().text();
            } else {
                System.out.println("Could not find product name element");
                return null;
            }
            
            // Trích xuất URL sản phẩm (thử nhiều cách)
            String productUrl = "";
            Element linkElement = productElement.select("a").first();
            if (linkElement != null) {
                productUrl = linkElement.attr("href");
                // Thêm domain nếu là đường dẫn tương đối
                if (productUrl.startsWith("/")) {
                    productUrl = "https://fptshop.com.vn" + productUrl;
                }
            } else {
                System.out.println("Could not find product URL for: " + name);
                return null;
            }
            
            // Trích xuất giá sản phẩm (thử nhiều selector)
            String priceText = "";
            if (!productElement.select("div.cdt-product__price").isEmpty()) {
                priceText = productElement.select("div.cdt-product__price").text();
            } else if (!productElement.select("div.price").isEmpty()) {
                priceText = productElement.select("div.price").text();
            } else if (!productElement.select("[class*=price]").isEmpty()) {
                priceText = productElement.select("[class*=price]").first().text();
            } else {
                System.out.println("Could not find price element for: " + name);
                priceText = "0";
            }
            double price = parsePrice(priceText);
            
            // Trích xuất URL hình ảnh (thử nhiều cách)
            String imageUrl = "";
            Element imgElement = productElement.select("img").first();
            if (imgElement != null) {
                imageUrl = imgElement.attr("data-src");
                if (imageUrl.isEmpty()) {
                    imageUrl = imgElement.attr("src");
                }
                if (imageUrl.isEmpty()) {
                    imageUrl = imgElement.attr("data-original");
                }
            }
            
            System.out.println("Found product: " + name + " | Price: " + price);
            
            // Thêm độ trễ ngẫu nhiên trước khi lấy chi tiết
            int delay = 3000 + random.nextInt(2000);  // 3-5 giây
            System.out.println("Waiting for " + delay + "ms before getting details");
            Thread.sleep(delay);
            
            // Trích xuất thông số kỹ thuật
            List<String> specs = new ArrayList<>();
            try {
                Document productDoc = getProductDetail(productUrl);
                if (productDoc != null) {
                    specs = extractSpecifications(productDoc);
                } else {
                    System.out.println("Could not load detail page for: " + name);
                }
            } catch (Exception e) {
                System.err.println("Error getting product details for " + name + ": " + e.getMessage());
            }
            
            return new Phone(name, price, imageUrl, productUrl, specs);
            
        } catch (Exception e) {
            System.err.println("Error extracting phone info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Lấy trang chi tiết sản phẩm
    private Document getProductDetail(String url) {
        try {
            // Chọn ngẫu nhiên User-Agent và proxy
            String userAgent = getRandomUserAgent();
            ProxyInfo proxy = getRandomProxy();
            
            System.out.println("Getting details from: " + url);
            
            Document doc;
            if (proxy != null) {
                System.out.println("Using proxy for details: " + proxy.getHost() + ":" + proxy.getPort());
                
                if (proxy.hasAuthentication()) {
                    String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                        (proxy.getUsername() + ":" + proxy.getPassword()).getBytes());
                    
                    doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .header("Referer", "https://fptshop.com.vn/")
                        .header("Proxy-Authorization", authHeader)
                        .proxy(proxy.getHost(), proxy.getPort())
                        .timeout(TIMEOUT)
                        .followRedirects(true)
                        .get();
                } else {
                    doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .header("Referer", "https://fptshop.com.vn/")
                        .proxy(proxy.getHost(), proxy.getPort())
                        .timeout(TIMEOUT)
                        .followRedirects(true)
                        .get();
                }
            } else {
                doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Referer", "https://fptshop.com.vn/")
                    .timeout(TIMEOUT)
                    .followRedirects(true)
                    .get();
            }
            
            return doc;
        } catch (IOException e) {
            System.err.println("Error getting product detail: " + e.getMessage());
            return null;
        }
    }
    
    // Trích xuất thông số kỹ thuật từ trang chi tiết
    private List<String> extractSpecifications(Document doc) {
        List<String> specs = new ArrayList<>();
        
        if (doc != null) {
            try {
                // Thử nhiều selector để tìm thông số kỹ thuật
                Elements specElements = doc.select("div.specifications-param__item");
                
                if (specElements.isEmpty()) {
                    // Thử các selector khác
                    specElements = doc.select("table.specs-table tr");
                }
                
                if (specElements.isEmpty()) {
                    specElements = doc.select("div.st-param__item");
                }
                
                if (specElements.isEmpty()) {
                    specElements = doc.select("[class*=specs] li");
                }
                
                for (Element specElement : specElements) {
                    String specName = "";
                    String specValue = "";
                    
                    if (specElement.select("p.specifications-param__name").size() > 0) {
                        specName = specElement.select("p.specifications-param__name").text();
                        specValue = specElement.select("p.specifications-param__value").text();
                    } else if (specElement.select("td").size() >= 2) {
                        specName = specElement.select("td").get(0).text();
                        specValue = specElement.select("td").get(1).text();
                    } else if (specElement.select(".k-label").size() > 0) {
                        specName = specElement.select(".k-label").text();
                        specValue = specElement.select(".k-value").text();
                    } else if (specElement.select("strong").size() > 0) {
                        specName = specElement.select("strong").text();
                        specValue = specElement.text().replace(specName, "").trim();
                    }
                    
                    if (!specName.isEmpty() && !specValue.isEmpty()) {
                        specs.add(specName + ": " + specValue);
                    }
                }
                
                System.out.println("Extracted " + specs.size() + " specifications");
                
                // Nếu không tìm thấy thông số kỹ thuật, thử tìm trong mô tả sản phẩm
                if (specs.isEmpty()) {
                    Elements descElements = doc.select("div.product-description li");
                    for (Element descElement : descElements) {
                        String text = descElement.text().trim();
                        if (!text.isEmpty()) {
                            specs.add("Description: " + text);
                        }
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Error extracting specifications: " + e.getMessage());
            }
        }
        
        return specs;
    }
    
    // Chuyển đổi chuỗi giá thành số
    private double parsePrice(String priceText) {
        try {
            // Loại bỏ ký hiệu tiền tệ, dấu chấm và các ký tự không phải số
            String cleanPrice = priceText.replaceAll("[^0-9]", "");
            if (cleanPrice.isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing price: " + priceText);
            return 0.0;
        }
    }
    
    // Lấy ngẫu nhiên một User-Agent
    private String getRandomUserAgent() {
        return USER_AGENTS[random.nextInt(USER_AGENTS.length)];
    }
    
    // Lấy ngẫu nhiên một Referer
    private String getRandomReferer() {
        return REFERERS[random.nextInt(REFERERS.length)];
    }
    
    // Lấy ngẫu nhiên một proxy từ danh sách
    private ProxyInfo getRandomProxy() {
        if (proxyList.isEmpty()) {
            return null;
        }
        return proxyList.get(random.nextInt(proxyList.size()));
    }
    
    // Kiểm tra xem proxy có hoạt động không
    public boolean testProxy(String host, int port) {
        try {
            Jsoup.connect("https://www.google.com")
                .proxy(host, port)
                .timeout(5000)
                .get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Lớp nội bộ để lưu trữ thông tin proxy
    public static class ProxyInfo {
        private String host;
        private int port;
        private String username;
        private String password;
        
        public ProxyInfo(String host, int port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }
        
        public String getHost() {
            return host;
        }
        
        public int getPort() {
            return port;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public boolean hasAuthentication() {
            return username != null && !username.isEmpty() && password != null;
        }
    }
}