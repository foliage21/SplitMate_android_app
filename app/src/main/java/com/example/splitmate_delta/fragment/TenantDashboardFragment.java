package com.example.splitmate_delta.fragment;

import android.content.Intent;
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
import com.example.splitmate_delta.activity.BleDataActivity;

public class TenantDashboardFragment extends Fragment {

    private Button mCurrentPermissions;
    private Button mUsageRecords;
    private Button mRequestAccessPermissions;
    private Button mControlDevice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_dashboard, container, false);

        mCurrentPermissions = view.findViewById(R.id.CurrentPermissions);
        mUsageRecords = view.findViewById(R.id.UsageRecords);
        mRequestAccessPermissions = view.findViewById(R.id.RequestAccessPermissions);
        mControlDevice = view.findViewById(R.id.btn_control_device);

        mCurrentPermissions.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Current Permissions clicked", Toast.LENGTH_SHORT).show();
            // TODO: view current permissions
        });

        mUsageRecords.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Usage Records clicked", Toast.LENGTH_SHORT).show();
            // TODO: view usage records
        });

        mRequestAccessPermissions.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Request Access Permissions clicked", Toast.LENGTH_SHORT).show();
            // TODO: request access permissions
        });

        mControlDevice.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BleDataActivity.class);
            startActivity(intent);
        });

        return view;
    }
}