package com.example.splitmate_delta.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.splitmate_delta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TenantBillsFragment extends Fragment {

    private TextView mWaterBill, mElectricityBill, mInternetBill, mGasBill;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_bills, container, false);

        // Initializes
        mWaterBill = view.findViewById(R.id.waterBill);
        mElectricityBill = view.findViewById(R.id.electricityBill);
        mInternetBill = view.findViewById(R.id.internetBill);
        mGasBill = view.findViewById(R.id.gasBill);

        // Get the currently logged in user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String tenantId = currentUser.getUid();
            // // Initializes a Firebase database reference to the current user's billing data
            databaseReference = FirebaseDatabase.getInstance().getReference("bills").child(tenantId);
            // Load and display billing data from Firebase
            loadBills();
        } else {
            Toast.makeText(getContext(), "User not logged in. Please log in to view your bills.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadBills() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Reads billing data from Firebase and handles possible Long type conversions
                    Long waterBillValue = snapshot.child("waterBill").getValue(Long.class);
                    Long electricityBillValue = snapshot.child("electricityBill").getValue(Long.class);
                    Long internetBillValue = snapshot.child("internetBill").getValue(Long.class);
                    Long gasBillValue = snapshot.child("gasBill").getValue(Long.class);

                    // Converts the Long type to String
                    String waterBill = (waterBillValue != null) ? String.valueOf(waterBillValue) : "N/A";
                    String electricityBill = (electricityBillValue != null) ? String.valueOf(electricityBillValue) : "N/A";
                    String internetBill = (internetBillValue != null) ? String.valueOf(internetBillValue) : "N/A";
                    String gasBill = (gasBillValue != null) ? String.valueOf(gasBillValue) : "N/A";

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