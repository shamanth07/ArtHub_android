package com.example.arthub.Artist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ArtistDashboard extends AppCompatActivity {




    Button upldartwork;

     TextView artistname;

     ImageView menuIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_artist_dashboard);




        upldartwork = findViewById(R.id.upldartwork);
        artistname = findViewById(R.id.artistname);
        menuIcon = findViewById(R.id.menuIcon);

        upldartwork.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistDashboard.this, UploadArtwork.class);
            startActivity(intent);
            finish();
        });
        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistDashboard.this,ArtistAccountPage.class);
            startActivity(intent);
            finish();
        });





          FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

          if(user != null){
              String email = user.getEmail();
              artistname.setText(email);

          }else {
              artistname.setText("no artist logged in");

          }
    }

}