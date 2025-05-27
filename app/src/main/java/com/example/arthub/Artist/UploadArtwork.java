package com.example.arthub.Artist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.UUID;

public class UploadArtwork extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 101;

    private ImageView artworkImageView, backbtn;
    private Button uploadArtworkButton;
    private EditText titleEditText, descriptionEditText, categoryEditText, yearEditText, priceEditText;

    private Uri imageUri;

    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_artwork);

        artworkImageView = findViewById(R.id.artworkImageView);
        uploadArtworkButton = findViewById(R.id.uploadArtworkButton);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        categoryEditText = findViewById(R.id.categoryEditText);
        yearEditText = findViewById(R.id.yearEditText);
        priceEditText = findViewById(R.id.priceEditText);
        backbtn = findViewById(R.id.backbtn);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        artworkImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Artwork Image"), IMAGE_PICK_CODE);
        });

        uploadArtworkButton.setOnClickListener(v -> uploadArtwork());

        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(UploadArtwork.this, ArtistDashboard.class);
            startActivity(intent);
            finish();
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

    private void uploadArtwork() {
        String title = titleEditText.getText().toString().trim();
        String desc = descriptionEditText.getText().toString().trim();
        String year = yearEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();

        if (imageUri == null || title.isEmpty() || desc.isEmpty() || year.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.setCancelable(false);
        dialog.show();

        String uniqueId = UUID.randomUUID().toString();
        StorageReference imageRef = storage.getReference().child("artwork_images/" + uniqueId + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    long timestamp = System.currentTimeMillis();
                    String artistId = auth.getCurrentUser().getUid();

                    Artwork artwork = new Artwork(
                            uniqueId,
                            title,
                            desc,
                            imageUrl,
                            price,
                            year,
                            artistId,
                            timestamp
                    );

                    database.getReference("artworks").child(uniqueId).setValue(artwork)
                            .addOnSuccessListener(unused -> {
                                dialog.dismiss();
                                Toast.makeText(this, "Artwork uploaded!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, ArtistDashboard.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(this, "Failed to upload artwork: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
