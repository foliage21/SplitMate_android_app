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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ManageTenantsFragment extends Fragment {

    private ListView tenantListView;
    private Button addTenantButton;
    private Spinner propertySpinner;
    private DatabaseReference mDatabase;
    private StorageReference mStorageReference;
    private FirebaseAuth mAuth;
    private TenantListAdapter adapter;
    private ArrayList<String> tenantList = new ArrayList<>();
    private ArrayList<String> tenantIdList = new ArrayList<>();
    private String selectedProperty = "House 1";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_tenants, container, false);

        tenantListView = view.findViewById(R.id.tenantListView);
        addTenantButton = view.findViewById(R.id.addTenantButton);
        propertySpinner = view.findViewById(R.id.spinnerPropertySelection);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mStorageReference = FirebaseStorage.getInstance().getReference("profile_images");
        mAuth = FirebaseAuth.getInstance();

        // Set up custom adapter
        adapter = new TenantListAdapter();
        tenantListView.setAdapter(adapter);

        // Set up property spinner adapter
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.property_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propertySpinner.setAdapter(spinnerAdapter);

        // Set listener for property spinner
        propertySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProperty = parent.getItemAtPosition(position).toString();
                loadTenantList(selectedProperty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Load tenant list
        loadTenantList(selectedProperty);

        // Set add tenant button click event
        addTenantButton.setOnClickListener(v -> showAddTenantDialog());

        return view;
    }

    private void loadTenantList(String selectedProperty) {
        mDatabase.orderByChild("role").equalTo("tenant").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tenantList.clear();
                tenantIdList.clear();

                for (DataSnapshot tenantSnapshot : task.getResult().getChildren()) {
                    String tenantProperty = tenantSnapshot.child("property").getValue(String.class);
                    if (tenantProperty != null && tenantProperty.equals(selectedProperty)) {  // Filter tenants by property
                        String tenantName = tenantSnapshot.child("username").getValue(String.class);
                        String tenantId = tenantSnapshot.getKey();

                        tenantList.add(tenantName != null ? tenantName : "Unknown User");
                        tenantIdList.add(tenantId); // Store the tenant ID
                    }
                }

                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(getActivity(), "Failed to load tenants: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed to load tenants: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void showAddTenantDialog() {
        // Create a dialog for landlord to input new tenant information
        AddTenantDialogFragment dialog = new AddTenantDialogFragment();
        dialog.setListener((email, username, password, imageUri, property) -> {
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(property)) {
                Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register tenant using Firebase Authentication with the selected property
            registerTenant(email, username, password, imageUri, property);
        });
        dialog.show(getChildFragmentManager(), "AddTenantDialogFragment");
    }

    private void registerTenant(String email, String username, String password, Uri imageUri, String property) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String tenantId = user.getUid();

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("username", username);
                            userMap.put("email", email);
                            userMap.put("role", "tenant");
                            userMap.put("property", property);

                            if (imageUri != null) {
                                uploadProfileImage(tenantId, userMap, imageUri);
                            } else {
                                writeUserData(tenantId, userMap);
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadProfileImage(String userId, HashMap<String, String> userMap, Uri imageUri) {
        String imageName = UUID.randomUUID().toString();
        StorageReference imageRef = mStorageReference.child(userId + "/" + imageName);

        imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    userMap.put("profileImageUrl", uri.toString());
                    writeUserData(userId, userMap);
                })
        ).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void writeUserData(String userId, HashMap<String, String> userMap) {
        mDatabase.child(userId).setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Tenant added successfully", Toast.LENGTH_SHORT).show();
                loadTenantList(selectedProperty);
            } else {
                Toast.makeText(getActivity(), "Failed to add tenant: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void removeTenant(int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Tenant")
                .setMessage("Are you sure you want to delete this tenant?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String tenantId = tenantIdList.get(position);

                    // First, remove the profile image from Firebase Storage
                    mDatabase.child(tenantId).child("profileImageUrl").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String profileImageUrl = task.getResult().getValue(String.class);
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImageUrl);
                                imageRef.delete().addOnSuccessListener(aVoid -> {
                                    // Successfully deleted image from storage, now delete user data
                                    mDatabase.child(tenantId).removeValue().addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Tenant removed successfully", Toast.LENGTH_SHORT).show();
                                            loadTenantList(selectedProperty);  // Re-load the tenant list
                                        } else {
                                            Toast.makeText(getActivity(), "Failed to remove tenant from database", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Failed to delete profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                // If there is no profile image, proceed to delete user data directly
                                mDatabase.child(tenantId).removeValue().addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Tenant removed successfully", Toast.LENGTH_SHORT).show();
                                        loadTenantList(selectedProperty);  // Re-load the tenant list
                                    } else {
                                        Toast.makeText(getActivity(), "Failed to remove tenant from database", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getActivity(), "Failed to get profile image URL", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

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

            String tenantName = getItem(position);

            TextView tenantNameTextView = convertView.findViewById(R.id.tenantNameTextView);
            Button deleteTenantButton = convertView.findViewById(R.id.deleteTenantButton);

            tenantNameTextView.setText(tenantName);
            deleteTenantButton.setOnClickListener(v -> removeTenant(position));

            return convertView;
        }
    }
}