package com.example.splitmate_delta.billfragment;

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
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.User;
import com.example.splitmate_delta.models.bills.BillByUserId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandlordBillsFragment extends Fragment {

    private AutoCompleteTextView propertyAutoComplete, tenantAutoComplete;
    private ArrayAdapter<String> tenantAdapter;
    private ArrayList<String> tenantList = new ArrayList<>();
    private ArrayList<String> tenantIdList = new ArrayList<>();
    private BackendApiService apiService;
    private int selectedHouseId;
    private TextView mWaterBill, mElectricityBill, mInternetBill, mGasBill;

    private List<String> houseIdList = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_bills, container, false);

        apiService = ApiClient.getApiService();

        propertyAutoComplete = view.findViewById(R.id.spinnerPropertySelection);
        tenantAutoComplete = view.findViewById(R.id.spinnerTenantSelection);
        mWaterBill = view.findViewById(R.id.waterBill);
        mElectricityBill = view.findViewById(R.id.electricityBill);
        mInternetBill = view.findViewById(R.id.internetBill);
        mGasBill = view.findViewById(R.id.gasBill);

        propertyAutoComplete.setOnClickListener(v -> propertyAutoComplete.showDropDown());
        tenantAutoComplete.setOnClickListener(v -> tenantAutoComplete.showDropDown());

        // Load all users and populate the property list
        loadAllUsersAndPopulateProperties();

        // Listen for Property selection
        propertyAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            try {
                selectedHouseId = Integer.parseInt(selectedItem);
                loadTenantList(selectedHouseId);  // Load tenants for selected property
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid property selected.", Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for Tenant selection
        tenantAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
            String tenantId = tenantIdList.get(position);  // Get selected tenant ID
            loadTenantBills(tenantId);  // Load bills for selected tenant
        });

        return view;
    }

    // Load all users and extract a unique list of house IDs
    private void loadAllUsersAndPopulateProperties() {
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsers = response.body();

                    // Extract unique house IDs
                    Set<Integer> houseIdSet = new HashSet<>();
                    for (User user : allUsers) {
                        houseIdSet.add(user.getHouseId());
                    }

                    // Convert house IDs to a list of strings
                    houseIdList = new ArrayList<>();
                    for (Integer houseId : houseIdSet) {
                        houseIdList.add(String.valueOf(houseId));
                    }

                    if (houseIdList.isEmpty()) {
                        Toast.makeText(getContext(), "No properties found.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Set the adapter for the property selection box using the house ID list
                        ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                houseIdList
                        );
                        propertyAutoComplete.setAdapter(propertyAdapter);
                    }

                } else {
                    Toast.makeText(getContext(), "Failed to load properties", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading properties", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Load the list of tenants for a given house ID
    private void loadTenantList(int houseId) {
        tenantList.clear();
        tenantIdList.clear();

        for (User user : allUsers) {
            if ("tenant".equalsIgnoreCase(user.getRole()) && user.getHouseId() == houseId) {
                tenantList.add(user.getName());
                tenantIdList.add(String.valueOf(user.getId()));
            }
        }

        if (tenantList.isEmpty()) {
            Toast.makeText(getContext(), "No tenants found for this property.", Toast.LENGTH_SHORT).show();
            tenantAutoComplete.setAdapter(null);
        } else {
            tenantAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, tenantList);
            tenantAutoComplete.setAdapter(tenantAdapter);
            tenantAutoComplete.showDropDown();
        }
    }

    // Load bills for a given tenant ID
    private void loadTenantBills(String tenantId) {
        apiService.getBillsByUserId(Integer.parseInt(tenantId)).enqueue(new Callback<List<BillByUserId>>() {
            @Override
            public void onResponse(Call<List<BillByUserId>> call, Response<List<BillByUserId>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayBills(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to load bills", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BillByUserId>> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading bills", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Display bill information
    private void displayBills(List<BillByUserId> bills) {
        // Reset bills
        mWaterBill.setText("Water Bill: $0.00");
        mElectricityBill.setText("Electricity Bill: $0.00");
        mInternetBill.setText("Internet Bill: $0.00");
        mGasBill.setText("Gas Bill: $0.00");

        if (bills.isEmpty()) {
            Toast.makeText(getContext(), "No bills found for this tenant.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (BillByUserId bill : bills) {
            String billName = bill.getName().toLowerCase();
            String amountText = "$" + String.format("%.2f", bill.getAmount());

            if (billName.contains("water")) {
                mWaterBill.setText("Water Bill: " + amountText);
            } else if (billName.contains("electricity")) {
                mElectricityBill.setText("Electricity Bill: " + amountText);
            } else if (billName.contains("internet")) {
                mInternetBill.setText("Internet Bill: " + amountText);
            } else if (billName.contains("gas")) {
                mGasBill.setText("Gas Bill: " + amountText);
            }
        }
    }
}