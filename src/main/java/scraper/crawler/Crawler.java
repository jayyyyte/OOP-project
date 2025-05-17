package scraper.crawler;

import scraper.model.Product;
import java.util.List;

public interface Crawler {
    List<Product> crawl(int maxProducts);
    void close();
}