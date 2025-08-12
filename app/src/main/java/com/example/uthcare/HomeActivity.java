package com.example.uthcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private EditText etSearch;
    private ImageButton btnCart;
    private TextView tvUsername, tvCategorySelector;
    private ImageView ivBanner;
    private RecyclerView rvCategories, rvProducts;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> productList = new ArrayList<>();
    private static final String PRODUCT_URL = "http://192.168.1.3:3000/products";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ánh xạ view
        etSearch = findViewById(R.id.et_search);
        btnCart = findViewById(R.id.btn_cart);
        tvUsername = findViewById(R.id.tv_username);
//        tvCategorySelector = findViewById(R.id.tvCategorySelector);
        ivBanner = findViewById(R.id.iv_banner);
        rvCategories = findViewById(R.id.rv_categories);
        rvProducts = findViewById(R.id.rv_products);

//        // Kiểm tra null để tránh crash
//        if (etSearch == null || btnCart == null || tvUsername == null || tvCategorySelector == null ||
//                ivBanner == null || rvCategories == null || rvProducts == null) {
//            Log.e(TAG, "One or more views are null");
//            Toast.makeText(this, "Lỗi khởi tạo giao diện", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }

        // Hiển thị tên người dùng từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String username = prefs.getString("username", "Người dùng");
        tvUsername.setText("Xin chào, " + username);

        // Thiết lập danh mục
        setupCategoryRecycler();

        // Khởi tạo productAdapter trước
        productAdapter = new ProductAdapter(this, productList);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(productAdapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        // Tải toàn bộ sản phẩm
        loadAllProducts();

        // Bấm giỏ hàng
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Tìm kiếm sản phẩm theo tên
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim().toLowerCase();
                filterProductsBySearch(query);
                return true;
            }
            return false;
        });
    }

    private void setupCategoryRecycler() {

        if (rvCategories != null) {
            rvCategories.setLayoutManager(new GridLayoutManager(this, 3));
            List<Category> categories = new ArrayList<>();
            categories.add(new Category("Trang Chủ",R.drawable.ic_home));
            categories.add(new Category("Thuốc", R.drawable.ic_medicine));
            categories.add(new Category("Thực phẩm bảo vệ sức khỏe", R.drawable.ic_menu_info_details));
            categories.add(new Category("Chăm sóc cá nhân", R.drawable.ic_personal_care));
            categories.add(new Category("Chăm sóc sắc đẹp", R.drawable.ic_beauty));
            categoryAdapter = new CategoryAdapter(this, categories);
            rvCategories.setAdapter(categoryAdapter);
            categoryAdapter.setOnCategoryClickListener(category -> {
                if (category.equals("Trang Chủ")) {
                    if (productAdapter != null) productAdapter.updateList(productList);
                } else {
                    filterProductsByCategory(category);
                }
            });
        }
    }

    private void loadAllProducts() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, PRODUCT_URL, null,
                response -> {
                    try {
                        productList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Log.d(TAG, "Raw JSON: " + obj.toString());
                            String description = obj.optString("description", "Không có mô tả");
                            Log.d(TAG, "Extracted description: " + description);
                            Product product = new Product(
                                    obj.getInt("product_id"),
                                    obj.getString("product_name"),
                                    obj.getDouble("current_price"),
                                    obj.optString("thumbnail_url"),
                                    obj.getString("category"),
                                    description
                            );
                            productList.add(product);
                        }
                        if (productAdapter != null) {
                            productAdapter.updateList(productList);
                            productAdapter.setOnItemClickListener(product -> {
                                Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
                                intent.putExtra("name", product.getProductName());
                                intent.putExtra("price", product.getPrice());
                                intent.putExtra("category", product.getCategory());
                                intent.putExtra("imageUrl", product.getThumbnailUrl());
                                intent.putExtra("productId", product.getProductId());
                                intent.putExtra("description", product.getDescription());
                                startActivity(intent);
                                Log.d(TAG, "Description sent: " + product.getDescription());
                            });
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON", e);
                        Toast.makeText(this, "Lỗi phân tích dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading products", error);
                    Toast.makeText(this, "Không thể tải sản phẩm: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void filterProductsByCategory(String category) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : productList) {
            if (p.getCategory().equalsIgnoreCase(category)) {
                filtered.add(p);
            }
        }
        if (productAdapter != null) {
            productAdapter.updateList(filtered);
        }
    }

    private void filterProductsBySearch(String query) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : productList) {
            if (p.getProductName().toLowerCase().contains(query)) {
                filtered.add(p);
            }
        }
        if (productAdapter != null) {
            productAdapter.updateList(filtered);
        }
    }
}