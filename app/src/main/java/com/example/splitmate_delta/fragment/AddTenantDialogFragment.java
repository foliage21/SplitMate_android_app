package com.example.splitmate_delta.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.splitmate_delta.R;

public class AddTenantDialogFragment extends DialogFragment {

    private EditText mEtEmail;
    private EditText mEtUsername;
    private EditText mEtPassword;
    private EditText mEtConfirmationCode;
    private ImageView mImgProfile;
    private Uri mImageUri;
    private Spinner mSpinnerProperty;
    private AddTenantListener listener;

    public interface AddTenantListener {
        void onSendVerificationCode(String email, String username, String password, Uri imageUri, String houseId);
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
        mImgProfile = view.findViewById(R.id.imgTenantProfile);
        mSpinnerProperty = view.findViewById(R.id.spinnerTenantProperty);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
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

        // Upload image button click event
        btnSelectImage.setOnClickListener(v -> openImageSelector());

        // Send verification code button click event
        btnSendVerificationCode.setOnClickListener(v -> {
            String email = mEtEmail.getText().toString().trim();
            String username = mEtUsername.getText().toString().trim();
            String password = mEtPassword.getText().toString().trim();
            String houseId = mSpinnerProperty.getSelectedItem().toString();

            if (listener != null) {
                listener.onSendVerificationCode(email, username, password, mImageUri, houseId);
            }
        });

        builder.setView(view);
        return builder.create();
    }

    private void openImageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            mImageUri = result.getData().getData();
                            mImgProfile.setImageURI(mImageUri);
                        }
                    });
}