package com.example.splitmate_delta.activityfortenant;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.permissions.Permission;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentPermissionsActivity extends AppCompatActivity {

    private LinearLayout permissionsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_permissions);

        // Get the LinearLayout container
        permissionsContainer = findViewById(R.id.permissionsContainer);

        // Retrieve userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            fetchPermissions(userId);
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPermissions(int userId) {
        BackendApiService apiService = ApiClient.getApiService();

        Call<List<Permission>> call = apiService.getAuthorizedDevices(userId);
        call.enqueue(new Callback<List<Permission>>() {
            @Override
            public void onResponse(Call<List<Permission>> call, Response<List<Permission>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Dynamically generate permission views
                    addPermissionsToLayout(response.body());
                } else {
                    Toast.makeText(CurrentPermissionsActivity.this, "Failed to fetch permissions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Permission>> call, Throwable t) {
                Toast.makeText(CurrentPermissionsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPermissionsToLayout(List<Permission> permissions) {
        for (Permission permission : permissions) {
            // Dynamically create MaterialCardView
            MaterialCardView cardView = new MaterialCardView(this);
            LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardLayoutParams.setMargins(0, 0, 0, 16);
            cardView.setLayoutParams(cardLayoutParams);
            cardView.setCardElevation(8);
            cardView.setRadius(12);

            // Create main horizontal LinearLayout
            LinearLayout mainLayout = new LinearLayout(this);
            mainLayout.setOrientation(LinearLayout.HORIZONTAL);
            mainLayout.setPadding(16, 16, 16, 16);
            mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            // Create vertical LinearLayout for texts
            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f  // Weight to fill remaining space
            );
            textLayout.setLayoutParams(textLayoutParams);

            // Device name
            TextView deviceNameTextView = new TextView(this);
            deviceNameTextView.setText("Device: " + permission.getDeviceName());
            deviceNameTextView.setTextSize(18);
            deviceNameTextView.setTextColor(Color.BLACK);
            textLayout.addView(deviceNameTextView);

            // Permission status
            TextView statusTextView = new TextView(this);
            String statusText = "Permission: " + permission.getPermissionStatus();
            statusTextView.setText(statusText);
            statusTextView.setTextSize(16);
            statusTextView.setTextColor(Color.GRAY);
            textLayout.addView(statusTextView);

            // Add textLayout to mainLayout
            mainLayout.addView(textLayout);

            // Create ImageView for checkmark icon
            ImageView checkMarkImageView = new ImageView(this);
            // Set the image resource
            checkMarkImageView.setImageResource(R.drawable.check_circle_24px);
            // Optionally, set the tint color to green
            checkMarkImageView.setColorFilter(Color.GREEN);
            // Set layout parameters for ImageView
            LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            imageLayoutParams.gravity = Gravity.CENTER_VERTICAL; // Center vertically
            checkMarkImageView.setLayoutParams(imageLayoutParams);

            // Add ImageView to mainLayout
            mainLayout.addView(checkMarkImageView);

            // Add mainLayout to cardView
            cardView.addView(mainLayout);

            // Add cardView to permissionsContainer
            permissionsContainer.addView(cardView);
        }
    }
}