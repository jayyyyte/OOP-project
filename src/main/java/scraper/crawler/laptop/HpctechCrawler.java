package scraper.crawler.laptop;

import scraper.config.WebsiteConfig;
import scraper.crawler.AbstractCrawler;
import scraper.factory.LaptopFactory;

public class HpctechCrawler extends AbstractCrawler {
    public HpctechCrawler(WebsiteConfig config) {
        super(config, new LaptopFactory());
    }
}