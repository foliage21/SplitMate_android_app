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

        mWaterBill = view.findViewById(R.id.waterBill);
        mElectricityBill = view.findViewById(R.id.electricityBill);
        mInternetBill = view.findViewById(R.id.internetBill);
        mGasBill = view.findViewById(R.id.gasBill);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String tenantId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("bills").child(tenantId);
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