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

import java.io.IOException;

import okhttp3.ResponseBody;
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
        Call<ResponseBody> call = apiService.assignDeviceToHouse(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Toast.makeText(PiManagementActivity.this, "Device assigned successfully: " + responseBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(PiManagementActivity.this, "Failed to read response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(PiManagementActivity.this, "Failed to assign device: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(PiManagementActivity.this, "Failed to read error response", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(PiManagementActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}