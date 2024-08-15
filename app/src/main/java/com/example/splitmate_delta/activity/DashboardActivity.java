package com.example.splitmate_delta.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.splitmate_delta.fragment.LandlordBillsFragment;
import com.example.splitmate_delta.fragment.LandlordDashboardFragment;
import com.example.splitmate_delta.R;
import com.example.splitmate_delta.fragment.TenantBillsFragment;
import com.example.splitmate_delta.fragment.TenantDashboardFragment;
import com.example.splitmate_delta.fragment.AccountFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        userRole = getIntent().getStringExtra("user_role");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_dashboard) {
                loadFragment(getDashboardFragment());
                return true;
            } else if (id == R.id.navigation_bills) {
                loadFragment(getBillsFragment());
                return true;
            } else if (id == R.id.navigation_account) {
                loadFragment(new AccountFragment());
                return true;
            }
            return false;
        });

        // 默认加载Dashboard
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
        }
    }

    private Fragment getDashboardFragment() {
        if ("landlord".equals(userRole)) {
            return new LandlordDashboardFragment();
        } else {
            return new TenantDashboardFragment();
        }
    }

    private Fragment getBillsFragment() {
        if ("landlord".equals(userRole)) {
            return new LandlordBillsFragment(); // 创建并实现 LandlordBillsFragment
        } else {
            return new TenantBillsFragment(); // 创建并实现 TenantBillsFragment
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}