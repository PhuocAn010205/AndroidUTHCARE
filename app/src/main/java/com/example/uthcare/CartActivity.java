package com.example.uthcare;

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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView rvCart;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private static final String CART_URL = "http://192.168.1.3:3000/cart/";
    private static final String DELETE_ALL_URL = "http://192.168.1.3:3000/cart/clear"; // Giả sử có API xóa tất cả

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rv_cart);
        rvCart.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(this, cartItems);
        rvCart.setAdapter(cartAdapter);

        // Lấy user_id từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải giỏ hàng
        loadCart(userId);

        // Xử lý các sự kiện từ adapter
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

        // Xử lý nút Thanh toán
        Button btnCheckout = findViewById(R.id.btn_checkout);
        btnCheckout.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng thanh toán chưa được cài đặt", Toast.LENGTH_SHORT).show();
            // Thêm logic thanh toán ở đây (ví dụ: gửi đơn hàng đến server)
        });

        // Xử lý nút Xóa tất cả
        Button btnClearCart = findViewById(R.id.btn_clear_cart);
        btnClearCart.setOnClickListener(v -> {
            clearCart(userId);
        });
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
                                    obj.getInt("quantity"),
                                    obj.getString("product_name"),
                                    obj.getDouble("current_price"),
                                    obj.getString("thumbnail_url")
                            );
                            cartItems.add(cartItem);
                        }
                        cartAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Lỗi phân tích dữ liệu giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void updateCartItem(int cartItemId, int newQuantity) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("quantity", newQuantity);
        } catch (JSONException e) {
            Toast.makeText(this, "Lỗi tạo dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                CART_URL + cartItemId, // Giả sử có API PUT để cập nhật số lượng
                jsonBody,
                response -> {
                    Toast.makeText(this, "Cập nhật số lượng thành công", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void deleteCartItem(int cartItemId) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                CART_URL + cartItemId,
                null,
                response -> {
                    Toast.makeText(this, response.optString("message", "Xóa sản phẩm thành công"), Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                    loadCart(prefs.getInt("user_id", -1));
                },
                error -> Toast.makeText(this, "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void clearCart(int userId) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
        } catch (JSONException e) {
            Toast.makeText(this, "Lỗi tạo dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                DELETE_ALL_URL,
                jsonBody,
                response -> {
                    Toast.makeText(this, "Xóa tất cả sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    loadCart(userId); // Tải lại giỏ hàng
                },
                error -> Toast.makeText(this, "Lỗi xóa giỏ hàng", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
}