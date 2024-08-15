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

public class TenantDashboardFragment extends Fragment {

    private Button mBtnViewBills;
    private Button mBtnViewPermissions;
    private Button mBtnViewUsage;
    private Button mBtnRequestPermission;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_dashboard, container, false);

        mBtnViewBills = view.findViewById(R.id.btn_view_bills);
        mBtnViewPermissions = view.findViewById(R.id.btn_view_permissions);
        mBtnViewUsage = view.findViewById(R.id.btn_view_usage);
        mBtnRequestPermission = view.findViewById(R.id.btn_request_permission);

        mBtnViewBills.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "View Bills clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement view bills functionality
        });

        mBtnViewPermissions.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "View Permissions clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement view permissions functionality
        });

        mBtnViewUsage.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "View Usage clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement view usage functionality
        });

        mBtnRequestPermission.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Request Permission clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement request permission functionality
        });

        return view;
    }
}