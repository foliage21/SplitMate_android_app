package com.example.splitmate_delta.activityforlandlord;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.models.pi.AssignDeviceRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PiManagementActivity extends AppCompatActivity {

    private EditText uidEditText;
    private EditText houseIdEditText;
    private Button assignDeviceButton;
    private BackendApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pi_management);

        uidEditText = findViewById(R.id.uidEditText);
        houseIdEditText = findViewById(R.id.houseIdEditText);
        assignDeviceButton = findViewById(R.id.assignDeviceButton);

        apiService = ApiClient.getClient().create(BackendApiService.class);

        assignDeviceButton.setOnClickListener(view -> {
            String uid = uidEditText.getText().toString().trim();
            String houseIdStr = houseIdEditText.getText().toString().trim();

            if (uid.isEmpty() || houseIdStr.isEmpty()) {
                Toast.makeText(PiManagementActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int houseId = Integer.parseInt(houseIdStr);

                AssignDeviceRequest request = new AssignDeviceRequest(uid, houseId);

                assignDeviceToHouse(request);
            } catch (NumberFormatException e) {
                Toast.makeText(PiManagementActivity.this, "The house ID needs to be a number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // API
    private void assignDeviceToHouse(AssignDeviceRequest request) {
        Call<Void> call = apiService.assignDeviceToHouse(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Request successful
                    Toast.makeText(PiManagementActivity.this, "Device assigned successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Request failed, show status code
                    int statusCode = response.code();
                    Toast.makeText(PiManagementActivity.this, "Failed to assign device: " + statusCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Network error or other issues
                Toast.makeText(PiManagementActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}