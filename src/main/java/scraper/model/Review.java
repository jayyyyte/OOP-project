package scraper.model;

public class Review {
    private String author;
    private String content;
    private double rating;
    private String date;
    private boolean verifiedPurchase;

    public Review(String author, String content, double rating, String date, boolean verifiedPurchase) {
        this.author = author;
        this.content = content;
        this.rating = rating;
        this.date = date;
        this.verifiedPurchase = verifiedPurchase;
    }

    // Getters and setters
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public boolean isVerifiedPurchase() { return verifiedPurchase; }
    public void setVerifiedPurchase(boolean verifiedPurchase) { this.verifiedPurchase = verifiedPurchase; }
}