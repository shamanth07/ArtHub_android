package com.example.arthub.Artist;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.List;

public class ArtistApplyForEvent extends AppCompatActivity {


    ImageView backbtn;


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
        adapter = new ArtistEventAdapter(this, eventList, this::applyForEvent);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        eventRef = FirebaseDatabase.getInstance().getReference("events");

        backbtn.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistApplyForEvent.this, ArtistAccountPage.class);
            startActivity(intent);
            finish();
        });

        loadEvents();
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



    private void applyForEvent(Event event) {
        String artistId = mAuth.getCurrentUser().getUid();
        DatabaseReference applyRef = FirebaseDatabase.getInstance()
                .getReference("ArtistEvent")
                .child(event.getEventId())
                .child(artistId);

        applyRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Applied to " + event.getTitle(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to apply", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
