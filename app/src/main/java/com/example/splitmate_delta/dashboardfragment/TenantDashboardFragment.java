package com.example.splitmate_delta.dashboardfragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import com.google.android.material.card.MaterialCardView;

public class TenantDashboardFragment extends Fragment {

    private MaterialCardView mCurrentPermissionsCard;
    private MaterialCardView mUsageRecordsCard;
    private MaterialCardView mRequestAccessPermissionsCard;
    private MaterialCardView mControlDeviceCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_dashboard, container, false);

        mCurrentPermissionsCard = view.findViewById(R.id.CurrentPermissionsCard);
        mUsageRecordsCard = view.findViewById(R.id.UsageRecordsCard);
        mRequestAccessPermissionsCard = view.findViewById(R.id.RequestAccessPermissionsCard);
        mControlDeviceCard = view.findViewById(R.id.ControlDeviceCard);

        mCurrentPermissionsCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), CurrentPermissionsActivity.class)));
        mUsageRecordsCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), UsageRecordsActivity.class)));
        mRequestAccessPermissionsCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), RequestAccessPermissionsActivity.class)));
        mControlDeviceCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), BleDataActivity.class)));

        loadImageForImageView(view, R.id.CurrentPermissionsImage, "https://cdn.langeek.co/photo/48233/original/correct?type=jpeg");
        loadImageForImageView(view, R.id.UsageRecordsImage, "https://cdn.langeek.co/photo/48551/original/write?type=jpeg");
        loadImageForImageView(view, R.id.RequestAccessPermissionsImage, "https://cdn.langeek.co/photo/50683/original/hand?type=jpeg");
        loadImageForImageView(view, R.id.ControlDeviceImage, "https://cdn.langeek.co/photo/49820/original/?type=jpeg");

        return view;
    }

    private void loadImageForImageView(View parentView, int imageViewId, String imageUrl) {
        ImageView imageView = parentView.findViewById(imageViewId);
        Glide.with(this)
                .load(imageUrl)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Do nothing
                    }
                });
    }
}