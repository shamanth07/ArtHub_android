package com.example.arthub.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.arthub.Auth.SignIn;
import com.example.arthub.R;
import com.example.arthub.Visitor.Settings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class AdminAccountPage extends AppCompatActivity {


    Button btnLogout;

    ImageView backbtn;

    TextView adminName,adminprofilebtn,manageinvitations,createevent,settings,reports;

    String role = "admin";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_account_page);


        btnLogout = findViewById(R.id.btnLogout);
        backbtn = findViewById(R.id.backbtn);

        adminName = findViewById(R.id.adminName);
        adminprofilebtn = findViewById(R.id.adminprofilebtn);
        manageinvitations = findViewById(R.id.manageinvitations);
        createevent = findViewById(R.id.createevent);
        settings = findViewById(R.id.settings);
        reports = findViewById(R.id.reports);




        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(AdminAccountPage.this, "Logged out", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AdminAccountPage.this, SignIn.class);
            startActivity(intent);
            finish();
        });

        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(AdminAccountPage.this, AdminDashboard.class);
            startActivity(intent);
            finish();

        });
        reports.setOnClickListener(v -> {
            Intent intent = new Intent(AdminAccountPage.this, AdminReportsActivity.class);
            startActivity(intent);

        });
        adminprofilebtn.setOnClickListener(v -> {
            Intent intent = new Intent(AdminAccountPage.this, AdminProfilePage.class);
            startActivity(intent);

        });
        createevent.setOnClickListener(v -> {
            Intent intent = new Intent(AdminAccountPage.this, CreateEvent.class);
            startActivity(intent);

        });
        manageinvitations.setOnClickListener(v -> {
            Intent intent = new Intent(AdminAccountPage.this,AdminManageInvitations.class);
            startActivity(intent);

        });
        settings.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String currentUserId = user.getUid();
               Intent intent = new Intent(AdminAccountPage.this, Settings.class);
                intent.putExtra("userId", currentUserId);
                intent.putExtra("role", role);
                startActivity(intent);
            } else {

            }

        });



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String username = null;
            if (email != null) {
                username = email.split("@")[0];
            }
            adminName.setText(username + "(Admin)");
        }

    }



}
