package com.example.splitmate_delta.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.login.LoginRequest;
import com.example.splitmate_delta.models.login.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private Button mBtnLogin;
    private EditText mEtUser;
    private EditText mEtPassword;
    private BackendApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mBtnLogin = findViewById(R.id.btn_login);
        mEtUser = findViewById(R.id.et_1);
        mEtPassword = findViewById(R.id.et_2);

        apiService = ApiClient.getApiService();

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginClick(v);
            }
        });
    }

    private void onLoginClick(View v) {
        String email = mEtUser.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "The username and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        loginUser(email, password);
    }

    private void loginUser(String email, String password) {

        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    LoginResponse loginResponse = response.body();
                    String role = loginResponse.getRole();

                    int userId = Integer.parseInt(loginResponse.getUserId());

                    saveUserIdToPreferences(userId);

                    proceedToNavigationActivity(role);
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save the user ID to SharedPreferences
    private void saveUserIdToPreferences(int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("userId", userId);
        editor.apply();
    }

    private void proceedToNavigationActivity(String userRole) {
        Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
        intent.putExtra("user_role", userRole);
        startActivity(intent);
        finish();
    }
}