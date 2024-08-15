package com.example.splitmate_delta.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;

public class LoginActivity extends AppCompatActivity {

    private Button mBtnLogin;
    private EditText mEtUser;
    private EditText mEtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initializes the UI control
        mBtnLogin = findViewById(R.id.btn_login);
        mEtUser = findViewById(R.id.et_1);
        mEtPassword = findViewById(R.id.et_2);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginClick(v);
            }
        });
    }

    // Simple login logic
    private void onLoginClick(View v) {
        String username = mEtUser.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "The username and password cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            String userRole = getUserRole(username);
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            intent.putExtra("user_role", userRole);
            startActivity(intent);
            finish();
        }
    }

    // Mock function to determine user role
    private String getUserRole(String username) {
        if (username.equals("landlord")) {
            return "landlord";
        } else {
            return "tenant";
        }
    }
}