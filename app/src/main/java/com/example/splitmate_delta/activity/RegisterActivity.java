package com.example.splitmate_delta.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.addphoto.AddPhotoResponse;
import com.example.splitmate_delta.models.signup.SignupRequest;
import com.example.splitmate_delta.models.signup.ConfirmSignupRequest;
import com.example.splitmate_delta.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEtEmail, mEtUsername, mEtPassword, mEtConfirmPassword, mEtConfirmationCode;
    private Button mBtnRegister, mBtnUploadPhoto, mBtnConfirmSignup;
    private ImageView mImgProfile;
    private Uri mImageUri;
    private RadioGroup mRgRole;
    private Spinner mSpinnerHouseId;
    private String uploadedImageUrl;

    private BackendApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getApiService();

        mEtEmail = findViewById(R.id.et_register_email);
        mEtUsername = findViewById(R.id.et_register_user);
        mEtPassword = findViewById(R.id.et_register_password);
        mEtConfirmPassword = findViewById(R.id.et_register_confirm_password);
        mEtConfirmationCode = findViewById(R.id.et_register_confirmation_code);
        mBtnRegister = findViewById(R.id.btn_register);
        mBtnUploadPhoto = findViewById(R.id.btn_upload_photo);
        mBtnConfirmSignup = findViewById(R.id.btn_confirm_signup);
        mImgProfile = findViewById(R.id.img_profile);
        mRgRole = findViewById(R.id.rg_role);
        mSpinnerHouseId = findViewById(R.id.spinner_houseId);

        // upload pictures
        mBtnUploadPhoto.setOnClickListener(v -> openImageSelector());

        // Register button (click to upload pictures before registering)
        mBtnRegister.setOnClickListener(v -> {
            if (mImageUri != null) {
                File file = FileUtils.getFileFromUri(this, mImageUri);
                if (file != null) {
                    uploadPhotoAndRegister(file); // Upload pictures and register
                } else {
                    Toast.makeText(RegisterActivity.this, "Unable to upload pictures", Toast.LENGTH_SHORT).show();
                }
            } else {
                registerUser(null);
            }
        });

        // Confirm registration button recognition
        mBtnConfirmSignup.setOnClickListener(v -> confirmSignup());
    }

    // Upload pictures and register users
    private void uploadPhotoAndRegister(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        apiService.uploadPhoto(body).enqueue(new Callback<AddPhotoResponse>() {
            @Override
            public void onResponse(Call<AddPhotoResponse> call, Response<AddPhotoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Obtain the URL of the uploaded image
                    uploadedImageUrl = response.body().getPhotoUrl();

                    // After uploading the picture successfully, register the user
                    registerUser(uploadedImageUrl);
                } else {
                    Toast.makeText(RegisterActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddPhotoResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Image uploading error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // user registration
    private void registerUser(String imageUrl) {
        String email = mEtEmail.getText().toString().trim();
        String username = mEtUsername.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();
        String confirmPassword = mEtConfirmPassword.getText().toString().trim();
        String role = ((RadioButton) findViewById(mRgRole.getCheckedRadioButtonId())).getText().toString().toLowerCase();
        String houseId = mSpinnerHouseId.getSelectedItem().toString();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password mismatch", Toast.LENGTH_SHORT).show();
            return;
        }

        SignupRequest signupRequest = new SignupRequest(username, password, email, Integer.parseInt(houseId), role, imageUrl);

        apiService.registerUser(signupRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Verification code sent to email", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Registration error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Confirm registration
    private void confirmSignup() {
        String username = mEtUsername.getText().toString().trim();
        String confirmationCode = mEtConfirmationCode.getText().toString().trim();

        ConfirmSignupRequest confirmSignupRequest = new ConfirmSignupRequest(username, confirmationCode);

        apiService.confirmSignup(confirmSignupRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "registered successfully\n", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(RegisterActivity.this, "Confirm registration failure", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Confirm registration error", Toast.LENGTH_SHORT).show();
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