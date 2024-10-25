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
import com.example.splitmate_delta.activityforlandlord.PiManagementActivity;
import com.example.splitmate_delta.activityforlandlord.ManagePermissionsActivity;
import com.example.splitmate_delta.activityforlandlord.ManageTenantsActivity;
import com.example.splitmate_delta.activityforlandlord.TenantUsageRecordsActivity;
import com.google.android.material.card.MaterialCardView;

public class LandlordDashboardFragment extends Fragment {

    private MaterialCardView mAddRemoveTenantCard;
    private MaterialCardView mTenantUsageRecordsCard;
    private MaterialCardView mManagePermissionsCard;
    private MaterialCardView mDeviceManagementCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_dashboard, container, false);

        mAddRemoveTenantCard = view.findViewById(R.id.AddRemoveTenantCard);
        mTenantUsageRecordsCard = view.findViewById(R.id.TenantUsageRecordsCard);
        mManagePermissionsCard = view.findViewById(R.id.ManagePermissionsCard);
        mDeviceManagementCard = view.findViewById(R.id.DeviceManagementCard);

        mAddRemoveTenantCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageTenantsActivity.class)));
        mTenantUsageRecordsCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), TenantUsageRecordsActivity.class)));
        mManagePermissionsCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManagePermissionsActivity.class)));
        mDeviceManagementCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), PiManagementActivity.class)));

        loadImageForImageView(view, R.id.AddRemoveTenantImage, "https://cdn.langeek.co/photo/22289/original/?type=jpeg");
        loadImageForImageView(view, R.id.TenantUsageRecordsImage, "https://cdn.langeek.co/photo/36257/original/?type=jpeg");
        loadImageForImageView(view, R.id.ManagePermissionsImage, "https://cdn.langeek.co/photo/46776/original/?type=jpeg");
        loadImageForImageView(view, R.id.DeviceManagementImage, "https://cdn.langeek.co/photo/33081/original/?type=jpeg");

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