package com.example.arthub.Auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ChangePassword extends AppCompatActivity {

    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private Button submitButton;
    private ImageView backBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();


        currentPasswordEditText = findViewById(R.id.currentpassowrdedittext);
        newPasswordEditText = findViewById(R.id.newpasswordedittext);
        confirmPasswordEditText = findViewById(R.id.confirmpasswordedittext);
        submitButton = findViewById(R.id.submitbtn);
        backBtn = findViewById(R.id.backbtn);


        backBtn.setOnClickListener(v -> finish());


        submitButton.setOnClickListener(view -> {
            String currentPass = currentPasswordEditText.getText().toString().trim();
            String newPass = newPasswordEditText.getText().toString().trim();
            String confirmPass = confirmPasswordEditText.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty() || currentPass.isEmpty()) {
                Toast.makeText(ChangePassword.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(ChangePassword.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null && user.getEmail() != null) {
                // Re-authenticate the user first
                mAuth.signInWithEmailAndPassword(user.getEmail(), currentPass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPass)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                Toast.makeText(ChangePassword.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(ChangePassword.this, "Password update failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(ChangePassword.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}