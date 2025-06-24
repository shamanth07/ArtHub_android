package com.example.arthub.Artist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arthub.Auth.SignIn;
import com.example.arthub.R;
import com.example.arthub.Visitor.Settings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ArtistAccountPage extends AppCompatActivity implements View.OnClickListener {

    Button btnLogout;
    ImageView backbtn;
    TextView artistName, artistprofile, applyforevent, artiststatus, settings;

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
        settings = findViewById(R.id.settings);

        btnLogout.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        applyforevent.setOnClickListener(this);
        artiststatus.setOnClickListener(this);
        artistprofile.setOnClickListener(this);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String username = null;
            if (email != null) {
                username = email.split("@")[0];
            }
            artistName.setText(username + "(artist)");


            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userRole = snapshot.child("role").getValue(String.class);

                        settings.setOnClickListener(view -> {
                            Intent intent = new Intent(ArtistAccountPage.this, Settings.class);
                            intent.putExtra("role", userRole);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ArtistAccountPage.this, "Error loading user info", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnLogout) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignIn.class));
            finish();
        } else if (id == R.id.backbtn) {
            startActivity(new Intent(this, ArtistDashboard.class));
        } else if (id == R.id.applyforevent) {
            startActivity(new Intent(this, ArtistApplyForEvent.class));
        } else if (id == R.id.artiststatus) {
            startActivity(new Intent(this, ArtistStatusPage.class));
        } else if (id == R.id.artistprofile) {
            startActivity(new Intent(this, ArtistProfilePage.class));
        }
    }
}
