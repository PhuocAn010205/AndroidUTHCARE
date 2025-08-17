package com.example.uthcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView rvCart;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private static final String CART_URL = "http://10.0.2.2:3000/cart/";
    private static final String DELETE_ALL_URL = "http://10.0.2.2:3000/cart/clear";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Toolbar + back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_cart);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        rvCart = findViewById(R.id.rv_cart);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartItems);
        rvCart.setAdapter(cartAdapter);

        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Snackbar.make(rvCart, "Vui lòng đăng nhập để xem giỏ hàng", Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        loadCart(userId);

        // Handle adapter events
        cartAdapter.setOnItemActionListener(new CartAdapter.OnItemActionListener() {
            @Override
            public void onItemDelete(CartItem cartItem) {
                deleteCartItem(cartItem.getId());
            }

            @Override
            public void onQuantityChange(CartItem cartItem, int newQuantity) {
                updateCartItem(cartItem.getId(), newQuantity);
            }
        });

        // Checkout Button
        Button btnCheckout = findViewById(R.id.btn_checkout);
        btnCheckout.setOnClickListener(v -> {
            List<CartItem> selectedItems = new ArrayList<>();
            for (CartItem item : cartItems) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }
            if (selectedItems.isEmpty()) {
                Snackbar.make(rvCart, "Vui lòng chọn sản phẩm để thanh toán", Snackbar.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                intent.putExtra("selectedItems", new ArrayList<>(selectedItems));
                startActivity(intent);
            }
        });

        // Clear Cart Button
        Button btnClearCart = findViewById(R.id.btn_clear_cart);
        btnClearCart.setOnClickListener(v -> clearCart(userId));
    }

    private void loadCart(int userId) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                CART_URL + userId,
                null,
                response -> {
                    try {
                        cartItems.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            CartItem cartItem = new CartItem(
                                    obj.getInt("id"),
                                    obj.getInt("product_id"), // Thêm product_id
                                    obj.getInt("quantity"),
                                    obj.getString("product_name"),
                                    obj.getDouble("current_price"),
                                    obj.getString("thumbnail_url")
                            );
                            cartItems.add(cartItem);
                        }
                        cartAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Snackbar.make(rvCart, "Lỗi phân tích dữ liệu giỏ hàng", Snackbar.LENGTH_LONG).show();
                    }
                },
                error -> Snackbar.make(rvCart, "Không thể tải giỏ hàng", Snackbar.LENGTH_LONG).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void updateCartItem(int cartItemId, int newQuantity) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("quantity", newQuantity);
        } catch (JSONException e) {
            Snackbar.make(rvCart, "Lỗi tạo dữ liệu", Snackbar.LENGTH_SHORT).show();
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                CART_URL + cartItemId,
                jsonBody,
                response -> Snackbar.make(rvCart, "Cập nhật số lượng thành công", Snackbar.LENGTH_SHORT).show(),
                error -> Snackbar.make(rvCart, "Lỗi cập nhật số lượng", Snackbar.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void deleteCartItem(int cartItemId) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                CART_URL + cartItemId,
                null,
                response -> {
                    Snackbar.make(rvCart, "Xóa sản phẩm thành công", Snackbar.LENGTH_SHORT).show();
                    SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                    loadCart(prefs.getInt("user_id", -1));
                },
                error -> Snackbar.make(rvCart, "Lỗi xóa sản phẩm", Snackbar.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void clearCart(int userId) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
        } catch (JSONException e) {
            Snackbar.make(rvCart, "Lỗi tạo dữ liệu", Snackbar.LENGTH_SHORT).show();
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                DELETE_ALL_URL,
                jsonBody,
                response -> {
                    Snackbar.make(rvCart, "Xóa tất cả sản phẩm thành công", Snackbar.LENGTH_LONG).show();
                    loadCart(userId);
                },
                error -> Snackbar.make(rvCart, "Lỗi xóa giỏ hàng", Snackbar.LENGTH_LONG).show()
        );
        Volley.newRequestQueue(this).add(request);
    }
}