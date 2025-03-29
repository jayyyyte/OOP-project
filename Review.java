package com.product.model;

public class Review {
    private String userId;
    private int rating;
    private String comment;

    public Review(String userId, int rating, String comment) {
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters
    public String getUserId() { return userId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }

    @Override
    public String toString() {
        return "Review [userId=" + userId + ", rating=" + rating + ", comment=" + comment + "]";
    }
}