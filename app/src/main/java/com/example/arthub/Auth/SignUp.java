package com.example.arthub.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class SignUp extends AppCompatActivity {

    Spinner roleSpinner;
    EditText emailInput, passwordInput;
    CheckBox termsCheckbox;
    Button signupButton;
    TextView signInLink;

    String[] roles = {"Visitor", "Artist"};

    FirebaseAuth mAuth;
    DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        roleSpinner = findViewById(R.id.roleSpinner);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        signupButton = findViewById(R.id.signupButton);
        signInLink = findViewById(R.id.SignInlink);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        signupButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

            if (!termsCheckbox.isChecked()) {
                Toast.makeText(this, "Please agree to Terms & Privacy Policy", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }


            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkAndSaveUser(user.getUid(), email,password, role);
                            }
                        } else {
                            Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        signInLink.setOnClickListener(view -> {
            Toast.makeText(this, "Go to Sign In", Toast.LENGTH_SHORT).show();
               Intent intent = new Intent(SignUp.this,SignIn.class);
               startActivity(intent);
               finish();
        });
    }

    private void checkAndSaveUser(String uid, String email, String password, String role) {
        DatabaseReference adminRef = databaseRef.child("admin");

        adminRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (!snapshot.exists()) {
                    // No admin yet, this is the first user → Save as admin
                    adminRef.child(uid).setValue(new User(email, password, "admin"))
                            .addOnCompleteListener(adminTask -> {
                                if (adminTask.isSuccessful()) {
                                    Toast.makeText(this, "Signed up as Admin", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed to save admin info", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {

                    databaseRef.child("users").child(uid).setValue(new User(email, password, role))
                            .addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()) {
                                    Toast.makeText(this, "Signed up as " + role, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this,SignIn.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(this, "Failed to save user info", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                Toast.makeText(this, "Error checking admin: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




}
