package com.example.uthcare;

public class Product {
    private int productId;
    private String productName;
    private double price;
    private String thumbnailUrl;
    private String category;

    // URL server – nhớ đổi đúng IP khi chạy trên thiết bị thật
    private static final String BASE_URL = "http://192.168.1.3:3000";

    public Product(int productId, String productName, double price, String thumbnailUrl, String category) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
        this.category = category;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    /**
     * Trả về URL ảnh đầy đủ, ví dụ:
     * http://192.168.1.5:3000/uploads/thumbnail-xxx.jpg
     */
    public String getThumbnailUrl() {
        if (thumbnailUrl == null || thumbnailUrl.trim().isEmpty()) {
            return "";  // Không có ảnh
        }

        String fullPath;

        if (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://")) {
            fullPath = thumbnailUrl;
        } else {
            // Bảo đảm đường dẫn bắt đầu bằng /uploads
            if (!thumbnailUrl.startsWith("/uploads/")) {
                thumbnailUrl = "/uploads/" + thumbnailUrl;
            }
            fullPath = BASE_URL + thumbnailUrl;
        }



        return fullPath;
    }
}
