package scraper.crawler.laptop;

import scraper.config.WebsiteConfig;
import scraper.crawler.AbstractCrawler;
import scraper.factory.LaptopFactory;

public class CellphoneSLCrawler extends AbstractCrawler {
    public CellphoneSLCrawler(WebsiteConfig config) {
        super(config, new LaptopFactory());
    }
}