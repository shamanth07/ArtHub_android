package com.example.arthub.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.arthub.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class CreateEvent extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText titleInput, descriptionInput, timeInput, maxArtistsInput;
    private DatePicker datePicker;
    private ImageView bannerImage;
    private Uri selectedImageUri;

    private StorageReference storageRef;
    private DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);


        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        timeInput = findViewById(R.id.timeInput);
        maxArtistsInput = findViewById(R.id.maxArtistsInput);

        bannerImage = findViewById(R.id.uploadImage);


        storageRef = FirebaseStorage.getInstance().getReference("event_banners");
        eventsRef = FirebaseDatabase.getInstance().getReference("events");


        bannerImage.setOnClickListener(v -> openImagePicker());

        datePicker = findViewById(R.id.datePicker);

        Calendar today = Calendar.getInstance();
        datePicker.setMinDate(today.getTimeInMillis());



        findViewById(R.id.createButton).setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadBannerImageAndSaveEvent();
            } else {
                Toast.makeText(this, "Please select a banner image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            bannerImage.setImageURI(selectedImageUri);
        }
    }

    private void uploadBannerImageAndSaveEvent() {
        String eventId = eventsRef.push().getKey();
        if (eventId == null) return;

        StorageReference imageRef = storageRef.child("banner_" + eventId + ".jpg");
        imageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                        saveEventToDatabase(eventId, uri.toString())
                )
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void saveEventToDatabase(String eventId, String bannerImageUrl) {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        int maxArtists = Integer.parseInt(maxArtistsInput.getText().toString().trim());

        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        long eventDate = calendar.getTimeInMillis();

        Event event = new Event(eventId, title, description, eventDate, time, maxArtists, bannerImageUrl);

        eventsRef.child(eventId).setValue(event)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event created!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateEvent.this, AdminDashboard.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save event: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
