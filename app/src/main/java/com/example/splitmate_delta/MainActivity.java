package com.example.splitmate_delta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private Button mBtnLogin;
    private Button mBtnRegister;
    private EditText mEtUser;
    private EditText mEtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // initialization
        mBtnLogin = findViewById(R.id.btn_login);
        mEtUser = findViewById(R.id.et_1);
        mEtPassword = findViewById(R.id.et_2);
        mBtnRegister = findViewById(R.id.btn_register);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginClick(v);
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    //log in
    private void onLoginClick(View v) {

        String username = mEtUser.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "The name and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "");
        String savedPassword = sharedPreferences.getString("password", "");

        Log.d("MainActivity", "Saved Username: " + savedUsername + ", Saved Password: " + savedPassword);
        Log.d("MainActivity", "Entered Username: " + username + ", Entered Password: " + password);

        // verification
        if (username.equals(savedUsername) && password.equals(savedPassword)) {

            Intent intent = new Intent(MainActivity.this, FunctionActivity.class);
            startActivity(intent);
        } else {

            Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
        }
    }
}