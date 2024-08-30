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

    private Button mAddRemoveTenant;
    private Button mTenantUsageRecords;
    private Button mManagePermissions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_dashboard, container, false);

        mAddRemoveTenant = view.findViewById(R.id.AddRemoveTenant);
        mTenantUsageRecords = view.findViewById(R.id.TenantUsageRecords);
        mManagePermissions = view.findViewById(R.id.ManagePermissions);

        mAddRemoveTenant.setOnClickListener(v -> {
            // Add/Remove tenant
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ManageTenantsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        mTenantUsageRecords.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Tenant Usage Records clicked", Toast.LENGTH_SHORT).show();
            // TODO: view tenant usage
        });

        mManagePermissions.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Manage Permissions clicked", Toast.LENGTH_SHORT).show();
            // TODO: manage facility permissions
        });

        return view;
    }
}