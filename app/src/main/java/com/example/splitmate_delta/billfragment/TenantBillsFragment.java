package com.example.splitmate_delta.billfragment;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.bills.BillByUserId;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TenantBillsFragment extends Fragment {

    private TextView mWaterBill, mElectricityBill, mInternetBill, mGasBill;
    private BackendApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_bills, container, false);

        mWaterBill = view.findViewById(R.id.waterBill);
        mElectricityBill = view.findViewById(R.id.electricityBill);
        mInternetBill = view.findViewById(R.id.internetBill);
        mGasBill = view.findViewById(R.id.gasBill);

        apiService = ApiClient.getApiService();

        // Gets the current user ID
        int tenantId = getCurrentUserId();
        if (tenantId != -1) {
            loadBills(tenantId);
        } else {
            Toast.makeText(getContext(), "User not logged in. Please log in to view your bills.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    // Get the user ID from SharedPreferences
    private int getCurrentUserId() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("userId", -1);
    }

    // API
    private void loadBills(int tenantId) {
        Call<List<BillByUserId>> call = apiService.getBillsByUserId(tenantId);
        call.enqueue(new Callback<List<BillByUserId>>() {
            @Override
            public void onResponse(Call<List<BillByUserId>> call, Response<List<BillByUserId>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BillByUserId> bills = response.body();
                    for (BillByUserId bill : bills) {
                        updateBillText(bill);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load bills", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BillByUserId>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update the billing information and display the name
    private void updateBillText(BillByUserId bill) {
        switch (bill.getName().toLowerCase()) {
            case "water bill":
                mWaterBill.setText(bill.getName() + ": $" + bill.getAmount());
                break;
            case "electricity bill":
                mElectricityBill.setText(bill.getName() + ": $" + bill.getAmount());
                break;
            case "internet bill":
                mInternetBill.setText(bill.getName() + ": $" + bill.getAmount());
                break;
            case "gas bill":
                mGasBill.setText(bill.getName() + ": $" + bill.getAmount());
                break;
        }
    }
}