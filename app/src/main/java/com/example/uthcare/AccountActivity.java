package com.example.uthcare;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        TextView tvWelcome = findViewById(R.id.tv_welcome);
        tvWelcome.setText("Chào mừng đến trang tài khoản!");
        // Thêm logic hiển thị thông tin người dùng từ SharedPreferences nếu cần
    }
}