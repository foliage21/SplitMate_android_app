package com.example.splitmate_delta.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.addphoto.GeneratePresignedUrlsRequest;
import com.example.splitmate_delta.models.addphoto.GeneratePresignedUrlsResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import retrofit2.Callback;
import retrofit2.Response;

public class S3UploadUtils {

    private OkHttpClient okHttpClient;
    private BackendApiService apiService;
    private Handler mainHandler;

    public S3UploadUtils(BackendApiService apiService) {
        this.okHttpClient = new OkHttpClient();
        this.apiService = apiService;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Upload frames to S3
     *
     * @param context   Context
     * @param frames    List of frames to be uploaded
     * @param callback  Callback after the upload is completed
     */
    public void uploadFrames(Context context, List<Bitmap> frames, UploadCallback callback) {
        List<String> fileNames = new ArrayList<>();
        long timestamp = System.currentTimeMillis();
        for (int i = 0; i < frames.size(); i++) {
            String fileName = "frame_" + timestamp + "_" + i + ".jpg";
            fileNames.add(fileName);
        }

        // Create request body
        GeneratePresignedUrlsRequest request = new GeneratePresignedUrlsRequest(fileNames);

        // Call API to get pre-signed URLs
        apiService.generatePresignedUrls(request).enqueue(new Callback<GeneratePresignedUrlsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<GeneratePresignedUrlsResponse> call, Response<GeneratePresignedUrlsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GeneratePresignedUrlsResponse.PresignedUrlInfo> presignedUrls = response.body().getPresignedUrls();

                    // Upload images to S3 using the pre-signed URLs
                    uploadImagesToS3(context, frames, presignedUrls, callback);
                } else {
                    Toast.makeText(context, "Failed to get pre-signed URLs", Toast.LENGTH_SHORT).show();
                    callback.onUploadFailed();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<GeneratePresignedUrlsResponse> call, Throwable t) {
                Toast.makeText(context, "Error getting pre-signed URLs: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                callback.onUploadFailed();
            }
        });
    }

    private void uploadImagesToS3(Context context, List<Bitmap> frames, List<GeneratePresignedUrlsResponse.PresignedUrlInfo> presignedUrls, UploadCallback callback) {
        if (frames.size() != presignedUrls.size()) {
            Toast.makeText(context, "The number of frames does not match the number of pre-signed URLs", Toast.LENGTH_SHORT).show();
            callback.onUploadFailed();
            return;
        }

        // Thread-safe list to store successfully uploaded image URLs
        List<String> photoUrls = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger remainingUploads = new AtomicInteger(frames.size());

        for (int i = 0; i < frames.size(); i++) {
            final int index = i;
            Bitmap frame = frames.get(index);
            GeneratePresignedUrlsResponse.PresignedUrlInfo urlInfo = presignedUrls.get(index);

            // Convert Bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            frame.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            // Upload to S3 using the pre-signed URL
            uploadImageToS3(urlInfo.getPresignedUrl(), imageData, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    mainHandler.post(() -> {
                        Toast.makeText(context, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    int remaining = remainingUploads.decrementAndGet();
                    if (remaining == 0) {
                        // All uploads completed
                        callback.onUploadCompleted(photoUrls);
                    }
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        mainHandler.post(() -> {
                            Toast.makeText(context, "Failed to upload image: " + response.message(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // Collect successfully uploaded image URL
                        photoUrls.add(urlInfo.getPhotoUrl());
                    }

                    int remaining = remainingUploads.decrementAndGet();
                    if (remaining == 0) {
                        // All uploads completed
                        callback.onUploadCompleted(photoUrls);
                    }
                }
            });
        }
    }

    private void uploadImageToS3(String presignedUrl, byte[] imageData, okhttp3.Callback callback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageData);

        Request request = new Request.Builder()
                .url(presignedUrl)
                .put(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(callback);
    }

    public interface UploadCallback {
        void onUploadCompleted(List<String> photoUrls);
        void onUploadFailed();
    }
}