package com.example.splitmate_delta.dashboardfragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.splitmate_delta.R;
import com.example.splitmate_delta.activityforlandlord.ManageTenantsActivity;
import com.example.splitmate_delta.activityforlandlord.TenantUsageRecordsActivity;
import com.example.splitmate_delta.activityforlandlord.ManagePermissionsActivity;
import com.example.splitmate_delta.activityforlandlord.DeviceManagementActivity;

public class LandlordDashboardFragment extends Fragment {

    private Button mAddRemoveTenant;
    private Button mTenantUsageRecords;
    private Button mManagePermissions;
    private Button mDeviceManagement;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_dashboard, container, false);

        mAddRemoveTenant = view.findViewById(R.id.AddRemoveTenant);
        mTenantUsageRecords = view.findViewById(R.id.TenantUsageRecords);
        mManagePermissions = view.findViewById(R.id.ManagePermissions);
        mDeviceManagement = view.findViewById(R.id.DeviceManagement);

        mAddRemoveTenant.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageTenantsActivity.class)));
        mTenantUsageRecords.setOnClickListener(v -> startActivity(new Intent(getActivity(), TenantUsageRecordsActivity.class)));
        mManagePermissions.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManagePermissionsActivity.class)));
        mDeviceManagement.setOnClickListener(v -> startActivity(new Intent(getActivity(), DeviceManagementActivity.class)));

        // Glide
        loadImageForButton(mAddRemoveTenant, "https://cdn.langeek.co/photo/22289/original/?type=jpeg");
        loadImageForButton(mTenantUsageRecords, "https://cdn.langeek.co/photo/36257/original/?type=jpeg");
        loadImageForButton(mManagePermissions, "https://cdn.langeek.co/photo/46776/original/?type=jpeg");
        loadImageForButton(mDeviceManagement, "https://cdn.langeek.co/photo/46378/original/?type=jpeg");

        return view;
    }

    // Glide
    private void loadImageForButton(Button button, String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        button.setBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Do nothing
                    }
                });
    }
}