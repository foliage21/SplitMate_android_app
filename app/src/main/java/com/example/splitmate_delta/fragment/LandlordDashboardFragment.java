package com.example.splitmate_delta.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.splitmate_delta.R;

public class LandlordDashboardFragment extends Fragment {

    private Button mBtnAddTenant;
    private Button mBtnViewTenantUsage;
    private Button mBtnViewTenantBills;
    private Button mBtnManageFacilityPermissions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_dashboard, container, false);

        mBtnAddTenant = view.findViewById(R.id.btn_add_tenant);
        mBtnViewTenantUsage = view.findViewById(R.id.btn_view_tenant_usage);
        mBtnViewTenantBills = view.findViewById(R.id.btn_view_tenant_bills);
        mBtnManageFacilityPermissions = view.findViewById(R.id.btn_manage_facility_permissions);

        mBtnAddTenant.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Add Tenant clicked", Toast.LENGTH_SHORT).show();
            // TODO: add tenant
        });

        mBtnViewTenantUsage.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "View Tenant Usage clicked", Toast.LENGTH_SHORT).show();
            // TODO: view tenant usage
        });

        mBtnViewTenantBills.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "View Tenant Bills clicked", Toast.LENGTH_SHORT).show();
            // TODO: view tenant bills
        });

        mBtnManageFacilityPermissions.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Manage Facility Permissions clicked", Toast.LENGTH_SHORT).show();
            // TODO: manage facility permissions
        });

        return view;
    }
}