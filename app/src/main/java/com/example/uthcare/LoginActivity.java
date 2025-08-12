package com.example.uthcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.View;
import android.graphics.Color;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvErrorMessage, tvRegisterLink;
    private Button btnLogin;
    private static final String TIME_LOGO = "last_login_time";
    private static final long TIME_LOGOUT = 2 * 60 * 60 * 1000; // 2 giờ
    private static final String LOGIN_URL = "http://192.168.1.3:3000/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin();
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterLink = findViewById(R.id.tv_register_link);

        tvErrorMessage.setVisibility(View.GONE);

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                tvErrorMessage.setText("Vui lòng điền đầy đủ thông tin");
                tvErrorMessage.setVisibility(View.VISIBLE);
                return;
            }

            loginUser(email, password);
        });
    }

    private void checkLogin() {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        long lastLoginTime = prefs.getLong(TIME_LOGO, 0);

        if (lastLoginTime != 0 && (System.currentTimeMillis() - lastLoginTime) < TIME_LOGOUT) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("username");
            editor.remove("user_id"); // Xóa user_id
            editor.remove(TIME_LOGO);
            editor.apply();

            if (lastLoginTime != 0) {
                Toast.makeText(this, "Phiên đăng nhập đã hết hạn.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginUser(String email, String password) {
        tvErrorMessage.setVisibility(View.GONE);

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    LOGIN_URL,
                    jsonBody,
                    response -> {
                        try {
                            String msg = response.getString("message");
                            String username = response.optString("username", "Người dùng");
                            int userId = response.getInt("user_id"); // Lấy user_id từ response

                            // Lưu vào SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putLong(TIME_LOGO, System.currentTimeMillis());
                            editor.putString("username", username);
                            editor.putInt("user_id", userId); // Lưu user_id
                            editor.apply();

                            showMessage(msg, Color.parseColor("#4CAF50"));

                            new android.os.Handler().postDelayed(() -> {
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }, 1500);

                        } catch (JSONException e) {
                            showMessage("Phản hồi không hợp lệ!", Color.RED);
                        }
                    },
                    error -> {
                        String msg = "Lỗi đăng nhập!";
                        if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                            msg = "Email hoặc mật khẩu sai!";
                        }
                        showMessage(msg, Color.RED);
                    }
            );

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);

        } catch (JSONException e) {
            showMessage("Lỗi định dạng dữ liệu!", Color.RED);
        }
    }

    private void showMessage(String text, int color) {
        tvErrorMessage.setText(text);
        tvErrorMessage.setTextColor(color);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }
}