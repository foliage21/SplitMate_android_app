package com.example.splitmate_delta.activityfortenant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.permissions.PermissionRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestAccessPermissionsActivity extends AppCompatActivity {

    private Button btnRequestTV, btnRequestMicrowave;
    private BackendApiService apiService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_access_permissions);

        // Initialize UI components
        btnRequestTV = findViewById(R.id.request_tv);
        btnRequestMicrowave = findViewById(R.id.request_microwave);

        // Initialize API service
        apiService = ApiClient.getApiService();

        // Retrieve userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set click listeners for each button
        btnRequestTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String macAddress = "F2:41:15:61:63:3E"; // MAC address for the TV
                requestPermission(userId, macAddress);
            }
        });

        btnRequestMicrowave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String macAddress = "C3:8A:52:E5:40:C3"; // MAC address for the Microwave
                requestPermission(userId, macAddress);
            }
        });
    }

    private void requestPermission(int userId, String macAddress) {
        PermissionRequest permissionRequest = new PermissionRequest(userId, macAddress);

        apiService.requestPermission(permissionRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(RequestAccessPermissionsActivity.this, "Permission request sent successfully.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RequestAccessPermissionsActivity.this, "Failed to send permission request.", Toast.LENGTH_LONG).show();
                    try {
                        if (response.errorBody() != null) {
                            String error = response.errorBody().string();
                            System.out.println("Error: " + error);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RequestAccessPermissionsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}