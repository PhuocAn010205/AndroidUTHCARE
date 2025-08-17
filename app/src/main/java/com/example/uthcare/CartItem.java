package com.example.uthcare;

import java.io.Serializable;

public class CartItem implements Serializable {
    private int id; // cart item ID
    private int productId; // product ID
    private int quantity;
    private String productName;
    private double price;
    private String thumbnailUrl;
    private boolean isSelected;

    public CartItem(int id, int productId, int quantity, String productName, double price, String thumbnailUrl) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
        this.isSelected = false;
    }

    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}