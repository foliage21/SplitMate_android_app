package com.example.splitmate_delta.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.models.permissions.PendingRequest;

import java.util.List;
import java.util.Map;

public class PendingRequestAdapter extends RecyclerView.Adapter<PendingRequestAdapter.ViewHolder> {

    private List<PendingRequest> pendingRequests;
    private Map<Integer, String> userNamesMap;
    private OnItemClickListener mListener;

    // Interface to handle button click events
    public interface OnItemClickListener {
        void onApproveClick(PendingRequest request, int position);
        void onDenyClick(PendingRequest request, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public PendingRequestAdapter(List<PendingRequest> pendingRequests, Map<Integer, String> userNamesMap) {
        this.pendingRequests = pendingRequests;
        this.userNamesMap = userNamesMap;
    }

    @Override
    public PendingRequestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PendingRequestAdapter.ViewHolder holder, int position) {
        PendingRequest request = pendingRequests.get(position);

        String tenantName = userNamesMap.get(request.getUserId());
        String deviceName = request.getDeviceName();
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = "Device";
        }

        holder.tvRequestInfo.setText(tenantName + " requested access to " + deviceName);

        holder.btnApprove.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onApproveClick(request, position);
            }
        });

        holder.btnDeny.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDenyClick(request, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pendingRequests.size();
    }

    // Method to remove an item from the list
    public void removeItem(int position) {
        pendingRequests.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, pendingRequests.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvRequestInfo;
        public Button btnApprove;
        public Button btnDeny;

        public ViewHolder(View itemView) {
            super(itemView);
            tvRequestInfo = itemView.findViewById(R.id.tvRequestInfo);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDeny = itemView.findViewById(R.id.btnDeny);
        }
    }
}