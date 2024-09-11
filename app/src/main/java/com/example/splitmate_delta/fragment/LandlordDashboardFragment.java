package com.example.splitmate_delta.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.splitmate_delta.R;

public class LandlordDashboardFragment extends Fragment {

    private Button mAddRemoveTenant;
    private Button mTenantUsageRecords;
    private Button mManagePermissions;
    private Button mWarningInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_dashboard, container, false);

        mAddRemoveTenant = view.findViewById(R.id.AddRemoveTenant);
        mTenantUsageRecords = view.findViewById(R.id.TenantUsageRecords);
        mManagePermissions = view.findViewById(R.id.ManagePermissions);
        mWarningInfo = view.findViewById(R.id.WarningInfo);

        // Glide
        Glide.with(this)
                .load("https://cdn.langeek.co/photo/22289/original/?type=jpeg")
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mAddRemoveTenant.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        mAddRemoveTenant.setOnClickListener(v -> {
            // Add/Remove tenant
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ManageTenantsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Glide
        Glide.with(this)
                .load("https://cdn.langeek.co/photo/36257/original/?type=jpeg")
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mTenantUsageRecords.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        mTenantUsageRecords.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Tenant Usage Records clicked", Toast.LENGTH_SHORT).show();
            // TODO: view tenant usage
        });

        // Glide
        Glide.with(this)
                .load("https://cdn.langeek.co/photo/46776/original/?type=jpeg")
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mManagePermissions.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        mManagePermissions.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Manage Permissions clicked", Toast.LENGTH_SHORT).show();
            // TODO: handling manage Permissions
        });

        // Glide
        Glide.with(this)
                .load("https://cdn.langeek.co/photo/46378/original/?type=jpeg")
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mWarningInfo.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        mWarningInfo.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Warning Info clicked", Toast.LENGTH_SHORT).show();
            // TODO: receive warning Info
        });

        return view;
    }
}