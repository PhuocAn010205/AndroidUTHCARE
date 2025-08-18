package com.example.uthcare;

public class OrderItem {
    private int productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private String thumbnailUrl;

    public OrderItem(int productId, String productName, int quantity, double unitPrice, String thumbnailUrl) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}