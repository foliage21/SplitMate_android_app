package com.example.splitmate_delta.fragment;

import android.content.Intent;
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

        // Glide
        Glide.with(this)
                .load("https://cdn.langeek.co/photo/48233/original/correct?type=jpeg")
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mCurrentPermissions.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        mCurrentPermissions.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Current Permissions clicked", Toast.LENGTH_SHORT).show();
            // TODO: view current permissions
        });

        // Glide
        Glide.with(this)
                .load("https://cdn.langeek.co/photo/48551/original/write?type=jpeg")
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mUsageRecords.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        mUsageRecords.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Usage Records clicked", Toast.LENGTH_SHORT).show();
            // TODO: view usage records
        });

        // Glide
        Glide.with(this)
                .load("https://cdn.langeek.co/photo/50683/original/hand?type=jpeg")
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mRequestAccessPermissions.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        mRequestAccessPermissions.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Request Access Permissions clicked", Toast.LENGTH_SHORT).show();
            // TODO: request access permissions
        });

        // Glide
        Glide.with(this)
                .load("https://cdn.langeek.co/photo/49820/original/?type=jpeg")
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mControlDevice.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        mControlDevice.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BleDataActivity.class);
            startActivity(intent);
        });

        return view;
    }
}