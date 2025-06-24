package com.example.arthub.Artist;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.arthub.Admin.Event;
import com.example.arthub.Admin.Invitation;
import com.example.arthub.R;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ArtistEventDetailActivity extends AppCompatActivity {

    private TextView titleTextView, dateTextView, descriptionTextView;
    private TextView eventTimeTextView, eventLocationTextView, eventStatusTextView, rsvpcountTextView;
    private ImageView bannerImageView;
    private LinearLayout artistListLayout;

    private DatabaseReference dbRef;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_event_detail);


        initViews();


        dbRef = FirebaseDatabase.getInstance().getReference();


        Event event = (Event) getIntent().getSerializableExtra("event");
        Invitation invitation = (Invitation) getIntent().getSerializableExtra("invitation");

        if (event == null || invitation == null) {
            Toast.makeText(this, "Event or Invitation data missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        populateEventDetails(event, invitation);


        loadAcceptedArtists();
    }

    private void initViews() {
        titleTextView = findViewById(R.id.eventTitleTextView);
        dateTextView = findViewById(R.id.eventDateTextView);
        descriptionTextView = findViewById(R.id.eventDescriptionTextView);
        artistListLayout = findViewById(R.id.artistListLayout);

        bannerImageView = findViewById(R.id.bannerImageView);
        eventTimeTextView = findViewById(R.id.eventTimeTextView);
        eventLocationTextView = findViewById(R.id.eventLocationTextView);
        eventStatusTextView = findViewById(R.id.eventStatusTextView);
        rsvpcountTextView = findViewById(R.id.rsvpcountTextView);
    }

    private void populateEventDetails(Event event, Invitation invitation) {
        eventId = event.getEventId();


        titleTextView.setText(event.getTitle());


        try {
            long eventDateMillis = event.getEventDate();
            String formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(new Date(eventDateMillis));
            dateTextView.setText(formattedDate);
        } catch (Exception e) {
            dateTextView.setText("Date not available");
        }


        descriptionTextView.setText(event.getDescription());


        eventTimeTextView.setText(event.getTime());
        eventLocationTextView.setText(event.getLocation());


        eventStatusTextView.setText("Status: " + invitation.getStatus());
        switch (invitation.getStatus().toLowerCase()) {
            case "accepted":
                eventStatusTextView.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "rejected":
                eventStatusTextView.setTextColor(Color.parseColor("#F44336"));
                break;
            default:
                eventStatusTextView.setTextColor(Color.GRAY);
                break;
        }


        Glide.with(this)
                .load(event.getBannerImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(bannerImageView);


        fetchRSVPCount(event.getTitle());
    }

    private void fetchRSVPCount(String eventTitle) {
        DatabaseReference rsvpCountRef = FirebaseDatabase.getInstance()
                .getReference("rsvp_counts")
                .child(eventTitle)
                .child("attending");

        rsvpCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.exists() ? snapshot.getValue(Long.class) : 0;
                rsvpcountTextView.setText("RSVP Count: " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                rsvpcountTextView.setText("RSVP Count: N/A");
                Toast.makeText(ArtistEventDetailActivity.this, "Failed to load RSVP count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAcceptedArtists() {
        if (eventId == null) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.child("invitations").child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                artistListLayout.removeAllViews();

                boolean found = false;
                for (DataSnapshot artistSnap : snapshot.getChildren()) {
                    String status = artistSnap.child("status").getValue(String.class);
                    String artistName = artistSnap.child("artistName").getValue(String.class);

                    if ("accepted".equalsIgnoreCase(status) && artistName != null) {
                        found = true;

                        TextView nameView = new TextView(ArtistEventDetailActivity.this);
                        nameView.setText(artistName);
                        nameView.setTextSize(16);
                        nameView.setPadding(32, 8, 8, 8);
                        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        artistListLayout.addView(nameView);
                    }
                }

                if (!found) {
                    TextView noneView = new TextView(ArtistEventDetailActivity.this);
                    noneView.setText("No accepted artists yet.");
                    noneView.setPadding(16, 8, 16, 8);
                    artistListLayout.addView(noneView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArtistEventDetailActivity.this, "Failed to load artists", Toast.LENGTH_SHORT).show();
            }
        });
    }
}