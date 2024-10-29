package com.example.splitmate_delta.activityforlandlord;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.utils.DeviceAdapter;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.models.pi.AssignDeviceRequest;
import com.example.splitmate_delta.models.pi.Device;
import com.example.splitmate_delta.models.pi.UnregisterDeviceRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PiManagementActivity extends AppCompatActivity {

    private Button assignDeviceButton;
    private Button unregisterDeviceButton;
    private Button viewDeviceListButton;
    private BackendApiService apiService;

    private RecyclerView deviceRecyclerView;
    private DeviceAdapter deviceAdapter;
    private TextView deviceListTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pi_management);

        assignDeviceButton = findViewById(R.id.assignDeviceButton);
        unregisterDeviceButton = findViewById(R.id.unregisterDeviceButton);
        viewDeviceListButton = findViewById(R.id.viewDeviceListButton);

        deviceListTitle = findViewById(R.id.deviceListTitle);
        deviceRecyclerView = findViewById(R.id.deviceRecyclerView);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceAdapter = new DeviceAdapter(new ArrayList<>());
        deviceRecyclerView.setAdapter(deviceAdapter);

        apiService = ApiClient.getClient().create(BackendApiService.class);

        // Assign device button listener
        assignDeviceButton.setOnClickListener(view -> showAssignDeviceDialog());

        // Unregister device button listener
        unregisterDeviceButton.setOnClickListener(view -> showUnregisterDeviceDialog());

        // View device list button listener
        viewDeviceListButton.setOnClickListener(view -> {
            deviceListTitle.setVisibility(View.VISIBLE);
            deviceRecyclerView.setVisibility(View.VISIBLE);
            fetchDeviceList();
        });
    }

    // Show assign device dialog
    private void showAssignDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Assign Device");

        // Inflate the dialog layout
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_assign_device, null, false);

        final EditText uidEditText = viewInflated.findViewById(R.id.uidEditText);
        final EditText houseIdEditText = viewInflated.findViewById(R.id.houseIdEditText);

        builder.setView(viewInflated);

        builder.setPositiveButton("Assign", (dialog, which) -> {
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
                Toast.makeText(PiManagementActivity.this, "House ID must be a number", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Show unregister device dialog
    private void showUnregisterDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unregister Device");

        // Inflate the dialog layout
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_unregister_device, null, false);

        final EditText uidEditText = viewInflated.findViewById(R.id.uidEditText);

        builder.setView(viewInflated);

        builder.setPositiveButton("Unregister", (dialog, which) -> {
            String uid = uidEditText.getText().toString().trim();

            if (uid.isEmpty()) {
                Toast.makeText(PiManagementActivity.this, "Please enter UID", Toast.LENGTH_SHORT).show();
                return;
            }

            UnregisterDeviceRequest request = new UnregisterDeviceRequest(uid);
            unregisterDevice(request);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Assign device to house
    private void assignDeviceToHouse(AssignDeviceRequest request) {
        Call<ResponseBody> call = apiService.assignDeviceToHouse(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Toast.makeText(PiManagementActivity.this, "Device assigned successfully: " + responseBody, Toast.LENGTH_SHORT).show();
                        fetchDeviceList(); // Refresh device list
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

    // Unregister device
    private void unregisterDevice(UnregisterDeviceRequest request) {
        Call<ResponseBody> call = apiService.unregisterDevice(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PiManagementActivity.this, "Device unregistered successfully", Toast.LENGTH_SHORT).show();
                    fetchDeviceList(); // Refresh device list
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(PiManagementActivity.this, "Failed to unregister: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(PiManagementActivity.this, "Failed to read error message", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(PiManagementActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch device list
    private void fetchDeviceList() {
        Call<List<Device>> call = apiService.getDeviceList();
        call.enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Device> deviceList = response.body();
                    deviceAdapter.updateDeviceList(deviceList);
                } else {
                    Toast.makeText(PiManagementActivity.this, "Failed to fetch device list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                Toast.makeText(PiManagementActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}