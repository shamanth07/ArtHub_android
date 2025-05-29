package com.example.arthub.Artist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.arthub.Admin.Event;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArtistApplyForEvent extends AppCompatActivity {


    ImageView backbtn;

    Button Eventapplied;




    private  String artistName,email;

    private Set<String> appliedEventIds = new HashSet<>();


    private RecyclerView recyclerView;
    private ArtistEventAdapter adapter;
    private List<Event> eventList;
    private DatabaseReference eventRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_apply_for_event);


        backbtn = findViewById(R.id.backbtn);








        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventList = new ArrayList<>();
        adapter = new ArtistEventAdapter(ArtistApplyForEvent.this, eventList, event -> {
            applyForEvent(event);
        });
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        eventRef = FirebaseDatabase.getInstance().getReference("events");

        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistApplyForEvent.this, ArtistAccountPage.class);
            startActivity(intent);
            finish();
        });

        loadEvents();
        loadAppliedEvents(appliedEventIds,adapter);



    }

    private void loadEvents() {
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Event event = snap.getValue(Event.class);
                    if (event != null) {
                        eventList.add(event);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArtistApplyForEvent.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAppliedEvents(Set<String> appliedEventIds, ArtistEventAdapter adapter) {
        String artistId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference appliedRef = FirebaseDatabase.getInstance()
                .getReference("invitations");

        appliedRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                if (eventSnapshot.hasChild(artistId)) {
                    appliedEventIds.add(eventSnapshot.getKey());
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("loadAppliedEvents", "Failed to fetch applied events", e);
        });
    }




    private void applyForEvent(Event event) {
        String artistId = mAuth.getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String artistName = email.split("@")[0];
        long appliedAt = System.currentTimeMillis();

        DatabaseReference invitationRef = FirebaseDatabase.getInstance()
                .getReference("invitations")
                .child(event.getEventId());

        // Count how many artists have already applied
        invitationRef.get().addOnSuccessListener(snapshot -> {
            int currentApplicants = 0;

            for (DataSnapshot child : snapshot.getChildren()) {
                String status = child.child("status").getValue(String.class);
                if (status != null && (status.equals("pending") || status.equals("accepted"))) {
                    currentApplicants++;
                }
            }

            if (currentApplicants >= event.getMaxArtists()) {
                Toast.makeText(this, "Application limit reached for this event", Toast.LENGTH_SHORT).show();
            } else {
                // Allow this artist to apply
                Map<String, Object> invitationData = new HashMap<>();
                invitationData.put("artistName", artistName);
                invitationData.put("email", email);
                invitationData.put("appliedAt", appliedAt);
                invitationData.put("status", "pending");

                invitationRef.child(artistId).setValue(invitationData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Applied to " + event.getTitle(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to apply", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to check existing applicants", Toast.LENGTH_SHORT).show();
        });
    }

}
