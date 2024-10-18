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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.ble.DeviceStatusUpdate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BleDataActivity extends AppCompatActivity {

    private static final String MAC_ADDRESS_DEVICE1 = "F2:41:15:61:63:3E"; // TV
    private static final String MAC_ADDRESS_DEVICE2 = "C1:1A:52:E5:40:C3"; // Microwave

    private BluetoothLeScanner bluetoothLeScanner;
    private Map<String, BluetoothGatt> bluetoothGattMap = new HashMap<>();
    private Map<String, Long> deviceTurnOnTimestampMap = new HashMap<>();

    private TextView device1StatusTextView, device1LastOperationTimeTextView, device1LastOperationTypeTextView, device1TotalUsageTimeTextView;
    private ImageView device1StatusIcon;
    private Button device1TurnOnButton, device1TurnOffButton;

    private TextView device2StatusTextView, device2LastOperationTimeTextView, device2LastOperationTypeTextView, device2TotalUsageTimeTextView;
    private ImageView device2StatusIcon;
    private Button device2TurnOnButton, device2TurnOffButton;

    private String userId;

    // Device name mapping
    private Map<String, String> deviceNameMap = new HashMap<>();

    // Requested device MAC address
    private String requestedDeviceMacAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_data);

        initUIComponents();

        // Get userId
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = String.valueOf(sharedPreferences.getInt("userId", -1));
        if (userId.equals("-1")) {
            showToast("User ID is not available");
        }

        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // Initialize device name mapping
        deviceNameMap.put(MAC_ADDRESS_DEVICE1, "TV");
        deviceNameMap.put(MAC_ADDRESS_DEVICE2, "Microwave");
    }

    private void initUIComponents() {
        // Initialize components for device 1
        device1StatusTextView = findViewById(R.id.device1StatusTextView);
        device1LastOperationTimeTextView = findViewById(R.id.device1LastOperationTimeTextView);
        device1LastOperationTypeTextView = findViewById(R.id.device1LastOperationTypeTextView);
        device1TotalUsageTimeTextView = findViewById(R.id.device1TotalUsageTimeTextView);
        device1StatusIcon = findViewById(R.id.device1StatusIcon);
        device1TurnOnButton = findViewById(R.id.device1TurnOnButton);
        device1TurnOffButton = findViewById(R.id.device1TurnOffButton);

        // Initialize components for device 2
        device2StatusTextView = findViewById(R.id.device2StatusTextView);
        device2LastOperationTimeTextView = findViewById(R.id.device2LastOperationTimeTextView);
        device2LastOperationTypeTextView = findViewById(R.id.device2LastOperationTypeTextView);
        device2TotalUsageTimeTextView = findViewById(R.id.device2TotalUsageTimeTextView);
        device2StatusIcon = findViewById(R.id.device2StatusIcon);
        device2TurnOnButton = findViewById(R.id.device2TurnOnButton);
        device2TurnOffButton = findViewById(R.id.device2TurnOffButton);

        // Set listeners for device 1 buttons
        device1TurnOnButton.setOnClickListener(v -> {
            requestedDeviceMacAddress = MAC_ADDRESS_DEVICE1;
            checkPermissionsAndScan();
        });

        device1TurnOffButton.setOnClickListener(v -> {
            disconnectFromDevice(MAC_ADDRESS_DEVICE1);
        });

        // Set listeners for device 2 buttons
        device2TurnOnButton.setOnClickListener(v -> {
            requestedDeviceMacAddress = MAC_ADDRESS_DEVICE2;
            checkPermissionsAndScan();
        });

        device2TurnOffButton.setOnClickListener(v -> {
            disconnectFromDevice(MAC_ADDRESS_DEVICE2);
        });
    }

    // Scan for devices
    private void scanForDevice() {
        if (checkBluetoothPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            showToast("Scanning for device...");
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            showToast("Bluetooth scan permission not granted");
        }
    }

    // Connect to a device
    private void connectToDevice(BluetoothDevice device) {
        String macAddress = device.getAddress();
        if (checkBluetoothPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            BluetoothGatt bluetoothGatt = device.connectGatt(this, false, new DeviceGattCallback(macAddress));
            bluetoothGattMap.put(macAddress, bluetoothGatt);
        } else {
            showToast("Bluetooth connection permission not granted");
        }
    }

    // Disconnect from a device
    private void disconnectFromDevice(String macAddress) {
        BluetoothGatt bluetoothGatt = bluetoothGattMap.get(macAddress);
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGattMap.remove(macAddress);

            runOnUiThread(() -> {
                long timestamp = System.currentTimeMillis();
                updateUIOnConnectionChange(macAddress, "Device disconnected", R.color.red, "Turned off", timestamp);
                Long deviceTurnOnTimestamp = deviceTurnOnTimestampMap.get(macAddress);
                if (deviceTurnOnTimestamp != null) {
                    long duration = timestamp - deviceTurnOnTimestamp;
                    String durationString = formatDuration(duration);
                    getTotalUsageTimeTextView(macAddress).setText("Total usage time: " + durationString);
                    deviceTurnOnTimestampMap.remove(macAddress);
                }
                showToast("Disconnected from device: " + deviceNameMap.get(macAddress));

                // Send status update
                String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
                DeviceStatusUpdate statusUpdate = new DeviceStatusUpdate(userId, macAddress, "off", formattedTime);
                sendDeviceStatusUpdate(statusUpdate);
            });
        } else {
            showToast("No device to disconnect");
        }
    }

    // Update UI on connection change
    private void updateUIOnConnectionChange(String macAddress, String statusMessage, int colorResId, String operationType, long timestamp) {
        String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
        TextView statusTextView = getStatusTextView(macAddress);
        ImageView statusIcon = getStatusIcon(macAddress);
        TextView lastOperationTimeTextView = getLastOperationTimeTextView(macAddress);
        TextView lastOperationTypeTextView = getLastOperationTypeTextView(macAddress);

        statusTextView.setText("Device status: " + statusMessage);
        statusIcon.setColorFilter(ContextCompat.getColor(BleDataActivity.this, colorResId), android.graphics.PorterDuff.Mode.SRC_IN);
        lastOperationTimeTextView.setText("Last operation time: " + formattedTime);
        lastOperationTypeTextView.setText("Last operation type: " + operationType);
    }

    // Get the corresponding TextView and ImageView
    private TextView getStatusTextView(String macAddress) {
        if (macAddress.equals(MAC_ADDRESS_DEVICE1)) {
            return device1StatusTextView;
        } else if (macAddress.equals(MAC_ADDRESS_DEVICE2)) {
            return device2StatusTextView;
        }
        return null;
    }

    private ImageView getStatusIcon(String macAddress) {
        if (macAddress.equals(MAC_ADDRESS_DEVICE1)) {
            return device1StatusIcon;
        } else if (macAddress.equals(MAC_ADDRESS_DEVICE2)) {
            return device2StatusIcon;
        }
        return null;
    }

    private TextView getLastOperationTimeTextView(String macAddress) {
        if (macAddress.equals(MAC_ADDRESS_DEVICE1)) {
            return device1LastOperationTimeTextView;
        } else if (macAddress.equals(MAC_ADDRESS_DEVICE2)) {
            return device2LastOperationTimeTextView;
        }
        return null;
    }

    private TextView getLastOperationTypeTextView(String macAddress) {
        if (macAddress.equals(MAC_ADDRESS_DEVICE1)) {
            return device1LastOperationTypeTextView;
        } else if (macAddress.equals(MAC_ADDRESS_DEVICE2)) {
            return device2LastOperationTypeTextView;
        }
        return null;
    }

    private TextView getTotalUsageTimeTextView(String macAddress) {
        if (macAddress.equals(MAC_ADDRESS_DEVICE1)) {
            return device1TotalUsageTimeTextView;
        } else if (macAddress.equals(MAC_ADDRESS_DEVICE2)) {
            return device2TotalUsageTimeTextView;
        }
        return null;
    }

    // Check permissions and scan
    private void checkPermissionsAndScan() {
        if (hasPermissions()) {
            scanForDevice();
        } else {
            requestPermissions();
        }
    }

    // Send device status update
    private void sendDeviceStatusUpdate(DeviceStatusUpdate statusUpdate) {
        BackendApiService apiService = ApiClient.getApiService();
        Call<ResponseBody> call = apiService.updateDeviceStatus(statusUpdate);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    showToast("Device status updated successfully");
                } else {
                    showToast("Failed to update device status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showToast("Error updating device status: " + t.getMessage());
            }
        });
    }

    // BLE scan callback
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String macAddress = device.getAddress();
            if (macAddress.equals(requestedDeviceMacAddress)) {
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

    // Custom GattCallback
    private class DeviceGattCallback extends BluetoothGattCallback {
        private String macAddress;

        public DeviceGattCallback(String macAddress) {
            this.macAddress = macAddress;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            long timestamp = System.currentTimeMillis();
            runOnUiThread(() -> {
                String state;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    deviceTurnOnTimestampMap.put(macAddress, timestamp);
                    updateUIOnConnectionChange(macAddress, "Device connected", R.color.green, "Turned on", timestamp);
                    state = "on";
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    updateUIOnConnectionChange(macAddress, "Device disconnected", R.color.red, "Turned off", timestamp);
                    state = "off";
                    Long deviceTurnOnTimestamp = deviceTurnOnTimestampMap.get(macAddress);
                    if (deviceTurnOnTimestamp != null) {
                        long duration = timestamp - deviceTurnOnTimestamp;
                        String durationString = formatDuration(duration);
                        getTotalUsageTimeTextView(macAddress).setText("Total usage time: " + durationString);
                        deviceTurnOnTimestampMap.remove(macAddress);
                    }
                } else {
                    return;
                }

                // Send device status update
                String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
                DeviceStatusUpdate statusUpdate = new DeviceStatusUpdate(userId, macAddress, state, formattedTime);
                sendDeviceStatusUpdate(statusUpdate);
            });
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            runOnUiThread(() -> {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    showToast("Services discovered for " + deviceNameMap.get(macAddress));
                } else {
                    showToast("Failed to discover services for " + deviceNameMap.get(macAddress) + ": " + status);
                }
            });
        }
    }

    // Check Bluetooth permissions
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

    private boolean hasPermissions() {
        return checkBluetoothPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                checkBluetoothPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
                checkBluetoothPermission(Manifest.permission.ACCESS_FINE_LOCATION);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Handle permission request results
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanForDevice();
            } else {
                showToast("Bluetooth permission denied");
            }
        }
    }
}