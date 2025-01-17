package com.example.splitmate_delta.activityfortenant;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.usage.UsageRecord;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsageRecordsActivity extends AppCompatActivity {

    private BackendApiService apiService;
    private LinearLayout usageRecordsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_records);

        usageRecordsContainer = findViewById(R.id.usage_records_container);
        apiService = ApiClient.getApiService();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            fetchUsageRecords(userId);
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUsageRecords(int userId) {
        Call<List<UsageRecord>> call = apiService.getUsageRecordsByUserId(userId);
        call.enqueue(new Callback<List<UsageRecord>>() {
            @Override
            public void onResponse(Call<List<UsageRecord>> call, Response<List<UsageRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UsageRecord> usageRecords = response.body();
                    displayUsageRecords(usageRecords);
                } else {
                    Toast.makeText(UsageRecordsActivity.this, "Failed to fetch usage records", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UsageRecord>> call, Throwable t) {
                Toast.makeText(UsageRecordsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUsageRecords(List<UsageRecord> usageRecords) {
        usageRecordsContainer.removeAllViews();

        for (UsageRecord record : usageRecords) {
            MaterialCardView cardView = new MaterialCardView(this);
            cardView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            cardView.setCardElevation(8f);
            cardView.setRadius(12f);
            cardView.setUseCompatPadding(true);

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

            if (record.getEndTime() != null && !record.getEndTime().isEmpty()) {
                TextView stopTime = new TextView(this);
                stopTime.setText("Stop Time: " + record.getEndTime());
                stopTime.setTextSize(18);
                layout.addView(stopTime);

                TextView usageTime = new TextView(this);
                usageTime.setText("Total Usage Time: " + calculateUsageTime(record.getStartTime(), record.getEndTime()));
                usageTime.setTextSize(18);
                layout.addView(usageTime);
            } else {
                TextView noEndTime = new TextView(this);
                noEndTime.setText("End Time: N/A");
                noEndTime.setTextSize(18);
                layout.addView(noEndTime);
            }

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