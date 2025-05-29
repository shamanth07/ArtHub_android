package com.example.arthub.Artist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;

public class EditArtworkPage extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 101;

    ImageView artworkImageView, backbtn;
    Button saveArtworkButton;
    EditText titleEditText, descriptionEditText, categoryEditText, yearEditText, priceEditText;

    Uri imageUri;
    String existingImageUrl, artworkId;

    FirebaseStorage storage;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_artwork);

        artworkImageView = findViewById(R.id.artworkImageView);
        saveArtworkButton = findViewById(R.id.uploadArtworkButton);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        categoryEditText = findViewById(R.id.categoryEditText);
        yearEditText = findViewById(R.id.yearEditText);
        priceEditText = findViewById(R.id.priceEditText);
        backbtn = findViewById(R.id.backbtn);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();


        saveArtworkButton.setText("Save");


        artworkId = getIntent().getStringExtra("artworkId");

        // fetch  existing artwork data from db
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && artworkId != null) {
            String artistId = user.getUid();

            DatabaseReference artworkRef = FirebaseDatabase.getInstance()
                    .getReference("artists")
                    .child(artistId)
                    .child("artworks")
                    .child(artworkId);

            artworkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        titleEditText.setText(snapshot.child("title").getValue(String.class));
                        descriptionEditText.setText(snapshot.child("description").getValue(String.class));
                        categoryEditText.setText(snapshot.child("category").getValue(String.class));
                        yearEditText.setText(snapshot.child("year").getValue(String.class));
                        priceEditText.setText(snapshot.child("price").getValue(String.class));
                        existingImageUrl = snapshot.child("imageUrl").getValue(String.class);

                        // Load image using Glide
                        Glide.with(EditArtworkPage.this)
                                .load(existingImageUrl)
                                .into(artworkImageView);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(EditArtworkPage.this, "Failed to load artwork", Toast.LENGTH_SHORT).show();
                }
            });
        }


        artworkImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Artwork Image"), IMAGE_PICK_CODE);
        });

        saveArtworkButton.setOnClickListener(v -> saveArtwork());

        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(EditArtworkPage.this, ArtistDashboard.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            artworkImageView.setImageURI(imageUri);
        }
    }

    private void saveArtwork() {
        String title = titleEditText.getText().toString().trim();
        String desc = descriptionEditText.getText().toString().trim();
        String catg = categoryEditText.getText().toString().trim();
        String year = yearEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || catg.isEmpty() || year.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Saving...");
        dialog.show();

        if (imageUri != null) {

            StorageReference imageRef = storage.getReference().child("artwork_images/" + artworkId + ".jpg");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveArtworkData(title, desc, catg, year, price, imageUrl, dialog);
                    }))
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveArtworkData(title, desc, catg, year, price, existingImageUrl, dialog);
        }
    }

    private void saveArtworkData(String title, String desc, String catg, String year, String price, String imageUrl, ProgressDialog dialog) {
        String artistId = auth.getCurrentUser().getUid();

        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("title", title);
        updatedData.put("description", desc);
        updatedData.put("category", catg);
        updatedData.put("year", year);
        updatedData.put("price", price);
        updatedData.put("imageUrl", imageUrl);
        updatedData.put("artistId", artistId);
        updatedData.put("id", artworkId);

        // New reference path
        DatabaseReference artworkRef = database.getReference();
                artworkRef.child("artists")
                .child(artistId)
                .child("artworks")
                .child(artworkId)
                        .setValue(updatedData)
                 .addOnFailureListener(e -> {
            Log.e("UpdateArtwork", "Failed to update artist node: " + e.getMessage());
        });

        DatabaseReference artworkref = database.getReference();
        artworkref.child("artworks")
                .child(artworkId)
                .setValue(updatedData)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Artwork updated!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(EditArtworkPage.this, ArtistDashboard.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Log.e("UpdateArtwork", "Failed to update global node: " + e.getMessage());
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
