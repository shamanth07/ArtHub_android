package com.example.arthub.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.R;
import com.example.arthub.Visitor.InvitationAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManageInvitations extends AppCompatActivity {

    private ImageView menuIcon;
    private RecyclerView recyclerViewInvitations;
    private InvitationAdapter adapter;

    private List<String> compositeKeys = new ArrayList<>();
    private List<Invitation> invitations = new ArrayList<>();
    private DatabaseReference dbRef;
    private DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_invitations);

        menuIcon = findViewById(R.id.menuIcon);
        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(AdminManageInvitations.this, AdminAccountPage.class);
            startActivity(intent);
        });

        recyclerViewInvitations = findViewById(R.id.recyclerViewinvitations);
        recyclerViewInvitations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInvitations.setHasFixedSize(true);

        dbRef = FirebaseDatabase.getInstance().getReference("invitations");
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        adapter = new InvitationAdapter(compositeKeys, invitations, dbRef, eventsRef);
        recyclerViewInvitations.setAdapter(adapter);

        loadInvitations();
    }

    private void loadInvitations() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                invitations.clear();
                compositeKeys.clear();

                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    String eventId = eventSnapshot.getKey();
                    if (eventId == null) continue;

                    for (DataSnapshot artistSnapshot : eventSnapshot.getChildren()) {
                        String artistId = artistSnapshot.getKey();
                        Invitation invite = artistSnapshot.getValue(Invitation.class);

                        if (invite != null && artistId != null) {
                            invite.setEventId(eventId);
                            invite.setArtistId(artistId);

                            invitations.add(invite);
                            compositeKeys.add(eventId + "/" + artistId);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminManageInvitations.this, "Failed to load invitations.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}