package com.example.arthub.Visitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.Admin.Event;
import com.example.arthub.Artist.Artwork;
import com.example.arthub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VisitorDashboard extends AppCompatActivity {

    ImageView menuIcon;

    private RecyclerView recyclerViewArtworks;
    private RecyclerView recyclerViewEvents;

    private VisitorArtworkAdapter artworkAdapter;
    private VisitorEventAdapter eventAdapter;

    private List<Artwork> artworkList;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_dashboard);

        Spinner spinner = findViewById(R.id.categorySpinner);
        menuIcon = findViewById(R.id.menuIcon);

        recyclerViewArtworks = findViewById(R.id.recyclerViewvisitorartworks);
        recyclerViewEvents = findViewById(R.id.recyclerViewvisitortEvents);

        recyclerViewArtworks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));

        artworkList = new ArrayList<>();
        eventList = new ArrayList<>();

        artworkAdapter = new VisitorArtworkAdapter(this, artworkList);
        eventAdapter = new VisitorEventAdapter(this, eventList);

        recyclerViewArtworks.setAdapter(artworkAdapter);
        recyclerViewEvents.setAdapter(eventAdapter);

        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(VisitorDashboard.this, VisitorAccountPage.class);
            startActivity(intent);
        });

        // Spinner logic
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("Artworks")) {
                    recyclerViewArtworks.setVisibility(View.VISIBLE);
                    recyclerViewEvents.setVisibility(View.GONE);
                    loadArtworksFromFirebase();
                } else {
                    recyclerViewArtworks.setVisibility(View.GONE);
                    recyclerViewEvents.setVisibility(View.VISIBLE);
                    loadEventsFromFirebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        loadEventsFromFirebase();
    }

    private void loadEventsFromFirebase() {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                eventList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event = dataSnapshot.getValue(Event.class);
                    if (event != null) {
                        eventList.add(event);
                    }
                }
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("VisitorDashboard", "Failed to load events", error.toException());
            }
        });
    }

    private void loadArtworksFromFirebase() {
        DatabaseReference artworksRef = FirebaseDatabase.getInstance().getReference("artworks");
        artworksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                artworkList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Artwork artwork = dataSnapshot.getValue(Artwork.class);
                    if (artwork != null) {
                        artworkList.add(artwork);
                    }
                }
                artworkAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("VisitorDashboard", "Failed to load artworks", error.toException());
            }
        });
    }
}
