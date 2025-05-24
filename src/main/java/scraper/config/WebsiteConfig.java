package scraper.config;

public class WebsiteConfig {
    private final String url;
    private final String targetCategory;
    private final String productContainer;
    private final String name;
    private final String price;
    private final String image;
    private final String detailUrl;
    private final String description;
    private final String specsButton;
    private final String specsTable;
    private final String specsRow;
    private final String specsLabel;
    private final String specsValue;
    private final String altSpecsRow;
    private final String rating;
    private final String reviewCount;
    private final String reviewContainer;
    private final String reviewAuthor;
    private final String reviewText;
    private final String reviewRating;
    private final String reviewDate;

    public WebsiteConfig(String url, String targetCategory, String productContainer, String name, String price,
                         String image, String detailUrl, String description, String specsButton, String specsTable, String specsRow,
                         String specsLabel, String specsValue, String altSpecsRow, String rating, String reviewCount,
                         String reviewContainer, String reviewAuthor, String reviewText, String reviewRating,
                         String reviewDate) {
        this.url = url;
        this.targetCategory = targetCategory;
        this.productContainer = productContainer;
        this.name = name;
        this.price = price;
        this.image = image;
        this.detailUrl = detailUrl;
        this.description = description;
        this.specsButton = specsButton;
        this.specsTable = specsTable;
        this.specsRow = specsRow;
        this.specsLabel = specsLabel;
        this.specsValue = specsValue;
        this.altSpecsRow = altSpecsRow;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.reviewContainer = reviewContainer;
        this.reviewAuthor = reviewAuthor;
        this.reviewText = reviewText;
        this.reviewRating = reviewRating;
        this.reviewDate = reviewDate;
    }

    public void validate() {
        if (url == null || productContainer == null || name == null) {
            throw new IllegalStateException("Invalid WebsiteConfig: missing required fields");
        }
    }

    // Getters
    public String getUrl() { return url; }
    public String getTargetCategory() { return targetCategory; }
    public String getProductContainer() { return productContainer; }
    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getImage() { return image; }
    public String getDetailUrl() { return detailUrl; }
    public String getDescription() { return description; }
    public String getSpecsButton() { return specsButton;}
    public String getSpecsTable() { return specsTable; }
    public String getSpecsRow() { return specsRow; }
    public String getSpecsLabel() { return specsLabel; }
    public String getSpecsValue() { return specsValue; }
    public String getAltSpecsRow() { return altSpecsRow; }
    public String getRating() { return rating; }
    public String getReviewCount() { return reviewCount; }
    public String getReviewContainer() { return reviewContainer; }
    public String getReviewAuthor() { return reviewAuthor; }
    public String getReviewText() { return reviewText; }
    public String getReviewRating() { return reviewRating; }
    public String getReviewDate() { return reviewDate; }
}