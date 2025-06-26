package com.example.arthub.Visitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arthub.Auth.SignIn;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VisitorAccountPage extends AppCompatActivity {

    private ImageView backbtn;
    private Button btnLogout;
    private TextView visitorName, visitorprofile, favartworks, visitorbookinghistory, settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_visitor_account_page);


        backbtn = findViewById(R.id.backbtn);
        btnLogout = findViewById(R.id.btnLogout);
        visitorName = findViewById(R.id.visitorName);
        visitorprofile = findViewById(R.id.visitorprofile);
        favartworks = findViewById(R.id.favartworks);
        visitorbookinghistory = findViewById(R.id.visitorbookinghistory);
        settings = findViewById(R.id.settings);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String username = (email != null) ? email.split("@")[0] : "Visitor";
            visitorName.setText(username + " (visitor)");
        }


        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(VisitorAccountPage.this, SignIn.class));
            finish();
        });


        visitorprofile.setOnClickListener(v -> {
            startActivity(new Intent(VisitorAccountPage.this, VisitorProfilePage.class));
        });


        backbtn.setOnClickListener(v -> {
            startActivity(new Intent(VisitorAccountPage.this, VisitorDashboard.class));
        });


        favartworks.setOnClickListener(v -> {
            startActivity(new Intent(VisitorAccountPage.this, VisitorFavouritesArtworks.class));
        });


        visitorbookinghistory.setOnClickListener(v -> {
            startActivity(new Intent(VisitorAccountPage.this, VisitorBookingHistory.class));
        });


        settings.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userId = (currentUser != null) ? currentUser.getUid() : "";

            Intent intent = new Intent(VisitorAccountPage.this, Settings.class);
            intent.putExtra("role", "visitor");
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }
}
