package com.example.arthub.Admin;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.arthub.Visitor.InvitationAdapter;
import com.example.arthub.Visitor.VisitorAccountPage;
import com.example.arthub.Visitor.VisitorDashboard;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManageInvitations extends AppCompatActivity {


    ImageView menuIcon;

    private RecyclerView recyclerViewinvitations;
    private InvitationAdapter adapter;
    private List<String> keys = new ArrayList<>();
    private List<Invitation> invitations = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_invitations);

          menuIcon = findViewById(R.id.menuIcon);


        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(AdminManageInvitations.this, AdminAccountPage.class);
            startActivity(intent);
        });

        recyclerViewinvitations = findViewById(R.id.recyclerViewinvitations);
        recyclerViewinvitations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewinvitations.setHasFixedSize(true);

        dbRef = FirebaseDatabase.getInstance().getReference("invitations");
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");

         adapter = new InvitationAdapter(keys, invitations, dbRef, eventsRef);
        recyclerViewinvitations.setAdapter(adapter);


        loadInvitations();
    }




    private void loadInvitations() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                keys.clear();
                invitations.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.getKey();
                    Invitation invite = ds.getValue(Invitation.class);

                    if (invite != null) {
                        keys.add(key);
                        invitations.add(invite);
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
