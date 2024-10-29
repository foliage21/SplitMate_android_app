package com.example.splitmate_delta.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.models.pi.Device;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<Device> deviceList;

    public DeviceAdapter(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    public void updateDeviceList(List<Device> newDeviceList) {
        this.deviceList = newDeviceList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new DeviceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.deviceNameTextView.setText("Device Name: " + device.getDeviceName());
        holder.statusTextView.setText("Status: " + device.getStatus());
        holder.houseIdTextView.setText("House ID: " + device.getHouseId());
        holder.macAddressTextView.setText("MAC Address: " + device.getMacAddress());
        holder.typeTextView.setText("Type: " + device.getType());
    }

    @Override
    public int getItemCount() {
        return deviceList != null ? deviceList.size() : 0;
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView deviceNameTextView, statusTextView, houseIdTextView, macAddressTextView, typeTextView;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.deviceNameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            houseIdTextView = itemView.findViewById(R.id.houseIdTextView);
            macAddressTextView = itemView.findViewById(R.id.macAddressTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
        }
    }
}