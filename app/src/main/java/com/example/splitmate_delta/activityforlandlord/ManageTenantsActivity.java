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
import android.widget.Spinner;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageTenantsActivity extends AppCompatActivity {

    private ListView tenantListView;
    private Button addTenantButton;
    private Spinner propertySpinner;
    private BackendApiService apiService;
    private TenantListAdapter adapter;
    private ArrayList<String> tenantList = new ArrayList<>();
    private ArrayList<String> tenantIdList = new ArrayList<>();
    private int selectedHouseId = 1;
    private Uri mVideoUri;

    private S3UploadUtils s3UploadUtils;

    private String tenantEmail;
    private String tenantUsername;
    private String tenantPassword;
    private int tenantHouseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_tenants);

        apiService = ApiClient.getApiService();
        s3UploadUtils = new S3UploadUtils(apiService);

        tenantListView = findViewById(R.id.tenantListView);
        addTenantButton = findViewById(R.id.addTenantButton);
        propertySpinner = findViewById(R.id.spinnerPropertySelection);

        adapter = new TenantListAdapter();
        tenantListView.setAdapter(adapter);

        // Set up the Spinner adapter
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.houseId_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propertySpinner.setAdapter(spinnerAdapter);

        // Listen for the Spinner selection
        propertySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                selectedHouseId = Integer.parseInt(parent.getItemAtPosition(position).toString());
                loadTenantList(selectedHouseId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Tenant button click event
        addTenantButton.setOnClickListener(v -> showAddTenantDialog());
    }

    // The Add Tenant dialog box is displayed
    private void showAddTenantDialog() {
        AddTenantDialogFragment dialog = new AddTenantDialogFragment();
        dialog.setListener(new AddTenantDialogFragment.AddTenantListener() {
            @Override
            public void onSendVerificationCode(String email, String username, String password, Uri imageUri, String houseId) {
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(houseId)) {
                    Toast.makeText(ManageTenantsActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                tenantEmail = email;
                tenantUsername = username;
                tenantPassword = password;
                tenantHouseId = Integer.parseInt(houseId);

                if (imageUri != null) {
                    mVideoUri = imageUri;
                    processVideoAndUpload(mVideoUri);
                } else {
                    Toast.makeText(ManageTenantsActivity.this, "No video, please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onConfirmSignup(String username, String confirmationCode) {
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(confirmationCode)) {
                    Toast.makeText(ManageTenantsActivity.this, "The username and verification code are required", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ManageTenantsActivity.this, "The verification code has been sent to the email", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ManageTenantsActivity.this, "fail to register: " + errorCode + "\n" + errorMessage, Toast.LENGTH_LONG).show();
                    System.out.println("fail to register: " + errorCode + "\n" + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ManageTenantsActivity.this, "Registration error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ManageTenantsActivity.this, "registered successfully", Toast.LENGTH_SHORT).show();
                    // refresh the tenant list
                    loadTenantList(selectedHouseId);
                } else {
                    Toast.makeText(ManageTenantsActivity.this, "Confirm registration failure", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ManageTenantsActivity.this, "Confirm registration error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load a tenant list
    private void loadTenantList(int houseId) {
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    tenantList.clear();
                    tenantIdList.clear();

                    // Filter the tenant of the specified houseId
                    for (User user : response.body()) {
                        if ("tenant".equals(user.getRole()) && user.getHouseId() == houseId) {
                            tenantList.add(user.getName());
                            tenantIdList.add(String.valueOf(user.getId()));
                        }
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ManageTenantsActivity.this, "Failed to load tenant", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(ManageTenantsActivity.this, "Error loading tenant: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Remove tenant
    private void removeTenant(String tenantId, String tenantName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tenant")
                .setMessage("Are you sure you want to delete tenant: " + tenantName + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    apiService.deleteUser(tenantId).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ManageTenantsActivity.this, "Tenant removed successfully", Toast.LENGTH_SHORT).show();
                                loadTenantList(selectedHouseId);  // Reload tenant list
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
                                Toast.makeText(ManageTenantsActivity.this, "Failed to remove tenant: " + errorCode + "\n" + errorMessage, Toast.LENGTH_LONG).show();
                                System.out.println("Failed to remove tenant: " + errorCode + "\n" + errorMessage + "\nRequest URL: " + requestUrl);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(ManageTenantsActivity.this, "Error removing tenant: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

            String tenantName = tenantList.get(position);
            String tenantId = tenantIdList.get(position);

            TextView tenantNameTextView = convertView.findViewById(R.id.tenantNameTextView);
            Button deleteTenantButton = convertView.findViewById(R.id.deleteTenantButton);

            tenantNameTextView.setText(tenantName);
            deleteTenantButton.setOnClickListener(v -> removeTenant(tenantId, tenantName));

            return convertView;
        }
    }
}