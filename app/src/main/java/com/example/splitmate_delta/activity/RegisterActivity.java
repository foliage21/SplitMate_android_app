package com.example.splitmate_delta.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEtUser;
    private EditText mEtPassword;
    private EditText mEtConfirmPassword;
    private Button mBtnRegister;
    private Button mBtnUploadPhoto;
    private ImageView mImgProfile;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mEtUser = findViewById(R.id.et_register_user);
        mEtPassword = findViewById(R.id.et_register_password);
        mEtConfirmPassword = findViewById(R.id.et_register_confirm_password);
        mBtnRegister = findViewById(R.id.btn_register);
        mBtnUploadPhoto = findViewById(R.id.btn_upload_photo);
        mImgProfile = findViewById(R.id.img_profile);

        // Sign up for image upload options
        mBtnUploadPhoto.setOnClickListener(v -> openImageSelector());

        mBtnRegister.setOnClickListener(v -> {
            String username = mEtUser.getText().toString();
            String password = mEtPassword.getText().toString();
            String confirmPassword = mEtConfirmPassword.getText().toString();

            if (password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
        });
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
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            mImageUri = result.getData().getData();
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                                mImgProfile.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
}