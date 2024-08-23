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
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEtEmail;
    private EditText mEtUsername;
    private EditText mEtPassword;
    private EditText mEtConfirmPassword;
    private Button mBtnRegister;
    private Button mBtnUploadPhoto;
    private ImageView mImgProfile;
    private Uri mImageUri;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mStorageReference = FirebaseStorage.getInstance().getReference("profile_images");

        mEtEmail = findViewById(R.id.et_register_email);
        mEtUsername = findViewById(R.id.et_register_user);
        mEtPassword = findViewById(R.id.et_register_password);
        mEtConfirmPassword = findViewById(R.id.et_register_confirm_password);
        mBtnRegister = findViewById(R.id.btn_register);
        mBtnUploadPhoto = findViewById(R.id.btn_upload_photo);
        mImgProfile = findViewById(R.id.img_profile);

        // Sign up for image upload options
        mBtnUploadPhoto.setOnClickListener(v -> openImageSelector());

        // Set up register button
        mBtnRegister.setOnClickListener(v -> {
            String email = mEtEmail.getText().toString().trim();
            String username = mEtUsername.getText().toString().trim();
            String password = mEtPassword.getText().toString().trim();
            String confirmPassword = mEtConfirmPassword.getText().toString().trim();

            // Validation input
            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register user
            registerUser(email, username, password);
        });
    }

    // Method of user registration
    private void registerUser(String email, String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("username", username);
                            userMap.put("email", email);

                            // Upload profile image
                            if (mImageUri != null) {
                                uploadProfileImage(userId, userMap);
                            } else {
                                writeUserData(userId, userMap);
                            }
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        System.out.println("Error during registration: " + task.getException().getMessage());
                    }
                });
    }

    // Method to upload profile image to Firebase Storage
    private void uploadProfileImage(String userId, HashMap<String, String> userMap) {
        String imageName = UUID.randomUUID().toString();
        StorageReference imageRef = mStorageReference.child(userId + "/" + imageName);

        UploadTask uploadTask = imageRef.putFile(mImageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            userMap.put("profileImageUrl", uri.toString());
            writeUserData(userId, userMap);
        })).addOnFailureListener(e -> {
            Toast.makeText(RegisterActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println("Error uploading image: " + e.getMessage());
        });
    }

    // Method to write user data to Firebase Realtime Database
    private void writeUserData(String userId, HashMap<String, String> userMap) {
        mDatabase.child(userId).setValue(userMap)
                .addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Data write failed: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        System.out.println("Error writing to database: " + dbTask.getException().getMessage());
                    }
                });
    }

    // Open image selector for profile picture
    private void openImageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(intent);
    }

    // The result of image selection
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