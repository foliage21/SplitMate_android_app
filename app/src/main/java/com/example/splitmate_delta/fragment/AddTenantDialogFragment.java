package com.example.splitmate_delta.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.splitmate_delta.R;

public class AddTenantDialogFragment extends DialogFragment {

    private EditText mEtEmail;
    private EditText mEtUsername;
    private EditText mEtPassword;
    private EditText mEtConfirmationCode;
    private Uri mVideoUri;
    private Spinner mSpinnerProperty;
    private AddTenantListener listener;

    public interface AddTenantListener {
        void onSendVerificationCode(String email, String username, String password, Uri videoUri, String houseId);
        void onConfirmSignup(String username, String confirmationCode);
    }

    public void setListener(AddTenantListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_tenant_dialog, null);

        mEtEmail = view.findViewById(R.id.etTenantEmail);
        mEtUsername = view.findViewById(R.id.etTenantUsername);
        mEtPassword = view.findViewById(R.id.etTenantPassword);
        mEtConfirmationCode = view.findViewById(R.id.etConfirmationCode);
        mSpinnerProperty = view.findViewById(R.id.spinnerTenantProperty);
        Button btnSelectVideo = view.findViewById(R.id.btnSelectVideo);
        Button btnSendVerificationCode = view.findViewById(R.id.btnSendVerificationCode);

        // "Add" button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String username = mEtUsername.getText().toString().trim();
            String confirmationCode = mEtConfirmationCode.getText().toString().trim();

            if (listener != null) {
                listener.onConfirmSignup(username, confirmationCode);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dismiss());

        // Upload video button click event
        btnSelectVideo.setOnClickListener(v -> showVideoSelectionDialog());

        // Send VerificationCode button click event
        btnSendVerificationCode.setOnClickListener(v -> {
            String email = mEtEmail.getText().toString().trim();
            String username = mEtUsername.getText().toString().trim();
            String password = mEtPassword.getText().toString().trim();
            String houseId = mSpinnerProperty.getSelectedItem().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(houseId)) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mVideoUri == null) {
                Toast.makeText(getContext(), "Please select the video first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onSendVerificationCode(email, username, password, mVideoUri, houseId);
            }
        });

        builder.setView(view);
        return builder.create();
    }

    // Displays a dialog box to select the video
    private void showVideoSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select a video source");
        builder.setItems(new CharSequence[]{"shoot a video", "upload a video"}, (dialog, which) -> {
            if (which == 0) {
                openCameraForVideo();
            } else {
                openVideoSelector();
            }
        });
        builder.show();
    }

    // Turn on the camera to shoot video
    private void openCameraForVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10); //Limit video length to 10 seconds
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);   // Set video quality
        captureVideoLauncher.launch(intent);
    }

    // Select the video from your device's media library
    private void openVideoSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        pickVideoLauncher.launch(intent);
    }

    // shoot ActivityResultLauncher
    private final ActivityResultLauncher<Intent> captureVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                            mVideoUri = result.getData().getData();
                            Toast.makeText(getContext(), "Successful video shooting", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Video shooting failed", Toast.LENGTH_SHORT).show();
                        }
                    });

    // select ActivityResultLauncher
    private final ActivityResultLauncher<Intent> pickVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                            mVideoUri = result.getData().getData();
                            Toast.makeText(getContext(), "Video selection successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Video selection failure", Toast.LENGTH_SHORT).show();
                        }
                    });
}