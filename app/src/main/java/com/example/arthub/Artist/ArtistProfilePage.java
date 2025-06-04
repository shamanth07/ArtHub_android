package com.example.arthub.Artist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.arthub.Auth.ChangePassword;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;
import java.util.Map;

public class ArtistProfilePage extends AppCompatActivity {

    TextView changepassword, ArtistUsername;
    EditText emailid, bioid, websiteLink, instagramLink;
    ImageView backbtn, artistprofilepicture;
    Button editprofile, saveprofile;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri = null;

    private String uid;
    private DatabaseReference artistRef;
    private StorageReference storageRef;

    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_artist_profile_page);

        changepassword = findViewById(R.id.changepassword);
        backbtn = findViewById(R.id.backbtn);
        ArtistUsername = findViewById(R.id.ArtistUsername);
        emailid = findViewById(R.id.emailid);
        bioid = findViewById(R.id.bioid);
        websiteLink = findViewById(R.id.websiteLink);
        instagramLink = findViewById(R.id.instagramLink);
        editprofile = findViewById(R.id.editprofile);
        saveprofile = findViewById(R.id.saveprofile);
        artistprofilepicture = findViewById(R.id.artistprofilepicture);

        // Disable editing initially
        enableEditing(false);
        saveprofile.setVisibility(View.GONE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        uid = user.getUid();
        String email = user.getEmail();
        String username = (email != null) ? email.split("@")[0] : "Artist";

        ArtistUsername.setText(username + " (Artist)");
        emailid.setText(email);

        artistRef = FirebaseDatabase.getInstance().getReference("artists").child(uid);
        storageRef = FirebaseStorage.getInstance().getReference("artists_profile_pictures/" + uid + ".jpg");

        loadArtistProfile();

        artistprofilepicture.setOnClickListener(v -> {
            if (isEditing) openImagePicker();
        });

        editprofile.setOnClickListener(v -> {
            enableEditing(true);
            saveprofile.setVisibility(View.VISIBLE);
        });

        saveprofile.setOnClickListener(v -> saveProfile());

        backbtn.setOnClickListener(v ->
                startActivity(new Intent(this, ArtistAccountPage.class)));

        changepassword.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePassword.class)));
    }

    private void enableEditing(boolean enable) {
        isEditing = enable;
        bioid.setFocusable(enable);
        bioid.setFocusableInTouchMode(enable);

        websiteLink.setFocusable(enable);
        websiteLink.setFocusableInTouchMode(enable);

        instagramLink.setFocusable(enable);
        instagramLink.setFocusableInTouchMode(enable);

        artistprofilepicture.setEnabled(enable);
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            artistprofilepicture.setImageURI(imageUri);
        }
    }

    private void loadArtistProfile() {
        artistRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                bioid.setText(snapshot.child("bio").getValue(String.class));
                websiteLink.setText(snapshot.child("socialLinks").child("website").getValue(String.class));
                instagramLink.setText(snapshot.child("socialLinks").child("instagram").getValue(String.class));
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(artistprofilepicture);
                }
            }
        });
    }

    private void saveProfile() {
        String bio = bioid.getText().toString().trim();
        String website = websiteLink.getText().toString().trim();
        String instagram = instagramLink.getText().toString().trim();
        String name = emailid.getText().toString().split("@")[0];
        String email = emailid.getText().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", "email : " + email);
        updates.put("bio", bio);
        updates.put("socialLinks/website", website);
        updates.put("socialLinks/instagram", instagram);

        artistRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            if (imageUri != null) {
                uploadProfilePicture();
            } else {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                enableEditing(false);
                saveprofile.setVisibility(View.GONE);
            }
        });
    }

    private void uploadProfilePicture() {
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    artistRef.child("profileImageUrl").setValue(uri.toString())
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Profile picture uploaded", Toast.LENGTH_SHORT).show();
                                enableEditing(false);
                                saveprofile.setVisibility(View.GONE);
                            });
                })).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show());
    }
}
