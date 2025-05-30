package com.example.arthub.Visitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.Admin.Event;
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



    private RecyclerView recyclerViewvisitortEvents;
    private VisitorEventAdapter adapter;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_dashboard);







        menuIcon = findViewById(R.id.menuIcon);
        recyclerViewvisitortEvents = findViewById(R.id.recyclerViewvisitortEvents);
        recyclerViewvisitortEvents.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        adapter = new VisitorEventAdapter(this, eventList);
        recyclerViewvisitortEvents.setAdapter(adapter);


        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(VisitorDashboard.this, VisitorAccountPage.class);
            startActivity(intent);
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("VisitorDashboard", "Failed to load events", error.toException());
            }
        });
    }
}
