package com.example.uthcare;

public class CartItem {
    private int id;
    private int quantity;
    private String productName;
    private double price;
    private String thumbnailUrl;

    public CartItem(int id, int quantity, String productName, double price, String thumbnailUrl) {
        this.id = id;
        this.quantity = quantity;
        this.productName = productName;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    // Thêm phương thức setQuantity để cập nhật số lượng
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}