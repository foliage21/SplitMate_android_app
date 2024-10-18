package com.example.splitmate_delta.activityforlandlord;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.User;
import com.example.splitmate_delta.models.usage.UsageRecord;
import com.google.android.material.card.MaterialCardView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Typeface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TenantUsageRecordsActivity extends AppCompatActivity {

    private BackendApiService apiService;
    private LinearLayout usageRecordsContainer;
    private AutoCompleteTextView propertyAutoComplete, tenantAutoComplete;

    private List<String> houseIdList = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private ArrayList<String> tenantList = new ArrayList<>();
    private ArrayList<String> tenantIdList = new ArrayList<>();

    private int selectedHouseId;
    private String selectedTenantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_usage_records);

        usageRecordsContainer = findViewById(R.id.usage_records_container);
        propertyAutoComplete = findViewById(R.id.spinnerPropertySelection);
        tenantAutoComplete = findViewById(R.id.spinnerTenantSelection);
        apiService = ApiClient.getApiService();

        propertyAutoComplete.setOnClickListener(v -> propertyAutoComplete.showDropDown());
        tenantAutoComplete.setOnClickListener(v -> tenantAutoComplete.showDropDown());

        loadAllUsersAndPopulateProperties();

        propertyAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            try {
                selectedHouseId = Integer.parseInt(selectedItem);
                loadTenantList(selectedHouseId);
            } catch (NumberFormatException e) {
                Toast.makeText(TenantUsageRecordsActivity.this, "Invalid property selected.", Toast.LENGTH_SHORT).show();
            }
        });

        tenantAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
            selectedTenantId = tenantIdList.get(position);
            fetchUsageRecords(selectedTenantId);
        });
    }

    private void loadAllUsersAndPopulateProperties() {
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsers = response.body();

                    // Extract a unique house ID
                    Set<Integer> houseIdSet = new HashSet<>();
                    for (User user : allUsers) {
                        houseIdSet.add(user.getHouseId());
                    }

                    // Converts house ids to a list of strings
                    houseIdList = new ArrayList<>();
                    for (Integer houseId : houseIdSet) {
                        houseIdList.add(String.valueOf(houseId));
                    }

                    if (houseIdList.isEmpty()) {
                        Toast.makeText(TenantUsageRecordsActivity.this, "No properties found.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Use the house ID list to set the adapter for the property selection box
                        ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>(
                                TenantUsageRecordsActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                houseIdList
                        );
                        propertyAutoComplete.setAdapter(propertyAdapter);
                    }

                } else {
                    Toast.makeText(TenantUsageRecordsActivity.this, "Failed to load properties", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(TenantUsageRecordsActivity.this, "Error loading properties", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Loads a list of tenants for a given house ID
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
            Toast.makeText(TenantUsageRecordsActivity.this, "No tenants found for this property.", Toast.LENGTH_SHORT).show();
            tenantAutoComplete.setAdapter(null);
        } else {
            ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>(TenantUsageRecordsActivity.this,
                    android.R.layout.simple_dropdown_item_1line, tenantList);
            tenantAutoComplete.setAdapter(tenantAdapter);
            tenantAutoComplete.showDropDown();
        }
    }

    private void fetchUsageRecords(String tenantId) {
        // Clear the previous usage history display
        usageRecordsContainer.removeAllViews();

        apiService.getUsageRecordsByUserId(Integer.parseInt(tenantId)).enqueue(new Callback<List<UsageRecord>>() {
            @Override
            public void onResponse(Call<List<UsageRecord>> call, Response<List<UsageRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UsageRecord> usageRecords = response.body();
                    displayUsageRecords(usageRecords);
                } else {
                    Toast.makeText(TenantUsageRecordsActivity.this, "Failed to fetch usage records", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UsageRecord>> call, Throwable t) {
                Toast.makeText(TenantUsageRecordsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUsageRecords(List<UsageRecord> usageRecords) {
        // 清空容器中的旧视图
        usageRecordsContainer.removeAllViews();

        if (usageRecords.isEmpty()) {
            Toast.makeText(this, "No usage records found for this tenant.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iterate over each record returned by the API
        for (UsageRecord record : usageRecords) {
            // MaterialCardView
            MaterialCardView cardView = new MaterialCardView(this);
            cardView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            cardView.setCardElevation(8f);
            cardView.setRadius(12f);
            cardView.setUseCompatPadding(true);
            cardView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            ((LinearLayout.LayoutParams) cardView.getLayoutParams()).setMargins(0, 0, 0, 16);

            LinearLayout layout = new LinearLayout(this);
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(16, 16, 16, 16);

            TextView deviceName = new TextView(this);
            deviceName.setText("Device: " + record.getDeviceUid());
            deviceName.setTextSize(20);
            deviceName.setTypeface(null, Typeface.BOLD);
            layout.addView(deviceName);

            TextView startTime = new TextView(this);
            startTime.setText("Start Time: " + record.getStartTime());
            startTime.setTextSize(18);
            layout.addView(startTime);

            TextView stopTime = new TextView(this);
            stopTime.setText("Stop Time: " + record.getEndTime());
            stopTime.setTextSize(18);
            layout.addView(stopTime);

            TextView usageTime = new TextView(this);
            usageTime.setText("Total Usage Time: " + calculateUsageTime(record.getStartTime(), record.getEndTime()));
            usageTime.setTextSize(18);
            layout.addView(usageTime);

            cardView.addView(layout);

            usageRecordsContainer.addView(cardView);
        }
    }

    private String calculateUsageTime(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            long diffInMillis = end.getTime() - start.getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long hours = minutes / 60;
            minutes = minutes % 60;
            return hours + " hours " + minutes + " minutes";
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
}