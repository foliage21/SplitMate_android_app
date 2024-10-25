package com.example.splitmate_delta.billfragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.splitmate_delta.models.bills.DownloadBillResponse;
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
    private TextView mTotalBill, mDueDate;
    private Button mDownloadBillButton;
    private BackendApiService apiService;
    private double totalAmount = 0;
    private String dueDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_bills, container, false);

        mWaterBill = view.findViewById(R.id.waterBill);
        mElectricityBill = view.findViewById(R.id.electricityBill);
        mInternetBill = view.findViewById(R.id.internetBill);
        mGasBill = view.findViewById(R.id.gasBill);
        mTotalBill = view.findViewById(R.id.totalBill);
        mDueDate = view.findViewById(R.id.dueDate);
        mDownloadBillButton = view.findViewById(R.id.downloadBillButton);

        apiService = ApiClient.getApiService();

        // Gets the current user ID
        int tenantId = getCurrentUserId();
        if (tenantId != -1) {
            loadBills(tenantId);
        } else {
            Toast.makeText(getContext(), "User not logged in. Please log in to view your bills.",
                    Toast.LENGTH_SHORT).show();
        }

        mDownloadBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tenantId = getCurrentUserId();
                if (tenantId != -1) {
                    downloadBill(tenantId);
                } else {
                    Toast.makeText(getContext(), "User not logged in. Please log in to download your bill.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    // Get the user ID from SharedPreferences
    private int getCurrentUserId() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("userId", -1);
    }

    // Load bills from API
    private void loadBills(int tenantId) {
        Call<List<BillByUserId>> call = apiService.getBillsByUserId(tenantId);
        call.enqueue(new Callback<List<BillByUserId>>() {
            @Override
            public void onResponse(Call<List<BillByUserId>> call, Response<List<BillByUserId>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BillByUserId> bills = response.body();
                    totalAmount = 0;
                    dueDate = "";

                    for (BillByUserId bill : bills) {
                        updateBillText(bill);
                        totalAmount += bill.getAmount();
                        if (dueDate.isEmpty()) {
                            dueDate = bill.getDueDate();
                        }
                    }

                    // Total bill and due date
                    mTotalBill.setText("Total Bill: $" + String.format("%.2f", totalAmount));
                    mDueDate.setText("Due Date: " + dueDate);

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
                mWaterBill.setText(bill.getName() + ": $" + String.format("%.2f", bill.getAmount()));
                break;
            case "electricity bill":
                mElectricityBill.setText(bill.getName() + ": $" + String.format("%.2f", bill.getAmount()));
                break;
            case "internet bill":
                mInternetBill.setText(bill.getName() + ": $" + String.format("%.2f", bill.getAmount()));
                break;
            case "gas bill":
                mGasBill.setText(bill.getName() + ": $" + String.format("%.2f", bill.getAmount()));
                break;
        }
    }

    // Download the bill PDF
    private void downloadBill(int tenantId) {
        Call<DownloadBillResponse> call = apiService.generateBillPdf(tenantId);
        call.enqueue(new Callback<DownloadBillResponse>() {
            @Override
            public void onResponse(Call<DownloadBillResponse> call, Response<DownloadBillResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getUrl();
                    // Open the URL in the browser
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(getContext(), "Failed to download bill", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DownloadBillResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}