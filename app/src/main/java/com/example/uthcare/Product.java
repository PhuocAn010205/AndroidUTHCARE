package com.example.uthcare;

public class Product {
    private int productId;
    private String productName;
    private double price;
    private String thumbnailUrl;
    private String category;
    private String description;

    private static final String BASE_URL = "http://192.168.1.3:3000";

    public Product(int productId, String productName, double price, String thumbnailUrl, String category, String description) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl != null ? thumbnailUrl : "";
        this.category = category;
        this.description = description != null ? description : "Không có mô tả";
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getThumbnailUrl() {
        if (thumbnailUrl == null || thumbnailUrl.trim().isEmpty()) return "";
        return thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://") ? thumbnailUrl : BASE_URL + thumbnailUrl;
    }
}