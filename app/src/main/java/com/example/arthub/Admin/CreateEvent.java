package com.example.arthub.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.arthub.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.Calendar;
public class CreateEvent extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText titleInput, descriptionInput, timeInput, maxArtistsInput,priceid;

    private DatePicker datePicker;
    private ImageView bannerImage;
    private Uri selectedImageUri;
    private StorageReference storageRef;
    private DatabaseReference eventsRef;

    private SupportMapFragment mapFragment;

    private String selectedEventLocation;




    private LatLng selectedLatLng;
    private AutocompleteSupportFragment autocompleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        timeInput = findViewById(R.id.timeInput);
        maxArtistsInput = findViewById(R.id.maxArtistsInput);
        datePicker = findViewById(R.id.datePicker);
        bannerImage = findViewById(R.id.uploadImage);
        priceid =  findViewById(R.id.priceid);


        storageRef = FirebaseStorage.getInstance().getReference("event_banners");
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        Calendar today = Calendar.getInstance();
        datePicker.setMinDate(today.getTimeInMillis());

        bannerImage.setOnClickListener(v -> openImagePicker());

        // Initialize Places
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCz39FE9RCbShlRfF2wZuw08JqfT-hZVvA");
        }

        // Initialize map fragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);


        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                selectedLatLng = place.getLatLng();
                selectedEventLocation = place.getName();

                if (mMap != null) {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Selected Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(CreateEvent.this, "Place selection failed: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.createButton).setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select a banner image", Toast.LENGTH_SHORT).show();
            } else if (selectedLatLng == null) {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            } else {
                uploadBannerImageAndSaveEvent();
            }
        });
        deleteExpiredEvents();
    }

    private GoogleMap mMap;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
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
    private void deleteExpiredEvents() {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
        long today = Calendar.getInstance().getTimeInMillis();

        eventsRef.get().addOnSuccessListener(dataSnapshot -> {
            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                Long eventDate = eventSnapshot.child("eventDate").getValue(Long.class);
                if (eventDate != null && eventDate < today) {
                    eventSnapshot.getRef().removeValue()
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(CreateEvent.this, "Deleted expired event: " + eventSnapshot.getKey(), Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(CreateEvent.this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to fetch events for cleanup: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void saveEventToDatabase(String eventId, String bannerImageUrl) {




        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        int maxArtists = Integer.parseInt(maxArtistsInput.getText().toString().trim());
        String locationName = selectedEventLocation;




        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        long eventDate = calendar.getTimeInMillis();

        double latitude = selectedLatLng.latitude;
        double longitude = selectedLatLng.longitude;
        String priceString = priceid.getText().toString().trim();

        double price;
        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
            return;
        }

        if (price < 0) {
            Toast.makeText(this, "Price cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty() || description.isEmpty() || time.isEmpty() || maxArtistsInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }


        Event event = new Event(eventId, title, description, eventDate, time, maxArtists, bannerImageUrl,locationName,latitude, longitude,price);

        eventsRef.child(eventId).setValue(event)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event created!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateEvent.this, AdminDashboard.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save event: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
