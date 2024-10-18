package com.example.splitmate_delta.activityforlandlord;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.utils.PendingRequestAdapter;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.permissions.ApproveOrRejectRequest;
import com.example.splitmate_delta.models.permissions.PendingRequest;
import com.example.splitmate_delta.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagePermissionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PendingRequestAdapter adapter;
    private BackendApiService apiService;
    private Map<Integer, String> userNamesMap = new HashMap<>();
    private List<PendingRequest> pendingRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_permissions);

        recyclerView = findViewById(R.id.recyclerViewPendingRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        apiService = ApiClient.getApiService();

        loadPendingPermissionRequests();
    }

    private void loadPendingPermissionRequests() {
        // Get all users to get their names
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User user : response.body()) {
                        userNamesMap.put(user.getId(), user.getName());
                    }
                    // Gets the pending permission request
                    fetchPendingRequests();
                } else {
                    Toast.makeText(ManagePermissionsActivity.this, "Failed to load users.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(ManagePermissionsActivity.this, "Error loading users.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchPendingRequests() {
        apiService.getPendingPermissionRequests().enqueue(new Callback<List<PendingRequest>>() {
            @Override
            public void onResponse(Call<List<PendingRequest>> call, Response<List<PendingRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pendingRequests = response.body();
                    displayPendingRequests(pendingRequests);
                } else {
                    Toast.makeText(ManagePermissionsActivity.this, "Failed to load pending requests.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<PendingRequest>> call, Throwable t) {
                Toast.makeText(ManagePermissionsActivity.this, "Error loading pending requests.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayPendingRequests(List<PendingRequest> pendingRequests) {
        if (pendingRequests.isEmpty()) {
            Toast.makeText(this, "No pending requests.", Toast.LENGTH_LONG).show();
            return;
        }

        adapter = new PendingRequestAdapter(pendingRequests, userNamesMap);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new PendingRequestAdapter.OnItemClickListener() {
            @Override
            public void onApproveClick(PendingRequest request, int position) {
                approveOrRejectRequest(request.getUserId(), request.getMacAddress(), true, position);
            }

            @Override
            public void onDenyClick(PendingRequest request, int position) {
                approveOrRejectRequest(request.getUserId(), request.getMacAddress(), false, position);
            }
        });
    }

    private void approveOrRejectRequest(int userId, String macAddress, boolean approve, int position) {
        ApproveOrRejectRequest request = new ApproveOrRejectRequest(userId, macAddress, approve);

        apiService.approveOrRejectPermission(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    String message = approve ? "Request approved." : "Request denied.";
                    Toast.makeText(ManagePermissionsActivity.this, message, Toast.LENGTH_LONG).show();
                    // Removes processed requests from the list
                    adapter.removeItem(position);
                } else {
                    Toast.makeText(ManagePermissionsActivity.this, "Failed to process the request.", Toast.LENGTH_LONG).show();
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
                Toast.makeText(ManagePermissionsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}