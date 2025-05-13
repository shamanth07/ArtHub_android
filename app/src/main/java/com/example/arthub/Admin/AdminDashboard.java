package com.example.arthub.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboard extends AppCompatActivity {


    ImageView menuIcon;


    Button btnCreateEvent;





    private RecyclerView recyclerViewEvents;
    private EventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        menuIcon = findViewById(R.id.menuIcon);
       btnCreateEvent = findViewById(R.id.btnCreateEvent);
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventAdapter(this, eventList);
        recyclerViewEvents.setAdapter(adapter);

        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        loadEvents();

        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, CreateEvent.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    eventList.add(event);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboard.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteEvent(String eventId) {
        eventsRef.child(eventId).removeValue()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    public void editEvent(Event event) {


    }
}