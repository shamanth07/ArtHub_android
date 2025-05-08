package com.example.arthub.Auth;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arthub.R;

public class SignUp extends AppCompatActivity {

    Spinner roleSpinner;
    EditText emailInput, passwordInput;
    CheckBox termsCheckbox;
    Button signupButton;
    TextView signInLink;

    String[] roles = {"Visitor", "Artist"};

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

        signupButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            String role = roleSpinner.getSelectedItem().toString();

            if (!termsCheckbox.isChecked()) {
                Toast.makeText(this, "Please agree to Terms & Privacy Policy", Toast.LENGTH_SHORT).show();
                return;
            }


            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }


            Toast.makeText(this, "Signing up as " + role, Toast.LENGTH_SHORT).show();
        });

        signInLink.setOnClickListener(view -> {

            Toast.makeText(this, "Go to Sign In", Toast.LENGTH_SHORT).show();
        });
    }

    public static class SplashScreen extends AppCompatActivity {




        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_splash_screen);





            new
                    Handler().postDelayed(() -> {
                Intent intent = new Intent(SplashScreen.this, SignUp.class);
                startActivity(intent);
                finish();
            }, 3000);
        }

    }
}
