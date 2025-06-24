package com.example.arthub.Artist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.arthub.Admin.Event;
import com.example.arthub.Admin.SwipeRefreshHelper;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class ArtistApplyForEvent extends AppCompatActivity {

    ImageView backbtn;
    private Set<String> appliedEventIds = new HashSet<>();
    private RecyclerView recyclerView;
    private ArtistEventAdapter adapter;
    private List<Event> eventList;
    private DatabaseReference eventRef;
    private FirebaseAuth mAuth;

    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_apply_for_event);

        backbtn = findViewById(R.id.backbtn);
        recyclerView = findViewById(R.id.recyclerViewEvents);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventList = new ArrayList<>();
        adapter = new ArtistEventAdapter(ArtistApplyForEvent.this, eventList, this::applyForEvent);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        eventRef = FirebaseDatabase.getInstance().getReference("events");

        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistApplyForEvent.this, ArtistAccountPage.class);
            startActivity(intent);
            finish();
        });

        SwipeRefreshHelper.setupSwipeRefresh(swipeRefresh, this, this::loadEvents);
        loadAppliedEvents(() -> loadEvents());



    }

    private void loadEvents() {
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Event event = snap.getValue(Event.class);
                        String eventId = snap.getKey();
                        if (event != null && eventId != null) {
                            event.setEventId(eventId);
                            if (!appliedEventIds.contains(eventId)) {
                                eventList.add(event);
                            }
                        }
                    }
                } else {
                    Toast.makeText(ArtistApplyForEvent.this, "No events found", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArtistApplyForEvent.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadAppliedEvents(Runnable onComplete) {
        String artistId = mAuth.getCurrentUser().getUid();
        DatabaseReference invitationsRef = FirebaseDatabase.getInstance().getReference("invitations");

        invitationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appliedEventIds.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    if (eventSnapshot.hasChild(artistId)) {
                        appliedEventIds.add(eventSnapshot.getKey());
                    }
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading applied events: " + error.getMessage());
                onComplete.run();
            }
        });
    }


    private void applyForEvent(Event event) {
        String artistId = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();
        String artistName = email != null ? email.split("@")[0] : "Unknown";
        long appliedAt = System.currentTimeMillis();

        DatabaseReference invitationRef = FirebaseDatabase.getInstance()
                .getReference("invitations")
                .child(event.getEventId());

        invitationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int currentApplicants = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String status = child.child("status").getValue(String.class);
                    if (status != null && (status.equals("pending") || status.equals("accepted"))) {
                        currentApplicants++;
                    }
                }

                if (currentApplicants >= event.getMaxArtists()) {
                    Toast.makeText(ArtistApplyForEvent.this, "Application limit reached for this event", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> application = new HashMap<>();
                    application.put("artistName", artistName);
                    application.put("email", email);
                    application.put("appliedAt", appliedAt);
                    application.put("status", "pending");

                    invitationRef.child(artistId).setValue(application).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ArtistApplyForEvent.this, "Applied successfully", Toast.LENGTH_SHORT).show();

                            eventList.remove(event);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ArtistApplyForEvent.this, "Failed to apply", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArtistApplyForEvent.this, "Error checking applications", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
