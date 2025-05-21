package scraper.crawler;

import scraper.model.Product;
import java.util.List;
import scraper.model.AbstractProduct;

//Interface defining the common operations for all crawlers
public interface Crawler {
    void initialize();      //Initialize the crawler with required configurations

    //Crawl the product list page to get basic product information
    //@return List of product URLs to be crawled in detail
    List<String> crawlProductListPage();


    /* Crawl a specific product detail page to get complete product information
       @param productUrl URL of the product detail page
       @return Fully populated product object*/
    AbstractProduct crawlProductDetailPage(String productUrl);


     /* Execute the complete crawling process
       @return List of crawled products   */
    List<AbstractProduct> crawl();

    /* Get the name of the website being crawled
      @return Website name  */
    String getWebsiteName();

    /* Get the base URL of the website
       @return Base URL */
    String getBaseUrl();

    List<Product> crawl(int maxProducts);
    void close();       //Clean up resources after crawling is complete
}