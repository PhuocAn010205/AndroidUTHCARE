package com.example.uthcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
    private TextView tvUsername;
    private RecyclerView rvCategories, rvProducts;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> productList = new ArrayList<>();
    private static final String PRODUCT_URL = "http://10.0.2.2:3000/products";

    // Banner
    private ViewPager2 bannerViewPager;
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private int currentBanner = 0;
    private List<Integer> banners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ánh xạ View
        etSearch = findViewById(R.id.et_search);
        btnCart = findViewById(R.id.btn_cart);
        tvUsername = findViewById(R.id.tv_username);
        rvCategories = findViewById(R.id.rv_categories);
        rvProducts = findViewById(R.id.rv_products);
        bannerViewPager = findViewById(R.id.bannerViewPager);

        // Hiển thị tên người dùng
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String username = prefs.getString("username", "Người dùng");
        tvUsername.setText("Xin chào, " + username);

        // Setup RecyclerView
        setupCategoryRecycler();
        setupProductRecycler();

        // Setup Banner
        setupBannerSlider();

        // Load sản phẩm
        loadAllProducts();

        // Sự kiện nút giỏ hàng
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Tìm kiếm
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
        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Trang Chủ", R.drawable.ic_home));
        categories.add(new Category("Thuốc", R.drawable.ic_medicine));
        categories.add(new Category("Thực phẩm bảo vệ sức khỏe", R.drawable.ic_menu_info_details));
        categories.add(new Category("Chăm sóc cá nhân", R.drawable.ic_personal_care));
        categories.add(new Category("Chăm sóc sắc đẹp", R.drawable.ic_beauty));

        categoryAdapter = new CategoryAdapter(this, categories);
        rvCategories.setAdapter(categoryAdapter);
        categoryAdapter.setOnCategoryClickListener(category -> {
            if (category.equals("Trang Chủ")) {
                productAdapter.updateList(productList);
            } else {
                filterProductsByCategory(category);
            }
        });
    }

    private void setupProductRecycler() {
        productAdapter = new ProductAdapter(this, productList);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(productAdapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));
    }

    private void setupBannerSlider() {
        // danh sách banner thật
        banners = new ArrayList<>();
        banners.add(R.drawable.banner1);
        banners.add(R.drawable.banner2);
        banners.add(R.drawable.banner3);
        banners.add(R.drawable.banner4);
        banners.add(R.drawable.banner5);

        if (banners.isEmpty()) return;

        BannerAdapter bannerAdapter = new BannerAdapter(banners);
        bannerViewPager.setAdapter(bannerAdapter);

        // Carousel look: show edges
        bannerViewPager.setOffscreenPageLimit(3);
        bannerViewPager.setClipToPadding(false);
        bannerViewPager.setClipChildren(false);

        // add margin + transformer (Composite)
        androidx.viewpager2.widget.ViewPager2.PageTransformer transformer =
                (page, position) -> {
                    float r = 1 - Math.abs(position);
                    float scale = 0.85f + r * 0.15f;
                    page.setScaleY(scale);
                    page.setAlpha(0.5f + r * 0.5f);
                };
        // dùng CompositePageTransformer để thêm margin nếu muốn
        androidx.viewpager2.widget.CompositePageTransformer composite = new androidx.viewpager2.widget.CompositePageTransformer();

        // Instead use MarginPageTransformer:
        composite.addTransformer(new androidx.viewpager2.widget.MarginPageTransformer(30));
        composite.addTransformer(transformer);
        bannerViewPager.setPageTransformer(composite);

        // Set start position tại "giữa" sao cho modulo đúng
        int startPos = Integer.MAX_VALUE / 2;
        startPos = startPos - (startPos % banners.size());
        bannerViewPager.setCurrentItem(startPos, false);

        // Auto-slide: sử dụng currentItem + 1 (không cần wrap thủ công)
        bannerRunnable = () -> {
            if (bannerViewPager.getAdapter() == null) return;
            int next = bannerViewPager.getCurrentItem() + 1;
            bannerViewPager.setCurrentItem(next, true);
            // post lại
            bannerHandler.postDelayed(bannerRunnable, 3000);
        };

        // start auto
        bannerHandler.removeCallbacks(bannerRunnable);
        bannerHandler.postDelayed(bannerRunnable, 3000);

        // dừng auto khi user drag, resume khi idle
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    bannerHandler.removeCallbacks(bannerRunnable);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    bannerHandler.removeCallbacks(bannerRunnable);
                    bannerHandler.postDelayed(bannerRunnable, 3000);
                }
            }
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
                                    obj.getString("category"),
                                    obj.optString("description", "Không có mô tả")
                            );
                            productList.add(product);
                        }
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
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON", e);
                        Toast.makeText(this, "Lỗi dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading products", error);
                    Toast.makeText(this, "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onPause() {
        super.onPause();
        bannerHandler.removeCallbacks(bannerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bannerHandler.removeCallbacks(bannerRunnable);
    }
}
