package com.example.arthub.Artist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
public class ArtistAccountPage extends AppCompatActivity implements View.OnClickListener {

    Button btnLogout;
    ImageView backbtn;
    TextView artistName, artistprofile, applyforevent, artiststatus,favartworks,settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_artist_account_page);

        btnLogout = findViewById(R.id.btnLogout);
        backbtn = findViewById(R.id.backbtn);
        artistName = findViewById(R.id.artistName);
        artistprofile = findViewById(R.id.artistprofile);
        applyforevent = findViewById(R.id.applyforevent);
        artiststatus = findViewById(R.id.artiststatus);
        favartworks = findViewById(R.id.favartworks);
        settings = findViewById(R.id.settings);


        btnLogout.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        applyforevent.setOnClickListener(this);
        artiststatus.setOnClickListener(this);
        artistprofile.setOnClickListener(this);
        favartworks.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnLogout) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.backbtn) {
            Intent intent = new Intent(this, ArtistDashboard.class);
            startActivity(intent);
        } else if (id == R.id.applyforevent) {
            Intent intent = new Intent(this, ArtistApplyForEvent.class);
            startActivity(intent);
        } else if (id == R.id.artiststatus) {
            Intent intent = new Intent(this, ArtistStatusPage.class);
            startActivity(intent);
        } else if (id == R.id.artistprofile) {
            Intent intent = new Intent(this, ArtistProfilePage.class);
            startActivity(intent);
        } else if (id == R.id.settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }
    }
}