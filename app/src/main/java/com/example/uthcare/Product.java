package com.example.uthcare;

public class Product {
    private int productId;
    private String productName;
    private double price;
    private String thumbnailUrl;
    private String category;

    // URL gốc server – bạn có thể đổi nếu server chạy trên địa chỉ khác
    private static final String BASE_URL = "http://192.168.1.4:3000";

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

    /**
     * Trả về đường dẫn ảnh đầy đủ, ví dụ:
     * http://192.168.1.4:3000/uploads/thumbnailUrl
     */


    public String getThumbnailUrl() {
        String BASE_URL = "http://192.168.1.4:3000"; // Cập nhật đúng IP
        if (thumbnailUrl == null) return "";

        if (thumbnailUrl.startsWith("http")) {
            return thumbnailUrl;
        }

        // Đảm bảo không bị lỗi //uploads/uploads
        if (!thumbnailUrl.startsWith("/uploads")) {
            thumbnailUrl = "/uploads/" + thumbnailUrl;
        }

        return BASE_URL + thumbnailUrl;
    }



    public String getCategory() {
        return category;
    }
}
