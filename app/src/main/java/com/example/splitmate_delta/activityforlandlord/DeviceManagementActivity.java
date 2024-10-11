package com.example.splitmate_delta.activityforlandlord;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.fragment.AddPiDialogFragment;
import com.example.splitmate_delta.fragment.ManagePiFragment;
import com.example.splitmate_delta.fragment.AddDeviceFragment;
import com.example.splitmate_delta.fragment.ManageDeviceFragment;

public class DeviceManagementActivity extends AppCompatActivity {

    private Button addPiButton;
    private Button managePiButton;
    private Button addDeviceButton;
    private Button manageDeviceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_management);

        addPiButton = findViewById(R.id.launchAddPiDialogButton);
        managePiButton = findViewById(R.id.managePiButton);
        addDeviceButton = findViewById(R.id.launchAddDeviceFragmentButton);
        manageDeviceButton = findViewById(R.id.manageDeviceButton);

        setupListeners();
    }

    private void setupListeners() {

        addPiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment addPiDialogFragment = new AddPiDialogFragment();
                addPiDialogFragment.show(getSupportFragmentManager(), "AddPiDialogFragment");
            }
        });

        managePiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManagePiFragment managePiFragment = new ManagePiFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, managePiFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDeviceFragment addDeviceFragment = new AddDeviceFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, addDeviceFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        manageDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageDeviceFragment manageDeviceFragment = new ManageDeviceFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, manageDeviceFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }
}