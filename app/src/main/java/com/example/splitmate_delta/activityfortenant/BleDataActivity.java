package com.example.splitmate_delta.activityfortenant;

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
import android.widget.Button; // Added import for Button
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.splitmate_delta.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BleDataActivity extends AppCompatActivity {

    private static final String BMD300_MAC_ADDRESS = "F2:41:15:61:63:3E";
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private TextView statusTextView, lastOperationTimeTextView, lastOperationTypeTextView, totalUsageTimeTextView;
    private ImageView statusIcon;
    private long deviceTurnOnTimestamp = 0;
    private Button turnOnButton, turnOffButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_data);

        initUIComponents();

        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

    }

    private void initUIComponents() {
        statusTextView = findViewById(R.id.statusTextView);
        lastOperationTimeTextView = findViewById(R.id.lastOperationTimeTextView);
        lastOperationTypeTextView = findViewById(R.id.lastOperationTypeTextView);
        totalUsageTimeTextView = findViewById(R.id.totalUsageTimeTextView);
        statusIcon = findViewById(R.id.statusIcon);

        // Initialize buttons
        turnOnButton = findViewById(R.id.turnOnButton);
        turnOffButton = findViewById(R.id.turnOffButton);

        // Set click listeners
        turnOnButton.setOnClickListener(v -> {
            checkPermissionsAndScan(); // Start scanning and connect
        });

        turnOffButton.setOnClickListener(v -> {
            disconnectFromDevice(); // Disconnect from device
        });
    }

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
            showToast("Scanning for device...");
            bluetoothLeScanner.startScan(leScanCallback);
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
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            updateUIOnConnectionChange("Device disconnected", R.color.red, "Turned off", timestamp);
                            if (deviceTurnOnTimestamp != 0) {
                                long duration = timestamp - deviceTurnOnTimestamp;
                                String durationString = formatDuration(duration);
                                totalUsageTimeTextView.setText("Total usage time: " + durationString);
                            }
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

    // Disconnect from device
    private void disconnectFromDevice() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
            runOnUiThread(() -> {
                long timestamp = System.currentTimeMillis();
                updateUIOnConnectionChange("Device disconnected", R.color.red, "Turned off", timestamp);
                if (deviceTurnOnTimestamp != 0) {
                    long duration = timestamp - deviceTurnOnTimestamp;
                    String durationString = formatDuration(duration);
                    totalUsageTimeTextView.setText("Total usage time: " + durationString);
                }
                deviceTurnOnTimestamp = 0;
                showToast("Disconnected from device");
            });
        } else {
            showToast("No device to disconnect");
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
                new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                1
        );
    }

    // Permission
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