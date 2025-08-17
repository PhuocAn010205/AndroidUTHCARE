package com.example.uthcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
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
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String CART_URL = "http://10.0.2.2:3000/cart"; // Thay bằng 192.168.2.11 nếu cần
    private static final String BASE_URL = "http://10.0.2.2:3000"; // Thêm host
    private static final String TAG = "ProductDetailActivity";
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnCartTop = findViewById(R.id.btn_cart_top);
        ImageView ivThumbnail = findViewById(R.id.iv_thumbnail);
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvPrice = findViewById(R.id.tv_price);
        TextView tvCategory = findViewById(R.id.tv_category);
        TextView tvDescription = findViewById(R.id.tv_description);
        LinearLayout layoutDynamicInfo = findViewById(R.id.layout_dynamic_info);
        TextView tvDynamicName = findViewById(R.id.tv_dynamic_name);
        TextView tvDynamicPrice = findViewById(R.id.tv_dynamic_price);
        TextView tvDynamicQuantity = findViewById(R.id.tv_dynamic_quantity);
        ImageButton btnDecrease = findViewById(R.id.btn_decrease);
        ImageButton btnIncrease = findViewById(R.id.btn_increase);
        Button btnAddToCart = findViewById(R.id.btn_add_to_cart);
        Button btnBuyNow = findViewById(R.id.btn_buy_now);

        // Lấy dữ liệu từ Intent
        String name = getIntent().getStringExtra("name");
        double price = getIntent().getDoubleExtra("price", 0);
        String category = getIntent().getStringExtra("category");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String description = getIntent().getStringExtra("description");
        int productId = getIntent().getIntExtra("productId", -1);

        // Gán dữ liệu
        tvName.setText(name);
        tvPrice.setText(String.format("%,.0f đ", price));
        tvCategory.setText(category);
        tvDescription.setText(description != null ? Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT) : "Không có mô tả");

        // Xử lý và log imageUrl
        String fullImageUrl = null;
        if (imageUrl != null) {
            if (imageUrl.startsWith("/")) {
                fullImageUrl = BASE_URL + imageUrl;
            } else {
                fullImageUrl = imageUrl; // Nếu đã là URL đầy đủ
            }
            Log.d(TAG, "Loading image with URL: " + fullImageUrl);
        } else {
            Log.w(TAG, "imageUrl is null or empty, using placeholder");
            fullImageUrl = ""; // Sử dụng placeholder nếu không có URL
        }

        // Tải ảnh với Glide, thêm error handling
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder); // Hiển thị placeholder nếu lỗi
        Glide.with(this)
                .load(fullImageUrl)
                .apply(options)
                .into(ivThumbnail);

        btnBack.setOnClickListener(v -> finish());
        btnCartTop.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        // Nút tăng/giảm
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvDynamicQuantity.setText(String.valueOf(quantity));
            }
        });

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvDynamicQuantity.setText(String.valueOf(quantity));
        });

        // Xử lý "Thêm vào giỏ"
        btnAddToCart.setOnClickListener(v -> {
            if (layoutDynamicInfo.getVisibility() != View.VISIBLE) {
                layoutDynamicInfo.setVisibility(View.VISIBLE);
                tvDynamicName.setText(name);
                tvDynamicPrice.setText(String.format("%,.0f đ", price));
                tvDynamicQuantity.setText(String.valueOf(quantity));
            } else {
                addToCart(getUserId(), productId, quantity, false);
            }
        });

        // Xử lý "Mua ngay"
        btnBuyNow.setOnClickListener(v -> {
            if (layoutDynamicInfo.getVisibility() != View.VISIBLE) {
                layoutDynamicInfo.setVisibility(View.VISIBLE);
                tvDynamicName.setText(name);
                tvDynamicPrice.setText(String.format("%,.0f đ", price));
                tvDynamicQuantity.setText(String.valueOf(quantity));
            } else {
                ArrayList<CartItem> selectedItems = new ArrayList<>();
                String fullThumbnailUrl = (imageUrl != null && imageUrl.startsWith("/")) ? BASE_URL + imageUrl : imageUrl;
                if (fullThumbnailUrl == null || fullThumbnailUrl.isEmpty()) {
                    fullThumbnailUrl = BASE_URL + "/uploads/default.jpg"; // URL mặc định nếu không có
                    Log.w(TAG, "Using default thumbnail URL: " + fullThumbnailUrl);
                }
                Log.d(TAG, "Passing thumbnail URL to PaymentActivity: " + fullThumbnailUrl); // Debug
                CartItem item = new CartItem(-1, productId, quantity, name, price, fullThumbnailUrl);
                item.setSelected(true);
                selectedItems.add(item);
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra("selectedItems", selectedItems);
                startActivity(intent);
            }
        });
    }

    private int getUserId() {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private void addToCart(int userId, int productId, int quantity, boolean goToCart) {
        if (userId == -1) {
            showTopToast("⚠️ Vui lòng đăng nhập!", false);
            return;
        }
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("product_id", productId);
            jsonBody.put("quantity", quantity);
        } catch (JSONException e) {
            showTopToast("❌ Lỗi dữ liệu!", false);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, CART_URL, jsonBody,
                response -> {
                    showTopToast("🛒 Thêm sản phẩm thành công", true);
                    if (goToCart) {
                        startActivity(new Intent(this, CartActivity.class));
                    }
                },
                error -> showTopToast("❌ Lỗi thêm vào giỏ!", false));
        Volley.newRequestQueue(this).add(request);
    }

    // Custom Toast hiển thị ở TOP với animation
    private void showTopToast(String message, boolean success) {
        Toast toast = new Toast(this);
        View view = getLayoutInflater().inflate(R.layout.custom_toast, null);
        TextView txtMessage = view.findViewById(R.id.txt_message);

        txtMessage.setText(message);
        txtMessage.setTextColor(Color.WHITE);
        view.setBackgroundColor(success ? Color.parseColor("#4CAF50") : Color.RED);

        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 100);

        // Animation vào từ trái
        view.setTranslationX(1000f);
        view.animate().translationX(0).setDuration(400).start();

        toast.show();

        // Animation ra bên phải
        new Handler().postDelayed(() -> {
            view.animate().translationX(-1000f).setDuration(400).withEndAction(toast::cancel).start();
        }, 2000);
    }
}