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
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // Inverted Index cho tìm kiếm
    private Map<String, Set<Integer>> invertedIndex = new HashMap<>();
    private static final Set<String> STOP_WORDS = new HashSet<>(List.of("là", "và", "cái", "ở", "với"));

    // Banner
    private ViewPager2 bannerViewPager;
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private int currentBanner = 0;
    private List<Integer> banners;

    // Bottom buttons
    private ImageButton btnHome, btnViewOrders, btnLogout;
    private NestedScrollView nestedScrollView;

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
        nestedScrollView = findViewById(R.id.nested_scroll_view);

        // Bottom buttons
        btnHome = findViewById(R.id.btn_home);
        btnViewOrders = findViewById(R.id.btn_view_orders);
        btnLogout = findViewById(R.id.btn_logout);

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

        // Tìm kiếm nâng cao
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim().toLowerCase();
                if (!query.isEmpty()) {
                    filterProductsByFullTextSearch(query);
                } else {
                    productAdapter.updateList(productList); // Hiển thị tất cả nếu không nhập
                }
                return true;
            }
            return false;
        });

        // Sự kiện bottom buttons
        btnHome.setOnClickListener(v -> {
            if (nestedScrollView != null) {
                nestedScrollView.smoothScrollTo(0, 0);
            }
            Toast.makeText(this, "Đã load lại đầu trang", Toast.LENGTH_SHORT).show();
        });

        btnViewOrders.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, OrdersActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefsLogout = getSharedPreferences("user_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefsLogout.edit();
            editor.clear();
            editor.apply();
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupCategoryRecycler() {
        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Tất cả", R.drawable.ic_list));
        categories.add(new Category("Thuốc", R.drawable.ic_medicine));
        categories.add(new Category("Thực phẩm bảo vệ sức khỏe", R.drawable.ic_menu_info_details));
        categories.add(new Category("Chăm sóc cá nhân", R.drawable.ic_personal_care));
        categories.add(new Category("Chăm sóc sắc đẹp", R.drawable.ic_beauty));

        categoryAdapter = new CategoryAdapter(this, categories);
        rvCategories.setAdapter(categoryAdapter);
        categoryAdapter.setOnCategoryClickListener(category -> {
            if (category.equals("Tất cả")) {
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
        banners = new ArrayList<>();
        banners.add(R.drawable.banner1);
        banners.add(R.drawable.banner2);
        banners.add(R.drawable.banner3);
        banners.add(R.drawable.banner4);
        banners.add(R.drawable.banner5);

        if (banners.isEmpty()) return;

        BannerAdapter bannerAdapter = new BannerAdapter(banners);
        bannerViewPager.setAdapter(bannerAdapter);

        bannerViewPager.setOffscreenPageLimit(3);
        bannerViewPager.setClipToPadding(false);
        bannerViewPager.setClipChildren(false);

        androidx.viewpager2.widget.ViewPager2.PageTransformer transformer =
                (page, position) -> {
                    float r = 1 - Math.abs(position);
                    float scale = 0.85f + r * 0.15f;
                    page.setScaleY(scale);
                    page.setAlpha(0.5f + r * 0.5f);
                };
        androidx.viewpager2.widget.CompositePageTransformer composite = new androidx.viewpager2.widget.CompositePageTransformer();
        composite.addTransformer(new androidx.viewpager2.widget.MarginPageTransformer(30));
        composite.addTransformer(transformer);
        bannerViewPager.setPageTransformer(composite);

        int startPos = Integer.MAX_VALUE / 2;
        startPos = startPos - (startPos % banners.size());
        bannerViewPager.setCurrentItem(startPos, false);

        bannerRunnable = () -> {
            if (bannerViewPager.getAdapter() == null) return;
            int next = bannerViewPager.getCurrentItem() + 1;
            bannerViewPager.setCurrentItem(next, true);
            bannerHandler.postDelayed(bannerRunnable, 3000);
        };

        bannerHandler.removeCallbacks(bannerRunnable);
        bannerHandler.postDelayed(bannerRunnable, 3000);

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
                            buildInvertedIndex(product); // Xây dựng Inverted Index
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

    private void buildInvertedIndex(Product product) {
        int productId = product.getProductId();
        String[] tokens = tokenize(product.getProductName());
        for (String token : tokens) {
            if (!STOP_WORDS.contains(token)) {
                invertedIndex.computeIfAbsent(token, k -> new HashSet<>()).add(productId);
            }
        }
    }

    private String[] tokenize(String text) {
        return text.toLowerCase().split("\\s+"); // Cắt từ bằng khoảng trắng
    }

    private void filterProductsByFullTextSearch(String query) {
        String[] queryTokens = tokenize(query);
        Set<Integer> relevantProductIds = new HashSet<>();
        for (String token : queryTokens) {
            if (invertedIndex.containsKey(token)) {
                if (relevantProductIds.isEmpty()) {
                    relevantProductIds.addAll(invertedIndex.get(token));
                } else {
                    relevantProductIds.retainAll(invertedIndex.get(token)); // Giao nhau
                }
            }
        }

        List<Product> filteredProducts = new ArrayList<>();
        for (Product product : productList) {
            if (relevantProductIds.contains(product.getProductId())) {
                filteredProducts.add(product);
            }
        }
        productAdapter.updateList(filteredProducts); // Hiển thị sản phẩm có tên giống
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