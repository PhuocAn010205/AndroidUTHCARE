package com.example.uthcare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import android.view.View;
import android.graphics.Color;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;
    TextView tvLoginLink, tvErrorMessage;

    String serverUrl = "http://10.0.2.2:3000/register";
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);
        tvErrorMessage = findViewById(R.id.tv_error_message);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showMessage("Vui lòng điền đầy đủ thông tin", Color.RED);
            return;
        }

        if (password.length() < 8) {
            showMessage("Mật khẩu phải có ít nhất 8 ký tự", Color.RED);
            return;
        }

        if (!password.equals(confirm)) {
            showMessage("Mật khẩu xác nhận không khớp", Color.RED);
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", name);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            Toast.makeText(this, "Lỗi tạo JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Sending request: " + jsonBody);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                serverUrl,
                jsonBody,
                response -> {
                    Log.d(TAG, "Response: " + response.toString());
                    String msg = response.optString("message", "Đăng ký thành công!");
                    showMessage(msg, Color.parseColor("#4CAF50"));
                    navigateToLoginAfterDelay();
                },
                error -> {
                    String errorMsg = "Lỗi kết nối máy chủ!";
                    if (error instanceof ServerError && error.networkResponse != null) {
                        try {
                            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            JSONObject obj = new JSONObject(body);
                            errorMsg = obj.optString("message", errorMsg);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response", e);
                        }
                    } else {
                        Log.e(TAG, "Volley error: ", error);
                    }
                    showMessage(errorMsg, Color.RED);
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void showMessage(String message, int color) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setTextColor(color);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void navigateToLoginAfterDelay() {
        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }
}
