package scraper.crawler.laptop;

import scraper.config.WebsiteConfig;
import scraper.crawler.AbstractCrawler;
import scraper.factory.LaptopFactory;

public class Laptop88Crawler extends AbstractCrawler {
    public Laptop88Crawler(WebsiteConfig config) {
        super(config, new LaptopFactory());
    }
}