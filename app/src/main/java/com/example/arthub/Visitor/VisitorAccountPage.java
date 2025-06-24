package com.example.arthub.Visitor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.arthub.Artist.ArtistAccountPage;
import com.example.arthub.Auth.SignIn;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VisitorAccountPage extends AppCompatActivity {

    ImageView backbtn;
    Button btnLogout;
    TextView visitorName,visitorprofile,visitorbookinghistory,settings,favartworks;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_visitor_account_page);



        backbtn = findViewById(R.id.backbtn);
        btnLogout = findViewById(R.id.btnLogout);
        visitorName = findViewById(R.id.visitorName);
        visitorprofile = findViewById(R.id.visitorprofile);
        visitorbookinghistory = findViewById(R.id.visitorbookinghistory);
        settings = findViewById(R.id.settings);
        favartworks = findViewById(R.id.favartworks);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String username = null;
            if (email != null) {
                username = email.split("@")[0];

            }
            visitorName.setText(username + "(visitor)");
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(VisitorAccountPage.this, SignIn.class);
            startActivity(intent);
            finish();
        });
        visitorprofile.setOnClickListener(v -> {
            Intent intent = new Intent(VisitorAccountPage.this, VisitorProfilePage.class);
            startActivity(intent);

        });
        visitorbookinghistory.setOnClickListener(v -> {
            Intent intent = new Intent(VisitorAccountPage.this, VisitorBookingHistory.class);
            startActivity(intent);

        });
        favartworks.setOnClickListener(v -> {
            Intent intent = new Intent(VisitorAccountPage.this, VisitorFavourtsArtist.class);
            startActivity(intent);

        });
        settings.setOnClickListener(v -> {
            Intent intent = new Intent(VisitorAccountPage.this, Settings.class);
            startActivity(intent);

        });


        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(VisitorAccountPage.this, VisitorDashboard.class);
            startActivity(intent);

        });





    }
}