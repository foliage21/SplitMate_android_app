package com.example.splitmate_delta.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.signup.ConfirmSignupRequest;
import com.example.splitmate_delta.models.signup.SignupRequest;
import com.example.splitmate_delta.utils.S3UploadUtils;
import com.example.splitmate_delta.utils.VideoUtils;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEtEmail, mEtUsername, mEtPassword, mEtConfirmPassword, mEtConfirmationCode, mEtHouseId;
    private Button mBtnRegister, mBtnUploadPhoto, mBtnConfirmSignup;
    private Uri mVideoUri;
    private RadioGroup mRgRole;

    private BackendApiService apiService;
    private S3UploadUtils s3UploadUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getApiService();
        s3UploadUtils = new S3UploadUtils(apiService); // S3UploadUtils

        mEtEmail = findViewById(R.id.et_register_email);
        mEtUsername = findViewById(R.id.et_register_user);
        mEtPassword = findViewById(R.id.et_register_password);
        mEtConfirmPassword = findViewById(R.id.et_register_confirm_password);
        mEtConfirmationCode = findViewById(R.id.et_register_confirmation_code);
        mEtHouseId = findViewById(R.id.et_register_houseId);
        mBtnRegister = findViewById(R.id.btn_register);
        mBtnUploadPhoto = findViewById(R.id.btn_upload_photo);
        mBtnConfirmSignup = findViewById(R.id.btn_confirm_signup);
        mRgRole = findViewById(R.id.rg_role);

        // The Settings button brings up a dialog box that lets the user choose to shoot a video or upload a video
        mBtnUploadPhoto.setOnClickListener(v -> showVideoSelectionDialog());

        // Register button
        mBtnRegister.setOnClickListener(v -> {
            if (mVideoUri != null) {
                processVideoAndUpload(mVideoUri); // Extract frames and upload them
            } else {
                registerUser(null); // Sign up directly when there is no video
            }
        });

        // Confirm registration button recognition
        mBtnConfirmSignup.setOnClickListener(v -> confirmSignup());
    }

    // Displays a dialog to choose whether to shoot or upload a video
    private void showVideoSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10); // Limit video length to 10 seconds
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);   // Set video quality
        captureVideoLauncher.launch(intent);
    }

    // Select the video from your device's media library
    private void openVideoSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10); // Limit video length to 10 seconds
        pickVideoLauncher.launch(intent);
    }

    // shoot ActivityResultLauncher
    private final ActivityResultLauncher<Intent> captureVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri videoUri = result.getData().getData();
                            mVideoUri = videoUri; // URI
                            Toast.makeText(this, "Successful video shooting", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Video shooting failed", Toast.LENGTH_SHORT).show();
                        }
                    });

    // select ActivityResultLauncher
    private final ActivityResultLauncher<Intent> pickVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri videoUri = result.getData().getData();
                            mVideoUri = videoUri; // URI
                            Toast.makeText(this, "Video selection successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Video selection failed", Toast.LENGTH_SHORT).show();
                        }
                    });

    // Process the video and upload it
    private void processVideoAndUpload(Uri videoUri) {
        // Extract frames from the video
        List<android.graphics.Bitmap> frames = VideoUtils.extractFramesFromVideo(this, videoUri, 30);

        if (frames == null || frames.isEmpty()) {
            Toast.makeText(this, "Failed to extract any frames", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload frames to S3
        s3UploadUtils.uploadFrames(this, frames, new S3UploadUtils.UploadCallback() {
            @Override
            public void onUploadCompleted(List<String> photoUrls) {
                runOnUiThread(() -> {
                    if (!photoUrls.isEmpty()) {
                        // Use the URL of the last image to register the user
                        String imageUrl = photoUrls.get(photoUrls.size() - 1);
                        registerUser(imageUrl);
                    } else {
                        Toast.makeText(RegisterActivity.this, "No successfully uploaded images", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onUploadFailed() {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // User registration
    private void registerUser(String imageUrl) {
        String email = mEtEmail.getText().toString().trim();
        String username = mEtUsername.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();
        String confirmPassword = mEtConfirmPassword.getText().toString().trim();
        String role = ((RadioButton) findViewById(mRgRole.getCheckedRadioButtonId())).getText().toString().toLowerCase();

        String houseIdStr = mEtHouseId.getText().toString().trim();
        if (houseIdStr.isEmpty()) {
            Toast.makeText(this, "Please enter House ID", Toast.LENGTH_SHORT).show();
            return;
        }

        int houseId;
        try {
            houseId = Integer.parseInt(houseIdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid House ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password mismatch", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register using the URL of the image
        SignupRequest signupRequest = new SignupRequest(username, password, email, houseId, role, imageUrl);

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
                    Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();

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
}