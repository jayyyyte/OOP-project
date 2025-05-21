package scraper.crawler.smartphone;

import scraper.config.WebsiteConfig;
import scraper.crawler.AbstractCrawler;
import scraper.factory.SmartphoneFactory;

public class CellphoneSCrawler extends AbstractCrawler {
    public CellphoneSCrawler(WebsiteConfig config) {
        super(config, new SmartphoneFactory());
    }
}