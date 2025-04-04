package main.crawler;

import java.util.List;

public class CrawlerManager {
    private HoangHaMobileCrawler hoangHaMobileCrawler;

    public CrawlerManager() {
        this.hoangHaMobileCrawler = new HoangHaMobileCrawler();
    }

    public List<Phone> crawlPhoneListFromHoangHaMobile(String categoryUrl) {
        return hoangHaMobileCrawler.crawlPhoneList(categoryUrl);
    }

    public Phone crawlPhoneDetailsFromHoangHaMobile(String productUrl) {
        return hoangHaMobileCrawler.crawlPhoneDetails(productUrl);
    }

    public void closeCrawlers() {
        hoangHaMobileCrawler.close();
    }
}