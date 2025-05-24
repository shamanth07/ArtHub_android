package com.example.arthub.Artist;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ArtistProfilePage extends AppCompatActivity {


    TextView changepassword,ArtistUsername;

    EditText emailid;

    ImageView backbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_artist_profile_page);


        changepassword = findViewById(R.id.changepassword);
        backbtn = findViewById(R.id.backbtn);
        ArtistUsername = findViewById(R.id.ArtistUsername);
        emailid = findViewById(R.id.emailid);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String email = user.getEmail();
            emailid.setText(email);
            if(email != null){
                String username = null;
              username = email.split("@")[0];
              ArtistUsername.setText(username + "(Artist)");

            }
        }

        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistProfilePage.this, ArtistAccountPage.class);
            startActivity(intent);
        });




        changepassword.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistProfilePage.this, ChangePassword.class);
            startActivity(intent);
        });

    }
}