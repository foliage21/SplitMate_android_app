package com.example.splitmate_delta.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.splitmate_delta.R;
import com.example.splitmate_delta.api.ApiClient;
import com.example.splitmate_delta.api.BackendApiService;
import com.example.splitmate_delta.models.User;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private TextView mTvUsername;
    private TextView mTvEmail;
    private ImageView mIvProfileImage;
    private Button mBtnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        mTvUsername = view.findViewById(R.id.tv_username);
        mTvEmail = view.findViewById(R.id.tv_email);
        mIvProfileImage = view.findViewById(R.id.iv_profile_image);
        mBtnLogout = view.findViewById(R.id.btn_logout);

        // Get the user ID from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            BackendApiService apiService = ApiClient.getApiService();
            Call<User> call = apiService.getUserById(userId);

            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        mTvUsername.setText(user.getName());
                        mTvEmail.setText(user.getEmail());

                        String imageUrl = user.getImage();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(mIvProfileImage);
                        } else {
                            mIvProfileImage.setImageResource(R.drawable.default_profile_image);
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load user data. Procedure", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(getContext(), "error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "The user ID is invalid. Please log in again", Toast.LENGTH_SHORT).show();
        }

        // Set the logout button click event
        mBtnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();  // Clear SharedPreferences
            editor.apply();

            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }
}