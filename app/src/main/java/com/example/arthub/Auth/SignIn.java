package com.example.arthub.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arthub.Admin.AdminDashboard;
import com.example.arthub.Admin.CreateEvent;
import com.example.arthub.Artist.ArtistDashboard;
import com.example.arthub.R;
import com.example.arthub.Visitor.VisitorDashboard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class SignIn extends AppCompatActivity {

    Spinner roleSpinner;
    EditText emailInput, passwordInput;
    Button signInButton;
    TextView signUpLink,forgetpassword;

    String[] roles = {"Visitor", "Artist", "Admin"};

    FirebaseAuth mAuth;
    DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        roleSpinner = findViewById(R.id.roleSpinner);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signInButton = findViewById(R.id.signInButton);
        signUpLink = findViewById(R.id.signUpLink);
        forgetpassword = findViewById(R.id.forgetpassword);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = roles[position];
                signUpLink.setVisibility(selectedRole.equals("Admin") ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        forgetpassword.setOnClickListener(v -> {
            startActivity(new Intent(SignIn.this, ForgetPassword.class));
        });

        signInButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();


                                databaseRef.child("admin").child(uid).get().addOnCompleteListener(adminTask -> {
                                    if (adminTask.isSuccessful() && adminTask.getResult().exists()) {
                                        Toast.makeText(this, "Logged in as Admin", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, CreateEvent.class));
                                        finish();
                                    } else {

                                        databaseRef.child("users").child(uid).child("role").get().addOnCompleteListener(userTask -> {
                                            if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                                String roleInDb = userTask.getResult().getValue(String.class);
                                                if (roleInDb != null) {
                                                    Toast.makeText(this, "Logged in as " + roleInDb, Toast.LENGTH_SHORT).show();
                                                    navigateToRolePage(roleInDb);
                                                } else {
                                                    Toast.makeText(this, "Role not found in database.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });



        signUpLink.setOnClickListener(view -> {
            startActivity(new Intent(SignIn.this, SignUp.class));
        });
    }

    private void navigateToRolePage(String role) {
        switch (role) {
            case "Visitor":
                Intent intent = new Intent(this, VisitorDashboard.class);
                startActivity(intent);
                finish();
                break;
            case "Artist":

                Toast.makeText(this, "Navigating to Artist Home Page...", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, ArtistDashboard.class);
                startActivity(intent);
                finish();
                break;
            default:
                Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                break;
        }

    }
}
