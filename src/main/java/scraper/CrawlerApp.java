package scraper;

import scraper.config.ConfigLoader;
import scraper.crawler.Crawler;
import scraper.crawler.laptop.CellphoneSLCrawler;
import scraper.crawler.laptop.Laptop88Crawler;
import scraper.crawler.smartphone.CellphoneSCrawler;
import scraper.crawler.smartphone.HoangHaMobileCrawler;
import scraper.model.Product;
import scraper.util.JsonSerializer;

import java.util.ArrayList;
import java.util.List;

public class CrawlerApp {
    public static void main(String[] args) {
        List<Crawler> crawlers = new ArrayList<>();
        try {
            crawlers.add(new CellphoneSCrawler(ConfigLoader.loadConfig("cellphones_smartphone.properties")));
            crawlers.add(new HoangHaMobileCrawler(ConfigLoader.loadConfig("hoanghamobile.properties")));
            crawlers.add(new CellphoneSLCrawler(ConfigLoader.loadConfig("cellphones_laptop.properties")));
            crawlers.add(new Laptop88Crawler(ConfigLoader.loadConfig("laptop88.properties")));

            List<Product> smartphones = new ArrayList<>();
            List<Product> laptops = new ArrayList<>();

            for (Crawler crawler : crawlers) {
                List<Product> products = crawler.crawl(20); // Crawl 2 products per site
                for (Product product : products) {
                    if ("Smartphone".equals(product.getCategoryData().get("category"))) {
                        smartphones.add(product);
                    } else if ("Laptop".equals(product.getCategoryData().get("category"))) {
                        laptops.add(product);
                    }
                }
                crawler.close();
            }

            JsonSerializer.saveToJson(smartphones, "smartphones.json");
            JsonSerializer.saveToJson(laptops, "laptops.json");

            System.out.println("Total smartphones scraped: " + smartphones.size());
            System.out.println("Total laptops scraped: " + laptops.size());
        } catch (Exception e) {
            System.err.println("Error running crawlers: " + e.getMessage());
            crawlers.forEach(Crawler::close);
        }
    }
}