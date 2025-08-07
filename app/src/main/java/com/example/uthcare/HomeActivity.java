package com.example.uthcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

public class HomeActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageButton btnCart;
    private TextView tvUsername;
    private RecyclerView rvCategories, rvProducts;

    private ProductAdapter productAdapter;
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
        rvProducts = findViewById(R.id.rv_products);

        // Hiển thị tên người dùng từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String username = prefs.getString("username", "Người dùng");
        tvUsername.setText("Xin chào, " + username);

        // Thiết lập danh mục
        setupCategoryRecycler();

        // Tải toàn bộ sản phẩm
        loadAllProducts();
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing); // ví dụ 8dp
        rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));


        // Bấm giỏ hàng (sau này mở CartActivity nếu có)
        btnCart.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng giỏ hàng chưa được cài đặt", Toast.LENGTH_SHORT).show();
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
        TextView tvCategorySelector = findViewById(R.id.tvCategorySelector);

        List<String> categories = Arrays.asList(
                "Trang Chủ",
                "Thuốc",
                "Thực phẩm bảo vệ sức khỏe",
                "Chăm sóc cá nhân",
                "Chăm sóc sắc đẹp"
        );


        tvCategorySelector.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            for (int i = 0; i < categories.size(); i++) {
                popupMenu.getMenu().add(0, i, i, categories.get(i));
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                String selectedCategory = categories.get(item.getItemId());
                tvCategorySelector.setText(selectedCategory); // hoặc hiển thị lại tên
                filterProductsByCategory(selectedCategory);   // lọc sản phẩm
                return true;
            });

            popupMenu.show();
        });

    }

    private void loadAllProducts() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, PRODUCT_URL, null,
                response -> {
                    try {
                        productList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Product product = new Product(
                                    obj.getInt("product_id"),
                                    obj.getString("product_name"),
                                    obj.getDouble("current_price"),
                                    obj.optString("thumbnail_url"),
                                    obj.getString("category")
                            );
                            productList.add(product);
                        }
                        productAdapter = new ProductAdapter(HomeActivity.this, productList);
                        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
                        rvProducts.setAdapter(productAdapter);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show()
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
        productAdapter.updateList(filtered);
    }

    private void filterProductsBySearch(String query) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : productList) {
            if (p.getProductName().toLowerCase().contains(query)) {
                filtered.add(p);
            }
        }
        productAdapter.updateList(filtered);
    }
}
