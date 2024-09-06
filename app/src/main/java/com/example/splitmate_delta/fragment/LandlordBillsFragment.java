package com.example.splitmate_delta.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.splitmate_delta.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LandlordBillsFragment extends Fragment {

    private Spinner propertySpinner, tenantSpinner;
    private TextView mWaterBill, mElectricityBill, mInternetBill, mGasBill;
    private DatabaseReference usersRef, billsRef;
    private List<String> tenantIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_bills, container, false);

        // Initialize
        propertySpinner = view.findViewById(R.id.propertySpinner);
        tenantSpinner = view.findViewById(R.id.tenantSpinner);
        mWaterBill = view.findViewById(R.id.waterBill);
        mElectricityBill = view.findViewById(R.id.electricityBill);
        mInternetBill = view.findViewById(R.id.internetBill);
        mGasBill = view.findViewById(R.id.gasBill);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        billsRef = FirebaseDatabase.getInstance().getReference("bills");

        // Set up property spinner
        ArrayAdapter<CharSequence> propertyAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.property_options,
                android.R.layout.simple_spinner_item
        );
        propertyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propertySpinner.setAdapter(propertyAdapter);

        propertySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProperty = parent.getItemAtPosition(position).toString();
                loadTenants(selectedProperty); // Load tenants based on selected property
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    private void loadTenants(String selectedProperty) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> tenantList = new ArrayList<>();
                tenantIds.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String tenantId = snapshot.getKey();
                    String username = snapshot.child("username").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);
                    String property = snapshot.child("property").getValue(String.class);

                    if ("tenant".equals(role) && selectedProperty.equals(property)) {
                        tenantList.add(username != null ? username : "Unknown User");
                        tenantIds.add(tenantId);
                    }
                }

                if (tenantList.isEmpty()) {
                    tenantList.add("No tenants available");
                }

                ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, tenantList);
                tenantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tenantSpinner.setAdapter(tenantAdapter);

                tenantSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (!tenantIds.isEmpty()) {
                            String selectedTenantId = tenantIds.get(position);
                            loadTenantBills(selectedTenantId);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

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
                    Long waterBillValue = snapshot.child("waterBill").getValue(Long.class);
                    Long electricityBillValue = snapshot.child("electricityBill").getValue(Long.class);
                    Long internetBillValue = snapshot.child("internetBill").getValue(Long.class);
                    Long gasBillValue = snapshot.child("gasBill").getValue(Long.class);

                    String waterBill = waterBillValue != null ? String.valueOf(waterBillValue) : "N/A";
                    String electricityBill = electricityBillValue != null ? String.valueOf(electricityBillValue) : "N/A";
                    String internetBill = internetBillValue != null ? String.valueOf(internetBillValue) : "N/A";
                    String gasBill = gasBillValue != null ? String.valueOf(gasBillValue) : "N/A";

                    mWaterBill.setText("Water Bill: $" + waterBill);
                    mElectricityBill.setText("Electricity Bill: $" + electricityBill);
                    mInternetBill.setText("Internet Bill: $" + internetBill);
                    mGasBill.setText("Gas Bill: $" + gasBill);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load billing data. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}