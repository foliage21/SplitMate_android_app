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
import com.example.splitmate_delta.activityfortenant.BleDataActivity;
import com.example.splitmate_delta.activityfortenant.CurrentPermissionsActivity;
import com.example.splitmate_delta.activityfortenant.UsageRecordsActivity;
import com.example.splitmate_delta.activityfortenant.RequestAccessPermissionsActivity;

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
            Intent intent = new Intent(getActivity(), CurrentPermissionsActivity.class);
            startActivity(intent);
        });

        mUsageRecords.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UsageRecordsActivity.class);
            startActivity(intent);
        });

        mRequestAccessPermissions.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RequestAccessPermissionsActivity.class);
            startActivity(intent);
        });

        mControlDevice.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BleDataActivity.class);
            startActivity(intent);
        });

        // Glide
        loadImageForButton(mCurrentPermissions, "https://cdn.langeek.co/photo/48233/original/correct?type=jpeg");
        loadImageForButton(mUsageRecords, "https://cdn.langeek.co/photo/48551/original/write?type=jpeg");
        loadImageForButton(mRequestAccessPermissions, "https://cdn.langeek.co/photo/50683/original/hand?type=jpeg");
        loadImageForButton(mControlDevice, "https://cdn.langeek.co/photo/49820/original/?type=jpeg");

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