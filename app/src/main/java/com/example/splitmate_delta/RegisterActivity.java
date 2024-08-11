package com.example.splitmate_delta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

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

        mBtnUploadPhoto.setOnClickListener(v -> openImageSelector());

        mBtnRegister.setOnClickListener(v -> {
            String username = mEtUser.getText().toString();
            String password = mEtPassword.getText().toString();
            String confirmPassword = mEtConfirmPassword.getText().toString();

            if (password.equals(confirmPassword)) {

                Log.d("RegisterActivity", "Username: " + username + ", Password: " + password);

                // save information to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.apply();

                if (mImageUri != null) {
                    uploadImageToFirebase(mImageUri);
                }

                Toast.makeText(RegisterActivity.this, "registered successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Password not match", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                mImgProfile.setImageBitmap(bitmap);

                // save pic in the local
                saveImageToLocalStorage(bitmap, "profile_image.png");

                // upload pic to the server
                uploadImageToFirebase(mImageUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImageToLocalStorage(Bitmap bitmap, String filename) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Toast.makeText(this, "Image saved locally", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {

    }
}