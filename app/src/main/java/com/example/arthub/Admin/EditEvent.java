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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.arthub.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.Calendar;

public class EditEvent extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText titleInput, descriptionInput, timeInput, maxArtistsInput, priceid;
    private DatePicker datePicker;
    private ImageView bannerImage;
    private Button actionButton;

    private Uri selectedImageUri;
    private Event existingEvent;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private String selectedEventLocation;

    private StorageReference storageRef;
    private DatabaseReference eventsRef;

    private GoogleMap map;
    private Marker locationMarker;

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
        priceid = findViewById(R.id.priceid);
        actionButton = findViewById(R.id.createButton);

        storageRef = FirebaseStorage.getInstance().getReference("event_banners");
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        Calendar today = Calendar.getInstance();
        datePicker.setMinDate(today.getTimeInMillis());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCz39FE9RCbShlRfF2wZuw08JqfT-hZVvA");
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        latitude = latLng.latitude;
                        longitude = latLng.longitude;
                        selectedEventLocation = place.getAddress(); // ✅ Full location

                        if (map != null) {
                            map.clear();
                            locationMarker = map.addMarker(new MarkerOptions().position(latLng).title("Event Location"));
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                        }
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(EditEvent.this, "Location search failed: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

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
        priceid.setText(String.valueOf(event.getticketPrice()));

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(event.getEventDate());
        datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        Glide.with(this).load(event.getBannerImageUrl()).into(bannerImage);

        selectedEventLocation = event.getLocation();
        latitude = event.getLatitude();
        longitude = event.getLongitude();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng initialLatLng = new LatLng(latitude, longitude);
        locationMarker = map.addMarker(new MarkerOptions().position(initialLatLng).title("Event Location"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 15));

        map.setOnMapClickListener(latLng -> {
            latitude = latLng.latitude;
            longitude = latLng.longitude;
            if (locationMarker != null) {
                locationMarker.setPosition(latLng);
            } else {
                locationMarker = map.addMarker(new MarkerOptions().position(latLng).title("Event Location"));
            }
        });
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
        double price = Double.parseDouble(priceid.getText().toString().trim());

        Calendar cal = Calendar.getInstance();
        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        long dateMillis = cal.getTimeInMillis();

        if (selectedImageUri != null) {
            StorageReference imageRef = storageRef.child("banner_" + eventId + ".jpg");
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                saveUpdatedEvent(eventId, title, description, dateMillis, time, maxArtists,
                                        uri.toString(), selectedEventLocation, latitude, longitude, price);
                            })
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        } else {
            saveUpdatedEvent(eventId, title, description, dateMillis, time, maxArtists,
                    existingEvent.getBannerImageUrl(), selectedEventLocation, latitude, longitude, price);
        }
    }

    private void saveUpdatedEvent(String eventId, String title, String description, long date, String time,
                                  int maxArtists, String bannerUrl, String location,
                                  double latitude, double longitude, double ticketPrice) {
        Event updatedEvent = new Event(eventId, title, description, date, time, maxArtists, bannerUrl,
                location, latitude, longitude, ticketPrice);

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
