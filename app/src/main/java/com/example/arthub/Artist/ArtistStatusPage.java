package com.example.arthub.Artist;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.Admin.Event;
import com.example.arthub.Admin.Invitation;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ArtistStatusPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArtistStatusAdapter adapter;
    private List<ArtistInvitationItem> itemList = new ArrayList<>();


    private DatabaseReference dbRef;
    private String currentArtistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_status_page);

        recyclerView = findViewById(R.id.statusRecyclerView);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArtistStatusAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        currentArtistId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        loadInvitations();
    }

    private void loadInvitations() {

        dbRef.child("invitations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    String eventId = eventSnapshot.getKey();
                    if (eventSnapshot.hasChild(currentArtistId)) {
                        Invitation invitation = eventSnapshot.child(currentArtistId).getValue(Invitation.class);
                        if (invitation != null) {
                            invitation.setArtistId(currentArtistId);
                            invitation.setEventId(eventId);

                            dbRef.child("events").child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot eventSnap) {
                                    Event event = eventSnap.getValue(Event.class);
                                    if (event != null) {
                                        itemList.add(new ArtistInvitationItem(invitation, event));
                                        adapter.notifyDataSetChanged();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }

                if (itemList.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
