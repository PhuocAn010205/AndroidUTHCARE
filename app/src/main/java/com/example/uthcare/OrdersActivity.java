package com.example.uthcare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();
    private static final String ORDERS_URL = "http://10.0.2.2:3000/orders/";
    private static final String UPDATE_ADDRESS_URL = "http://10.0.2.2:3000/orders/";
    private static final String CANCEL_ORDER_URL = "http://10.0.2.2:3000/orders/";
    private static final String TAG = "OrdersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // Ánh xạ nút Back
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(OrdersActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        rvOrders = findViewById(R.id.rv_orders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this, orderList);
        rvOrders.setAdapter(orderAdapter);

        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId != -1) {
            loadOrders(userId);
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }

        orderAdapter.setOnOrderActionListener(new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onEditAddress(Order order) {
                showEditAddressDialog(order);
            }

            @Override
            public void onCancelOrder(Order order) {
                showCancelConfirmation(order);
            }
        });
    }

    private void loadOrders(int userId) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ORDERS_URL + userId,
                null,
                response -> {
                    try {
                        orderList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Order order = new Order(
                                    obj.getInt("order_id"),
                                    obj.getDouble("total_amount"),
                                    obj.getString("shipping_name"),
                                    obj.getString("shipping_phone"),
                                    obj.getString("shipping_address"),
                                    obj.getString("payment_method"),
                                    obj.getString("status"),
                                    obj.getString("created_at")
                            );
                            JSONArray itemsArray = obj.getJSONArray("items");
                            List<OrderItem> items = new ArrayList<>();
                            for (int j = 0; j < itemsArray.length(); j++) {
                                JSONObject itemObj = itemsArray.getJSONObject(j);
                                OrderItem item = new OrderItem(
                                        itemObj.getInt("product_id"),
                                        itemObj.getString("product_name"),
                                        itemObj.getInt("quantity"),
                                        itemObj.getDouble("unit_price"),
                                        itemObj.getString("thumbnail_url")
                                );
                                items.add(item);
                            }
                            order.setItems(items);
                            orderList.add(order);
                        }
                        orderAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing orders", e);
                        Toast.makeText(this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading orders", error);
                    Toast.makeText(this, "Không thể tải đơn hàng. Vui lòng kiểm tra kết nối server.", Toast.LENGTH_SHORT).show();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void showEditAddressDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_address, null);
        EditText etNewAddress = dialogView.findViewById(R.id.et_new_address);
        etNewAddress.setText(order.getShippingAddress());
        builder.setView(dialogView)
                .setTitle("Chỉnh sửa địa chỉ")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newAddress = etNewAddress.getText().toString().trim();
                    if (!newAddress.isEmpty()) {
                        updateOrderAddress(order.getOrderId(), newAddress);
                    } else {
                        Toast.makeText(this, "Địa chỉ không được để trống", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateOrderAddress(int orderId, String newAddress) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("shipping_address", newAddress);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            Toast.makeText(this, "Lỗi định dạng dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                UPDATE_ADDRESS_URL + orderId + "/update-address",
                jsonBody,
                response -> {
                    try {
                        String message = response.getString("message");
                        if ("Cập nhật địa chỉ thành công".equals(message)) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            loadOrders(getSharedPreferences("user_data", MODE_PRIVATE).getInt("user_id", -1));
                        } else {
                            Toast.makeText(this, "Phản hồi không mong muốn từ server", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                        Toast.makeText(this, "Lỗi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error updating address", error);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        Toast.makeText(this, "Chức năng chỉnh sửa địa chỉ chưa được hỗ trợ. Vui lòng liên hệ quản trị viên để thêm route.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Lỗi kết nối server. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                    }
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void showCancelConfirmation(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc muốn hủy đơn hàng " + order.getOrderId() + "?")
                .setPositiveButton("Hủy", (dialog, which) -> cancelOrder(order.getOrderId()))
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void cancelOrder(int orderId) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                CANCEL_ORDER_URL + orderId + "/cancel",
                null,
                response -> {
                    try {
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        loadOrders(getSharedPreferences("user_data", MODE_PRIVATE).getInt("user_id", -1));
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                        Toast.makeText(this, "Lỗi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error canceling order", error);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        Toast.makeText(this, "Chức năng hủy đơn chưa được hỗ trợ. Vui lòng liên hệ quản trị viên.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Lỗi kết nối server. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                    }
                }
        );
        Volley.newRequestQueue(this).add(request);
    }
}