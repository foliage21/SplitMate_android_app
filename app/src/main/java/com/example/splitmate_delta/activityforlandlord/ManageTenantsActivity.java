package com.example.splitmate_delta.activityforlandlord;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.fragment.AddTenantDialogFragment;
import com.example.splitmate_delta.models.User;
import com.example.splitmate_delta.models.signup.ConfirmSignupRequest;
import com.example.splitmate_delta.models.signup.SignupRequest;
import com.example.splitmate_delta.utils.S3UploadUtils;
import com.example.splitmate_delta.utils.VideoUtils;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.AutoCompleteTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageTenantsActivity extends AppCompatActivity {

    private ListView tenantListView;
    private Button addTenantButton;
    private AutoCompleteTextView propertyAutoCompleteTextView;
    private BackendApiService apiService;
    private TenantListAdapter adapter;
    private ArrayList<String> tenantList = new ArrayList<>();
    private ArrayList<String> tenantIdList = new ArrayList<>();
    private int selectedHouseId = -1; // Initialize to -1 to indicate no selection
    private Uri mVideoUri;

    private S3UploadUtils s3UploadUtils;

    private String tenantEmail;
    private String tenantUsername;
    private String tenantPassword;
    private int tenantHouseId;

    private List<User> allUsers = new ArrayList<>();
    private List<String> houseIdList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_tenants);

        apiService = ApiClient.getApiService();
        s3UploadUtils = new S3UploadUtils(apiService);

        tenantListView = findViewById(R.id.tenantListView);
        addTenantButton = findViewById(R.id.addTenantButton);
        propertyAutoCompleteTextView = findViewById(R.id.autoCompletePropertySelection);

        adapter = new TenantListAdapter();
        tenantListView.setAdapter(adapter);

        // Load all users and populate property list
        loadAllUsersAndPopulateProperties();

        // Set listener for AutoCompleteTextView selection
        propertyAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                try {
                    selectedHouseId = Integer.parseInt(selectedItem);
                    loadUserList(selectedHouseId); // Load all users under this house
                } catch (NumberFormatException e) {
                    Toast.makeText(ManageTenantsActivity.this, "Invalid property selected.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Tenant button click event
        addTenantButton.setOnClickListener(v -> showAddTenantDialog());
    }

    // Load all users and extract unique house IDs
    private void loadAllUsersAndPopulateProperties() {
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsers = response.body();

                    // Extract unique house IDs
                    Set<Integer> houseIdSet = new HashSet<>();
                    for (User user : allUsers) {
                        houseIdSet.add(user.getHouseId());
                    }

                    // Convert to list of strings and sort
                    houseIdList = new ArrayList<>();
                    for (Integer houseId : houseIdSet) {
                        houseIdList.add(String.valueOf(houseId));
                    }
                    Collections.sort(houseIdList);

                    if (houseIdList.isEmpty()) {
                        Toast.makeText(ManageTenantsActivity.this, "No properties found.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Set up the AutoCompleteTextView adapter with dynamic house IDs
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                ManageTenantsActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                houseIdList
                        );
                        propertyAutoCompleteTextView.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(ManageTenantsActivity.this, "Failed to load properties", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(ManageTenantsActivity.this, "Error loading properties: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // The Add Tenant dialog box is displayed
    private void showAddTenantDialog() {
        if (selectedHouseId == -1) {
            Toast.makeText(this, "Please select a property first.", Toast.LENGTH_SHORT).show();
            return;
        }

        AddTenantDialogFragment dialog = new AddTenantDialogFragment();
        dialog.setListener(new AddTenantDialogFragment.AddTenantListener() {
            @Override
            public void onSendVerificationCode(String email, String username, String password, Uri imageUri, String houseId) {
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(ManageTenantsActivity.this, "Email, username, and password are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                tenantEmail = email;
                tenantUsername = username;
                tenantPassword = password;
                tenantHouseId = selectedHouseId; // Use the selected house ID

                if (imageUri != null) {
                    mVideoUri = imageUri;
                    processVideoAndUpload(mVideoUri);
                } else {
                    Toast.makeText(ManageTenantsActivity.this, "No video selected, please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onConfirmSignup(String username, String confirmationCode) {
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(confirmationCode)) {
                    Toast.makeText(ManageTenantsActivity.this, "Username and verification code are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                confirmSignup(username, confirmationCode);
            }
        });
        dialog.show(getSupportFragmentManager(), "AddTenantDialogFragment");
    }

    private void processVideoAndUpload(Uri videoUri) {
        List<Bitmap> frames = VideoUtils.extractFramesFromVideo(this, videoUri, 30);

        if (frames == null || frames.isEmpty()) {
            Toast.makeText(this, "Failed to extract any frames", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload frames to S3
        s3UploadUtils.uploadFrames(this, frames, new S3UploadUtils.UploadCallback() {
            @Override
            public void onUploadCompleted(List<String> photoUrls) {
                runOnUiThread(() -> {
                    if (!photoUrls.isEmpty()) {
                        // Use the URL of the last image to register a tenant
                        String imageUrl = photoUrls.get(photoUrls.size() - 1);
                        registerTenant(tenantEmail, tenantUsername, tenantPassword, imageUrl, tenantHouseId);
                    } else {
                        Toast.makeText(ManageTenantsActivity.this, "No successfully uploaded images", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onUploadFailed() {
                runOnUiThread(() -> {
                    Toast.makeText(ManageTenantsActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Register tenant
    private void registerTenant(String email, String username, String password, String imageUrl, int houseId) {
        SignupRequest signupRequest = new SignupRequest(username, password, email, houseId, "tenant", imageUrl);

        apiService.registerUser(signupRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageTenantsActivity.this, "Verification code has been sent to the email", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMessage = "";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int errorCode = response.code();
                    Toast.makeText(ManageTenantsActivity.this, "Failed to register: " + errorCode + "\n" + errorMessage, Toast.LENGTH_LONG).show();
                    System.out.println("Failed to register: " + errorCode + "\n" + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ManageTenantsActivity.this, "Registration error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Confirm signup
    private void confirmSignup(String username, String confirmationCode) {
        ConfirmSignupRequest confirmSignupRequest = new ConfirmSignupRequest(username, confirmationCode);

        apiService.confirmSignup(confirmSignupRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageTenantsActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                    // Refresh the user list
                    loadUserList(selectedHouseId);
                } else {
                    Toast.makeText(ManageTenantsActivity.this, "Confirm registration failure", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ManageTenantsActivity.this, "Confirm registration error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load user list for the selected house
    private void loadUserList(int houseId) {
        tenantList.clear();
        tenantIdList.clear();

        for (User user : allUsers) {
            if (user.getHouseId() == houseId) {
                tenantList.add(user.getName() + " (" + user.getRole() + ")");
                tenantIdList.add(String.valueOf(user.getId()));
            }
        }

        adapter.notifyDataSetChanged();
    }

    // Remove tenant
    private void removeUser(String userId, String userName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user: " + userName + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    apiService.deleteUser(userId).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ManageTenantsActivity.this, "User removed successfully", Toast.LENGTH_SHORT).show();
                                loadUserList(selectedHouseId);  // Reload user list
                            } else {
                                String errorMessage = "";
                                try {
                                    if (response.errorBody() != null) {
                                        errorMessage = response.errorBody().string();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                int errorCode = response.code();
                                String requestUrl = call.request().url().toString();
                                Toast.makeText(ManageTenantsActivity.this, "Failed to remove user: " + errorCode + "\n" + errorMessage, Toast.LENGTH_LONG).show();
                                System.out.println("Failed to remove user: " + errorCode + "\n" + errorMessage + "\nRequest URL: " + requestUrl);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(ManageTenantsActivity.this, "Error removing user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Custom adapter
    private class TenantListAdapter extends ArrayAdapter<String> {
        TenantListAdapter() {
            super(ManageTenantsActivity.this, R.layout.item_tenant_delete, tenantList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ManageTenantsActivity.this).inflate(R.layout.item_tenant_delete, parent, false);
            }

            String userName = tenantList.get(position);
            String userId = tenantIdList.get(position);

            TextView tenantNameTextView = convertView.findViewById(R.id.tenantNameTextView);
            Button deleteTenantButton = convertView.findViewById(R.id.deleteTenantButton);

            tenantNameTextView.setText(userName);
            deleteTenantButton.setOnClickListener(v -> removeUser(userId, userName));

            return convertView;
        }
    }
}