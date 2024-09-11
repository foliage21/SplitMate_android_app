package com.example.splitmate_delta.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.splitmate_delta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BleDataActivity extends AppCompatActivity {

    private static final String BMD300_MAC_ADDRESS = "F2:41:15:61:63:3E";
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private TextView statusTextView, lastOperationTimeTextView, lastOperationTypeTextView, totalUsageTimeTextView;
    private ImageView statusIcon;
    private DatabaseReference deviceControlsRef;
    private long deviceTurnOnTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_data);

        initUIComponents();

        deviceControlsRef = FirebaseDatabase.getInstance().getReference("deviceControls");

        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        checkPermissionsAndScan();
    }

    private void initUIComponents() {
        statusTextView = findViewById(R.id.statusTextView);
        lastOperationTimeTextView = findViewById(R.id.lastOperationTimeTextView);
        lastOperationTypeTextView = findViewById(R.id.lastOperationTypeTextView);
        totalUsageTimeTextView = findViewById(R.id.totalUsageTimeTextView);
        statusIcon = findViewById(R.id.statusIcon);
    }

    // Processing authority
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanForDevice();
            } else {
                showToast("Bluetooth permission denied");
            }
        }
    }

    // Scan Bluetooth devices
    private void scanForDevice() {
        if (checkBluetoothPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            bluetoothLeScanner.startScan(leScanCallback);  // Use leScanCallback
        } else {
            showToast("Bluetooth scan permission not granted");
        }
    }

    // Connect to device
    private void connectToDevice(BluetoothDevice device) {
        if (checkBluetoothPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    long timestamp = System.currentTimeMillis();
                    runOnUiThread(() -> {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            deviceTurnOnTimestamp = timestamp;
                            updateUIOnConnectionChange("Device connected", R.color.green, "Turned on", timestamp);
                            uploadDataToFirebase("open", timestamp);
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            updateUIOnConnectionChange("Device disconnected", R.color.red, "Turned off", timestamp);
                            if (deviceTurnOnTimestamp != 0) {
                                long duration = timestamp - deviceTurnOnTimestamp;
                                String durationString = formatDuration(duration);
                                totalUsageTimeTextView.setText("Total usage time: " + durationString);
                            }
                            uploadDataToFirebase("close", timestamp);
                        }
                    });
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    runOnUiThread(() -> {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            showToast("Device services discovered");
                        } else {
                            showToast("Failed to discover services: " + status);
                        }
                    });
                }
            });
        } else {
            showToast("Bluetooth connection permission not granted");
        }
    }

    // Update UI
    private void updateUIOnConnectionChange(String statusMessage, int colorResId, String operationType, long timestamp) {
        String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
        statusTextView.setText("Device status: " + statusMessage);
        statusIcon.setColorFilter(ContextCompat.getColor(BleDataActivity.this, colorResId), android.graphics.PorterDuff.Mode.SRC_IN);
        lastOperationTimeTextView.setText("Last operation time: " + formattedTime);
        lastOperationTypeTextView.setText("Last operation type: " + operationType);
    }

    // Firebase
    private void uploadDataToFirebase(String action, long timestamp) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userHistoryRef = deviceControlsRef.child(userId).child("controlHistory").push();

        Map<String, Object> controlData = new HashMap<>();
        controlData.put("action", action);
        controlData.put("timestamp", timestamp);

        userHistoryRef.setValue(controlData)
                .addOnSuccessListener(aVoid -> showToast("Operation recorded"))
                .addOnFailureListener(e -> showToast("Failed to upload: " + e.getMessage()));
    }

    private String formatDuration(long durationInMillis) {
        return String.format(Locale.getDefault(), "%02d hours %02d minutes %02d seconds",
                (durationInMillis / (1000 * 60 * 60)) % 24,
                (durationInMillis / (1000 * 60)) % 60,
                (durationInMillis / 1000) % 60);
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(BleDataActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private boolean checkBluetoothPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                1
        );
    }

    //Permission
    private void checkPermissionsAndScan() {
        if (hasPermissions()) {
            scanForDevice();
        } else {
            requestPermissions();
        }
    }

    private boolean hasPermissions() {
        return checkBluetoothPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                checkBluetoothPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
                checkBluetoothPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    // ScanCallback
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device.getAddress().equals(BMD300_MAC_ADDRESS)) {
                if (checkBluetoothPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                    bluetoothLeScanner.stopScan(leScanCallback);
                    connectToDevice(device);
                } else {
                    showToast("Bluetooth scan permission not granted");
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            showToast("Scan failed: " + errorCode);
        }
    };
}