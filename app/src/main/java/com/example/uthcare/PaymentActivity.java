package com.example.uthcare;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {
    private RecyclerView rvPaymentItems;
    private PaymentAdapter paymentAdapter;
    private List<CartItem> paymentItems = new ArrayList<>();
    private TextView tvTotalPrice;
    private EditText etName, etPhone, etAddress;
    private RadioGroup rgPaymentMethod;
    private Button btnConfirmPayment;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private static final String ORDER_URL = "http://10.0.2.2:3000/orders"; // Thay bằng 192.168.2.11 nếu cần
    private static final String TAG = "PaymentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_payment);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Views
        rvPaymentItems = findViewById(R.id.rv_payment_items);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);

        // Get data from Intent
        paymentItems = (List<CartItem>) getIntent().getSerializableExtra("selectedItems");
        if (paymentItems == null) {
            paymentItems = new ArrayList<>();
            Log.w(TAG, "No items received from Intent");
        } else {
            Log.d(TAG, "Received items: " + paymentItems.size());
        }

        // Setup RecyclerView
        paymentAdapter = new PaymentAdapter(this, paymentItems);
        rvPaymentItems.setAdapter(paymentAdapter);

        // Calculate and display total price
        updateTotalPrice();

        // Confirm Payment Button
        btnConfirmPayment.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Snackbar.make(btnConfirmPayment, "Vui lòng điền đầy đủ thông tin giao hàng", Snackbar.LENGTH_LONG).show();
                return;
            }

            // Prepare order data
            String paymentMethod = getSelectedPaymentMethod();
            SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Snackbar.make(btnConfirmPayment, "Vui lòng đăng nhập để đặt hàng", Snackbar.LENGTH_LONG).show();
                return;
            }

            try {
                JSONObject orderData = new JSONObject();
                orderData.put("user_id", userId);
                orderData.put("shipping_name", name);
                orderData.put("shipping_phone", phone);
                orderData.put("shipping_address", address);
                orderData.put("payment_method", paymentMethod);

                JSONArray items = new JSONArray();
                for (CartItem item : paymentItems) {
                    JSONObject itemData = new JSONObject();
                    itemData.put("id", item.getId());
                    itemData.put("product_id", item.getProductId());
                    itemData.put("quantity", item.getQuantity());
                    itemData.put("price", item.getPrice());
                    items.put(itemData);
                    Log.d(TAG, "Item added: id=" + item.getId() + ", product_id=" + item.getProductId() + ", quantity=" + item.getQuantity() + ", price=" + item.getPrice());
                }
                orderData.put("items", items);
                Log.d(TAG, "Sending order data: " + orderData.toString());

                // Send order request
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ORDER_URL,
                        orderData,
                        response -> {
                            Log.d(TAG, "Order response: " + response.toString());
                            String orderId = response.optString("order_id");
                            // Hiển thị dialog tùy chỉnh
                            CustomDialogFragment dialog = CustomDialogFragment.newInstance(orderId);
                            dialog.show(getSupportFragmentManager(), "SuccessDialog");
                        },
                        error -> {
                            Log.e(TAG, "Order error: " + (error.getMessage() != null ? error.getMessage() : "No error message"), error);
                            try {
                                String errorMsg = new JSONObject(new String(error.networkResponse.data)).optString("message", "Không thể kết nối đến server");
                                Snackbar.make(btnConfirmPayment, "Lỗi đặt hàng: " + errorMsg, Snackbar.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Snackbar.make(btnConfirmPayment, "Lỗi đặt hàng: Không thể kết nối đến server", Snackbar.LENGTH_LONG).show();
                            }
                        }
                );
                Volley.newRequestQueue(this).add(request);
            } catch (Exception e) {
                Log.e(TAG, "Error creating order data", e);
                Snackbar.make(btnConfirmPayment, "Lỗi tạo dữ liệu đơn hàng: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : paymentItems) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotalPrice.setText(decimalFormat.format(total) + " đ");
    }

    private String getSelectedPaymentMethod() {
        int checkedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_cash) {
            return "Thanh toán khi nhận hàng";
        } else if (checkedId == R.id.rb_bank) {
            return "Thanh toán qua thẻ ngân hàng";
        } else if (checkedId == R.id.rb_momo) {
            return "Thanh toán qua MoMo";
        }
        return "Không xác định";
    }
}