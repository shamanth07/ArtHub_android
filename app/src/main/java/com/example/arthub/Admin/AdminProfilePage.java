package com.example.arthub.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.arthub.Auth.ChangePassword;
import com.example.arthub.R;
import com.example.arthub.Visitor.VisitorAccountPage;
import com.example.arthub.Visitor.VisitorProfilePage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminProfilePage extends AppCompatActivity {


    ImageView backbtn;

    TextView Username,changepassword;

    EditText emailid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_profile_page);

        backbtn = findViewById(R.id.backbtn);
        Username = findViewById(R.id.Username);
        changepassword = findViewById(R.id.changepassword);
        emailid = findViewById(R.id.emailid);



        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProfilePage.this, AdminAccountPage.class);
            startActivity(intent);
        });
        changepassword.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProfilePage.this, ChangePassword.class);
            startActivity(intent);
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            emailid.setText(email);
            String username = null;
            if (email != null) {
                username = email.split("@")[0];
                Username.setText(username + "(Admin)");
            }


        }



    }
}