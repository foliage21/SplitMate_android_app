package com.example.splitmate_delta.billfragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.example.splitmate_delta.models.bills.DownloadBillResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TenantBillsFragment extends Fragment {

    private TextView mWaterBill, mElectricityBill, mInternetBill, mGasBill;
    private TextView mTotalBill, mDueDate;
    private MaterialButton mDownloadBillButton;
    private BackendApiService apiService;
    private double totalAmount = 0;
    private String dueDate = "";
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_bills, container, false);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            int tenantId = getCurrentUserId();
            if (tenantId != -1) {
                loadBills(tenantId);
            } else {
                Toast.makeText(getContext(), "User not logged in. Please log in to view your bills.",
                        Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Initialize view components
        mWaterBill = view.findViewById(R.id.waterBill);
        mElectricityBill = view.findViewById(R.id.electricityBill);
        mInternetBill = view.findViewById(R.id.internetBill);
        mGasBill = view.findViewById(R.id.gasBill);
        mTotalBill = view.findViewById(R.id.totalBill);
        mDueDate = view.findViewById(R.id.dueDate);
        mDownloadBillButton = view.findViewById(R.id.downloadBillButton);

        apiService = ApiClient.getApiService();

        int tenantId = getCurrentUserId();
        if (tenantId != -1) {
            loadBills(tenantId);
        } else {
            Toast.makeText(getContext(), "User not logged in. Please log in to view your bills.",
                    Toast.LENGTH_SHORT).show();
        }

        mDownloadBillButton.setOnClickListener(v -> {
            int tenantId1 = getCurrentUserId();
            if (tenantId1 != -1) {
                downloadBill(tenantId1);
            } else {
                Toast.makeText(getContext(), "User not logged in. Please log in to download your bill.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private int getCurrentUserId() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("userId", -1);
    }

    private void loadBills(int tenantId) {
        // Start the refreshing animation
        swipeRefreshLayout.setRefreshing(true);
        Log.d("TenantBillsFragment", "loadBills: Starting to load bills for tenantId: " + tenantId);

        Call<List<BillByUserId>> call = apiService.getBillsByUserId(tenantId);
        call.enqueue(new Callback<List<BillByUserId>>() {
            @Override
            public void onResponse(Call<List<BillByUserId>> call, Response<List<BillByUserId>> response) {
                // Stop the refreshing animation
                swipeRefreshLayout.setRefreshing(false);
                Log.d("TenantBillsFragment", "loadBills: Response received, success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    List<BillByUserId> bills = response.body();
                    totalAmount = 0;
                    dueDate = "";

                    // Reset bill displays
                    mWaterBill.setText("Water Bill: $0.00");
                    mElectricityBill.setText("Electricity Bill: $0.00");
                    mInternetBill.setText("Internet Bill: $0.00");
                    mGasBill.setText("Gas Bill: $0.00");
                    mTotalBill.setText("Total Bill: $0.00");
                    mDueDate.setText("Due Date: ");

                    for (BillByUserId bill : bills) {
                        updateBillText(bill);
                        totalAmount += bill.getAmount();
                        if (dueDate.isEmpty()) {
                            dueDate = bill.getDueDate();
                        }
                    }

                    mTotalBill.setText("Total Bill: $" + String.format("%.2f", totalAmount));
                    mDueDate.setText("Due Date: " + dueDate);

                    Log.d("TenantBillsFragment", "loadBills: Bills loaded successfully.");
                } else {
                    Toast.makeText(getContext(), "Failed to load bills", Toast.LENGTH_SHORT).show();
                    Log.d("TenantBillsFragment", "loadBills: Failed to load bills, response not successful.");
                }
            }

            @Override
            public void onFailure(Call<List<BillByUserId>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TenantBillsFragment", "loadBills: Error loading bills - " + t.getMessage(), t);
            }
        });
    }

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
            default:
                break;
        }
    }

    private void downloadBill(int tenantId) {
        Call<DownloadBillResponse> call = apiService.generateBillPdf(
                tenantId,
                "PostmanRuntime/7.42.0",
                "*/*",
                "no-cache",
                "keep-alive",
                "0"
        );
        call.enqueue(new Callback<DownloadBillResponse>() {
            @Override
            public void onResponse(Call<DownloadBillResponse> call, Response<DownloadBillResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getUrl();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    try {
                        startActivity(browserIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getContext(), "No app found to open the URL", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to download bill", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DownloadBillResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to connect to the server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}