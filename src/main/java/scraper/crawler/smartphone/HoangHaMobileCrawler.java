package scraper.crawler.smartphone;

import scraper.config.WebsiteConfig;
import scraper.crawler.AbstractCrawler;
import scraper.factory.SmartphoneFactory;

public class HoangHaMobileCrawler extends AbstractCrawler {
    public HoangHaMobileCrawler(WebsiteConfig config) {
        super(config, new SmartphoneFactory());
    }
}