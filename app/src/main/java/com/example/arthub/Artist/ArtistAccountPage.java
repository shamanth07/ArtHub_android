package com.example.arthub.Artist;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ArtistAccountPage extends AppCompatActivity {

    Button btnLogout;

    ImageView backbtn;

    TextView artistName,artistprofile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_artist_account_page);

        btnLogout = findViewById(R.id.btnLogout);
        backbtn = findViewById(R.id.backbtn);
        artistName = findViewById(R.id.artistName);
        artistprofile = findViewById(R.id.artistprofile);


        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ArtistAccountPage.this, SignIn.class);
            startActivity(intent);
            finish();
        });

        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistAccountPage.this, ArtistDashboard.class);
            startActivity(intent);
            finish();
        });

        artistprofile.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistAccountPage.this, ArtistProfilePage.class);
            startActivity(intent);
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String username = null;
            if (email != null) {
                username = email.split("@")[0];
            }
            artistName.setText(username + "(artist)");
        }







    }
}