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

            List<Product> allProducts = new ArrayList<>();
            for (Crawler crawler : crawlers) {
                allProducts.addAll(crawler.crawl(2)); // Crawl 2 products per site
                crawler.close();
            }

            JsonSerializer.saveToJson(allProducts, "products.json");
            System.out.println("Total products scraped: " + allProducts.size());
        } catch (Exception e) {
            System.err.println("Error running crawlers: " + e.getMessage());
            crawlers.forEach(Crawler::close);
        }
    }
}