package com.example.arthub.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.arthub.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class EditEvent extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText titleInput, descriptionInput, timeInput, maxArtistsInput;
    private DatePicker datePicker;
    private ImageView bannerImage;
    private Button actionButton;

    private Uri selectedImageUri;
    private Event existingEvent;

    private StorageReference storageRef;
    private DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event); // Reuse same layout

        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        timeInput = findViewById(R.id.timeInput);
        maxArtistsInput = findViewById(R.id.maxArtistsInput);
        datePicker = findViewById(R.id.datePicker);
        bannerImage = findViewById(R.id.uploadImage);
        actionButton = findViewById(R.id.createButton); // Same ID, change text

        storageRef = FirebaseStorage.getInstance().getReference("event_banners");
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        // Disable past dates
        Calendar today = Calendar.getInstance();
        datePicker.setMinDate(today.getTimeInMillis());

        // Check if event is passed
        if (getIntent().hasExtra("event")) {
            existingEvent = (Event) getIntent().getSerializableExtra("event");
            populateEventData(existingEvent);
        }

        bannerImage.setOnClickListener(v -> openImagePicker());

        actionButton.setOnClickListener(v -> {
            if (existingEvent != null) {
                updateEvent();
            }
        });
    }

    private void populateEventData(Event event) {
        actionButton.setText("Save Changes");

        titleInput.setText(event.getTitle());
        descriptionInput.setText(event.getDescription());
        timeInput.setText(event.getTime());
        maxArtistsInput.setText(String.valueOf(event.getMaxArtists()));

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(event.getEventDate());
        datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        Glide.with(this).load(event.getBannerImageUrl()).into(bannerImage);
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

    private void updateEvent() {
        String eventId = existingEvent.getEventId();
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        int maxArtists = Integer.parseInt(maxArtistsInput.getText().toString().trim());

        Calendar cal = Calendar.getInstance();
        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        long dateMillis = cal.getTimeInMillis();

        if (selectedImageUri != null) {
            // Upload new image
            StorageReference imageRef = storageRef.child("banner_" + eventId + ".jpg");
            imageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveUpdatedEvent(eventId, title, description, dateMillis, time, maxArtists, uri.toString());
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        } else {
            // Use existing banner URL
            saveUpdatedEvent(eventId, title, description, dateMillis, time, maxArtists, existingEvent.getBannerImageUrl());
        }
    }

    private void saveUpdatedEvent(String eventId, String title, String description, long date, String time, int maxArtists, String bannerUrl) {
        Event updatedEvent = new Event(eventId, title, description, date, time, maxArtists, bannerUrl);

        eventsRef.child(eventId).setValue(updatedEvent)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, AdminDashboard.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update event: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
