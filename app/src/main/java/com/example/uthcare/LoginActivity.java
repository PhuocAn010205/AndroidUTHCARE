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

    String LOGIN_URL = "http://192.168.1.4:3000/login"; // Đổi theo IP server của bạn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                            // 🔸 Lấy username từ response (nếu backend trả về)
                            String username = response.optString("username", "Người dùng");

                            // 🔸 Lưu vào SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("username", username);
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
