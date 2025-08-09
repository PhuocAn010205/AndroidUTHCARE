package com.example.uthcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONException;
import org.json.JSONObject;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String CART_URL = "http://192.168.1.5:3000/cart";
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnCartTop = findViewById(R.id.btn_cart_top);
        ImageView ivThumbnail = findViewById(R.id.iv_thumbnail);
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvPrice = findViewById(R.id.tv_price);
        TextView tvCategory = findViewById(R.id.tv_category);
        TextView tvProductType = findViewById(R.id.tv_product_type);
        TextView tvDescription = findViewById(R.id.tv_description);
        Button btnAdd = findViewById(R.id.btn_add);
        LinearLayout layoutDynamicInfo = findViewById(R.id.layout_dynamic_info);
        TextView tvDynamicName = findViewById(R.id.tv_dynamic_name);
        TextView tvDynamicPrice = findViewById(R.id.tv_dynamic_price);
        TextView tvDynamicType = findViewById(R.id.tv_dynamic_type);
        TextView tvDynamicQuantity = findViewById(R.id.tv_dynamic_quantity);
        Button btnDecrease = findViewById(R.id.btn_decrease);
        Button btnIncrease = findViewById(R.id.btn_increase);
        Button btnAddToCart = findViewById(R.id.btn_add_to_cart);
        Button btnBuyNow = findViewById(R.id.btn_buy_now);

        // Nhận dữ liệu từ Intent
        String name = getIntent().getStringExtra("name");
        double price = getIntent().getDoubleExtra("price", 0);
        String category = getIntent().getStringExtra("category");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String description = getIntent().getStringExtra("description");
        String productType = getIntent().getStringExtra("product_type");
        int productId = getIntent().getIntExtra("productId", -1);

        // Gán dữ liệu
        tvName.setText(name);
        tvPrice.setText(String.format("%,.0f đ", price));
        tvCategory.setText(category);
        tvProductType.setText(productType != null ? productType : "Không có loại");
        if (description != null) {
            tvDescription.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT));
        } else {
            tvDescription.setText("Không có mô tả");
        }
        Glide.with(this).load(imageUrl).placeholder(R.drawable.placeholder).into(ivThumbnail);

        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // Nút giỏ hàng ở trên
        btnCartTop.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        // Nút Thêm để hiển thị thông tin động
        btnAdd.setOnClickListener(v -> {
            layoutDynamicInfo.setVisibility(View.VISIBLE);
            tvDynamicName.setText(name);
            tvDynamicPrice.setText(String.format("%,.0f đ", price));
            tvDynamicType.setText(productType != null ? productType : "Không có loại");
            tvDynamicQuantity.setText(String.valueOf(quantity));
            btnAdd.setVisibility(View.GONE); // Ẩn nút Thêm sau khi bấm
        });

        // Nút Giảm số lượng
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvDynamicQuantity.setText(String.valueOf(quantity));
            }
        });

        // Nút Tăng số lượng
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvDynamicQuantity.setText(String.valueOf(quantity));
        });

        // Xử lý nút Thêm vào giỏ
        btnAddToCart.setOnClickListener(v -> {
            if (layoutDynamicInfo.getVisibility() == View.VISIBLE) {
                addToCart(getUserId(), productId, quantity);
            } else {
                Toast.makeText(this, "Vui lòng bấm Thêm để chọn số lượng", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút Mua ngay
        btnBuyNow.setOnClickListener(v -> {
            if (layoutDynamicInfo.getVisibility() == View.VISIBLE) {
                addToCart(getUserId(), productId, quantity);
                Toast.makeText(this, "Chuyển đến trang thanh toán...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, CartActivity.class));
            } else {
                Toast.makeText(this, "Vui lòng bấm Thêm để chọn số lượng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getUserId() {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private void addToCart(int userId, int productId, int quantity) {
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("product_id", productId);
            jsonBody.put("quantity", quantity);
        } catch (JSONException e) {
            Toast.makeText(this, "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, CART_URL, jsonBody,
                response -> Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Lỗi thêm vào giỏ!", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(this).add(request);
    }
}