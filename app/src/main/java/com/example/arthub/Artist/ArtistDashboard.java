package com.example.arthub.Artist;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ArtistDashboard extends AppCompatActivity {

    Button upldartwork;
    ImageView menuIcon;
    RecyclerView recyclerViewArtWorks;
    ArtworkAdapter adapter;
    List<Artwork> artworkList;

    DatabaseReference dbRef;
    FirebaseUser currentUser;
    private SharedPreferences prefs;

    private static final String CHANNEL_ID = "invitation_channel";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_artist_dashboard);

        upldartwork = findViewById(R.id.upldartwork);
        menuIcon = findViewById(R.id.menuIcon);
        recyclerViewArtWorks = findViewById(R.id.recyclerViewArtWorks);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        prefs = getSharedPreferences("InvitationStatusPrefs", MODE_PRIVATE);

        artworkList = new ArrayList<>();
        adapter = new ArtworkAdapter(this, artworkList, new ArtworkAdapter.OnArtworkActionListener() {
            @Override
            public void onLikeClick(Artwork artwork) {
                Toast.makeText(ArtistDashboard.this, "Liked: " + artwork.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(Artwork artwork) {
                Intent intent = new Intent(ArtistDashboard.this, EditArtworkPage.class);
                intent.putExtra("artworkId", artwork.getId());
                startActivity(intent);
                Toast.makeText(ArtistDashboard.this, "Edit: " + artwork.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Artwork artwork) {
                String artistId = currentUser.getUid();

                DatabaseReference artistArtworkRef = FirebaseDatabase.getInstance()
                        .getReference("artists")
                        .child(artistId)
                        .child("artworks")
                        .child(artwork.getId());

                DatabaseReference globalArtworkRef = FirebaseDatabase.getInstance()
                        .getReference("artworks")
                        .child(artwork.getId());

                globalArtworkRef.removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        artistArtworkRef.removeValue().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                artworkList.remove(artwork);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(ArtistDashboard.this, "Deleted: " + artwork.getTitle(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ArtistDashboard.this, "Delete failed in artist node.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ArtistDashboard.this, "Delete failed in global node.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        recyclerViewArtWorks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewArtWorks.setAdapter(adapter);

        if (currentUser != null) {
            fetchUserArtworks(currentUser.getUid());
            checkAndRequestNotificationPermission();
            createNotificationChannel();
            observeInvitationStatusChanges(currentUser.getUid());
        }

        upldartwork.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistDashboard.this, UploadArtwork.class);
            startActivity(intent);
        });

        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistDashboard.this, ArtistAccountPage.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchUserArtworks(String artistId) {
        DatabaseReference artistArtworksRef = FirebaseDatabase.getInstance()
                .getReference("artists")
                .child(artistId)
                .child("artworks");

        artistArtworksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                artworkList.clear();
                for (DataSnapshot artworkSnapshot : snapshot.getChildren()) {
                    Artwork artwork = artworkSnapshot.getValue(Artwork.class);
                    if (artwork != null) {
                        artworkList.add(artwork);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArtistDashboard.this, "Failed to load artworks: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void observeInvitationStatusChanges(String artistId) {
        DatabaseReference invitationsRef = FirebaseDatabase.getInstance().getReference("invitations");
        invitationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot eventSnap : snapshot.getChildren()) {
                    String eventId = eventSnap.getKey();
                    DataSnapshot artistSnap = eventSnap.child(artistId);
                    if (artistSnap.exists()) {
                        String status = artistSnap.child("status").getValue(String.class);
                        String eventName = eventSnap.child(artistId).child("eventName").getValue(String.class); // Assuming you saved eventName inside
                        if (eventName == null) eventName = "an event";

                        if (status != null && (status.equalsIgnoreCase("accepted") || status.equalsIgnoreCase("rejected"))) {
                            String key = "notif_" + eventName + "_" + status.toLowerCase();
                            if (!prefs.getBoolean(key, false)) {
                                showStatusNotification(status, eventName);
                                prefs.edit().putBoolean(key, true).apply();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showStatusNotification(String status, String eventName) {
        String title = status.equalsIgnoreCase("accepted") ? "Invitation Accepted" : "Invitation Rejected";
        String body = status.equalsIgnoreCase("accepted")
                ? "Your invitation for event \"" + eventName + "\" was accepted!"
                : "Your invitation for event \"" + eventName + "\" was rejected.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            return;
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Invitation Updates";
            String description = "Notification for invitation status changes";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
