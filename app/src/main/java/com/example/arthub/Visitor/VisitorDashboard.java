package com.example.arthub.Visitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SearchView;
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
    SearchView searchArtworks, searchEvents;
    RecyclerView recyclerViewArtworks, recyclerViewEvents;

    VisitorArtworkAdapter artworkAdapter;
    VisitorEventAdapter eventAdapter;

    List<Artwork> artworkList;
    List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_dashboard);

        Spinner spinner = findViewById(R.id.categorySpinner);
        menuIcon = findViewById(R.id.menuIcon);
        searchArtworks = findViewById(R.id.searchArtworks);
        searchEvents = findViewById(R.id.searchEvents);

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

        // Spinner logic to toggle between Artworks and Events
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();

                if (selected.equals("Artworks")) {
                    setVisibility(recyclerViewArtworks, true);
                    setVisibility(recyclerViewEvents, false);
                    setVisibility(searchArtworks, true);
                    setVisibility(searchEvents, false);

                    loadArtworksFromFirebase();
                    enableArtworkSearch();
                } else {
                    setVisibility(recyclerViewArtworks, false);
                    setVisibility(recyclerViewEvents, true);
                    setVisibility(searchArtworks, false);
                    setVisibility(searchEvents, true);

                    loadEventsFromFirebase();
                    enableEventSearch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        setVisibility(searchArtworks, false);
        setVisibility(searchEvents, true);
        setVisibility(recyclerViewArtworks, false);
        setVisibility(recyclerViewEvents, true);
        loadEventsFromFirebase();
        enableEventSearch();
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
                artworkAdapter.updateList(artworkList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("VisitorDashboard", "Failed to load artworks", error.toException());
            }
        });
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
                eventAdapter.updateList(eventList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("VisitorDashboard", "Failed to load events", error.toException());
            }
        });
    }

    private void enableArtworkSearch() {
        searchArtworks.setQuery("", false);
        searchArtworks.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterArtworks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterArtworks(newText);
                return true;
            }

            private void filterArtworks(String query) {
                List<Artwork> filtered = new ArrayList<>();
                for (Artwork artwork : artworkList) {
                    if (artwork.getTitle().toLowerCase().contains(query.toLowerCase())) {
                        filtered.add(artwork);
                    }
                }
                artworkAdapter.updateList(filtered);
            }
        });
    }

    private void enableEventSearch() {
        searchEvents.setQuery("", false);
        searchEvents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }

            private void filterEvents(String query) {
                List<Event> filtered = new ArrayList<>();
                for (Event event : eventList) {
                    if (event.getTitle().toLowerCase().contains(query.toLowerCase())) {
                        filtered.add(event);
                    }
                }
                eventAdapter.updateList(filtered);
            }
        });
    }


    private void setVisibility(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
