package com.example.splitmate_delta.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitmate_delta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button mBtnLogin;
    private EditText mEtUser;
    private EditText mEtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initializes
        mAuth = FirebaseAuth.getInstance();
        mBtnLogin = findViewById(R.id.btn_login);
        mEtUser = findViewById(R.id.et_1);
        mEtPassword = findViewById(R.id.et_2);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginClick(v);
            }
        });
    }

    // login logic to use Firebase Authentication
    private void onLoginClick(View v) {
        String email = mEtUser.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "The username and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        loginUser(email, password);
    }

    // Firebase login method
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userRole = getUserRole(user.getEmail());
                            Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
                            intent.putExtra("user_role", userRole);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "login failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Mock function to determine user role based on email
    private String getUserRole(String email) {
        // Example logic: use email to determine role
        if (email.equals("landlord@example.com")) {
            return "landlord";
        } else {
            return "tenant";
        }
    }
}