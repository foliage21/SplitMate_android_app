package com.example.splitmate_delta.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.splitmate_delta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class AccountFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView mTvUsername;
    private TextView mTvEmail;
    private ImageView mIvProfileImage;
    private Button mBtnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Initialize
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser user = mAuth.getCurrentUser();

        mTvUsername = view.findViewById(R.id.tv_username);
        mTvEmail = view.findViewById(R.id.tv_email);
        mIvProfileImage = view.findViewById(R.id.iv_profile_image);
        mBtnLogout = view.findViewById(R.id.btn_logout);

        if (user != null) {
            // Set email directly from FirebaseUser
            mTvEmail.setText(user.getEmail());

            // Retrieve username and profile image from Firebase Realtime Database
            String userId = user.getUid();
            mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                        mTvUsername.setText(username != null ? username : "No username");

                        if (profileImageUrl != null) {
                            Picasso.get().load(profileImageUrl).into(mIvProfileImage);
                        } else {
                            mIvProfileImage.setImageResource(R.drawable.default_profile_image); // Set a default image if no profile image is available
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors.
                }
            });
        }

        // Set logout button click listener
        mBtnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            getActivity().finish();  // Close current activity
        });

        return view;
    }
}