package com.example.splitmate_delta.fragment;

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
import androidx.fragment.app.Fragment;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.User;
import com.example.splitmate_delta.models.signup.SignupRequest;
import com.example.splitmate_delta.models.signup.ConfirmSignupRequest;
import com.example.splitmate_delta.models.addphoto.AddPhotoResponse;
import com.example.splitmate_delta.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageTenantsFragment extends Fragment {

    private ListView tenantListView;
    private Button addTenantButton;
    private Spinner propertySpinner;
    private BackendApiService apiService;
    private TenantListAdapter adapter;
    private ArrayList<String> tenantList = new ArrayList<>();
    private ArrayList<String> tenantIdList = new ArrayList<>();
    private int selectedHouseId = 1;  //
    private Uri mImageUri;  //
    private String uploadedImageUrl;  //

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_tenants, container, false);

        apiService = ApiClient.getApiService();

        tenantListView = view.findViewById(R.id.tenantListView);
        addTenantButton = view.findViewById(R.id.addTenantButton);
        propertySpinner = view.findViewById(R.id.spinnerPropertySelection);

        adapter = new TenantListAdapter();
        tenantListView.setAdapter(adapter);

        // Set up the Spinner adapter
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(),
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

        return view;
    }

    // Load the tenant list
    private void loadTenantList(int houseId) {
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    tenantList.clear();
                    tenantIdList.clear();

                    // Filter tenants with the specified houseId
                    for (User user : response.body()) {
                        if ("tenant".equals(user.getRole()) && user.getHouseId() == houseId) {
                            tenantList.add(user.getName());
                            tenantIdList.add(String.valueOf(user.getId()));
                        }
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "Failed to load tenants", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(getActivity(), "Error loading tenants: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Show Add Tenant Dialog
    private void showAddTenantDialog() {
        AddTenantDialogFragment dialog = new AddTenantDialogFragment();
        dialog.setListener(new AddTenantDialogFragment.AddTenantListener() {
            @Override
            public void onSendVerificationCode(String email, String username, String password, Uri imageUri, String houseId) {
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(houseId)) {
                    Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                mImageUri = imageUri;
                File file = null;
                if (mImageUri != null) {
                    file = FileUtils.getFileFromUri(getContext(), mImageUri);
                }

                if (file != null) {
                    uploadPhotoAndRegister(file, email, username, password, Integer.parseInt(houseId));
                } else {
                    registerTenant(email, username, password, null, Integer.parseInt(houseId));
                }
            }

            @Override
            public void onConfirmSignup(String username, String confirmationCode) {
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(confirmationCode)) {
                    Toast.makeText(getActivity(), "Username and Confirmation Code are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                confirmSignup(username, confirmationCode);
            }
        });
        dialog.show(getChildFragmentManager(), "AddTenantDialogFragment");
    }

    // Upload image and register user
    private void uploadPhotoAndRegister(File file, String email, String username, String password, int houseId) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        apiService.uploadPhoto(body).enqueue(new Callback<AddPhotoResponse>() {
            @Override
            public void onResponse(Call<AddPhotoResponse> call, Response<AddPhotoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Get the uploaded image URL
                    uploadedImageUrl = response.body().getPhotoUrl();

                    // After successful image upload, register the tenant
                    registerTenant(email, username, password, uploadedImageUrl, houseId);
                } else {
                    Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddPhotoResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Error uploading image", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "Verification code sent to email", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "Registration failed: " + errorCode + "\n" + errorMessage, Toast.LENGTH_LONG).show();
                    System.out.println("Registration failed: " + errorCode + "\n" + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getActivity(), "Registration error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "Registration confirmed successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, refresh the tenant list
                    loadTenantList(selectedHouseId);
                } else {
                    Toast.makeText(getActivity(), "Confirmation failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getActivity(), "Confirmation error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Remove tenant
    private void removeTenant(String tenantId, String tenantName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Tenant")
                .setMessage("Are you sure you want to delete tenant: " + tenantName + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    apiService.deleteUser(tenantId).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getActivity(), "Tenant removed successfully", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getActivity(), "Failed to remove tenant: " + errorCode + "\n" + errorMessage, Toast.LENGTH_LONG).show();
                                System.out.println("Failed to remove tenant: " + errorCode + "\n" + errorMessage + "\nRequest URL: " + requestUrl);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getActivity(), "Error removing tenant: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Custom adapter
    private class TenantListAdapter extends ArrayAdapter<String> {
        TenantListAdapter() {
            super(getActivity(), R.layout.item_tenant_delete, tenantList);
        }

        @NonNull
        @Override
        public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tenant_delete, parent, false);
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