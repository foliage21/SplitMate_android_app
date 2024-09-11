package com.example.splitmate_delta.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.splitmate_delta.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LandlordBillsFragment extends Fragment {

    private TextInputLayout propertyTextInputLayout, tenantTextInputLayout;
    private AutoCompleteTextView propertyAutoComplete, tenantAutoComplete;
    private TextView mWaterBill, mElectricityBill, mInternetBill, mGasBill;
    private DatabaseReference usersRef, billsRef;
    private List<String> tenantIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_bills, container, false);

        // Initialize
        propertyTextInputLayout = view.findViewById(R.id.propertyTextInputLayout);
        propertyAutoComplete = propertyTextInputLayout.findViewById(R.id.spinner);
        propertyTextInputLayout.setHint("Select Property");

        tenantTextInputLayout = view.findViewById(R.id.tenantTextInputLayout);
        tenantAutoComplete = tenantTextInputLayout.findViewById(R.id.spinner);
        tenantTextInputLayout.setHint("Select Tenant");

        mWaterBill = view.findViewById(R.id.waterBill);
        mElectricityBill = view.findViewById(R.id.electricityBill);
        mInternetBill = view.findViewById(R.id.internetBill);
        mGasBill = view.findViewById(R.id.gasBill);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        billsRef = FirebaseDatabase.getInstance().getReference("bills");

        ArrayAdapter<CharSequence> propertyAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.property_options,
                android.R.layout.simple_dropdown_item_1line
        );
        propertyAutoComplete.setAdapter(propertyAdapter);

        propertyAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedProperty = (String) parent.getItemAtPosition(position);
            loadTenants(selectedProperty);
        });

        return view;
    }

    private void loadTenants(String selectedProperty) {
        usersRef.orderByChild("property").equalTo(selectedProperty)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> tenantList = new ArrayList<>();
                        tenantIds.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String tenantId = snapshot.getKey();
                            String username = snapshot.child("username").getValue(String.class);
                            String role = snapshot.child("role").getValue(String.class);

                            if ("tenant".equals(role)) {
                                tenantList.add(username != null ? username : "Unknown User");
                                tenantIds.add(tenantId);
                            }
                        }

                        if (tenantList.isEmpty()) {
                            tenantList.add("No tenants available");
                        }

                        ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_dropdown_item_1line, tenantList);
                        tenantAutoComplete.setAdapter(tenantAdapter);

                        tenantAutoComplete.setOnItemClickListener((parent, view12, position, id) -> {
                            if (!tenantIds.isEmpty() && position < tenantIds.size()) {
                                String selectedTenantId = tenantIds.get(position);
                                loadTenantBills(selectedTenantId);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Failed to load tenants. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTenantBills(String tenantId) {
        billsRef.child(tenantId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateBillText(mWaterBill, "Water Bill", snapshot.child("waterBill").getValue(Long.class));
                    updateBillText(mElectricityBill, "Electricity Bill", snapshot.child("electricityBill").getValue(Long.class));
                    updateBillText(mInternetBill, "Internet Bill", snapshot.child("internetBill").getValue(Long.class));
                    updateBillText(mGasBill, "Gas Bill", snapshot.child("gasBill").getValue(Long.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load billing data. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBillText(TextView textView, String billType, Long billValue) {
        textView.setText(billType + ": $" + (billValue != null ? String.valueOf(billValue) : "N/A"));
    }
}