package com.example.arthub.Artist;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.arthub.Admin.SwipeRefreshHelper;
import com.example.arthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    private static final String PREFS_NAME = "ArtistDashboardPrefs";
    private static final String PREFS_KEY_STATUS_PREFIX = "invitation_status_";

    private SharedPreferences sharedPreferences;

    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_artist_dashboard);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        upldartwork = findViewById(R.id.upldartwork);
        menuIcon = findViewById(R.id.menuIcon);
        recyclerViewArtWorks = findViewById(R.id.recyclerViewArtWorks);

        artworkList = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference("artworks");

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        adapter = new ArtworkAdapter(this, artworkList, new ArtworkAdapter.OnArtworkActionListener() {
            @Override
            public void onLikeClick(Artwork artwork) {
                // Like logic here (if any)
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
                if (currentUser == null) return;
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

        // Setup swipe refresh here - reload artworks on swipe
        SwipeRefreshHelper.setupSwipeRefresh(swipeRefresh, this, () -> {
            if (currentUser != null) {
                fetchUserArtworks(currentUser.getUid());
            }
        });

        if (currentUser != null) {
            fetchUserArtworks(currentUser.getUid());
            checkInvitationStatusOnce(currentUser.getUid());
        }

        upldartwork.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistDashboard.this, UploadArtwork.class);
            startActivity(intent);
        });

        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ArtistDashboard.this, ArtistAccountPage.class);
            startActivity(intent);
        });

        checkNotificationPermission();
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

                // Stop swipe refresh animation once done
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArtistDashboard.this, "Failed to load artworks: " + error.getMessage(), Toast.LENGTH_LONG).show();
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }

    private void checkInvitationStatusOnce(String artistId) {
        DatabaseReference invitationsRef = FirebaseDatabase.getInstance()
                .getReference("invitations");

        invitationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    String eventId = eventSnapshot.getKey();
                    if (eventId == null) continue;

                    if (eventSnapshot.hasChild(artistId)) {
                        DataSnapshot artistInvite = eventSnapshot.child(artistId);
                        String status = artistInvite.child("status").getValue(String.class);
                        if (status == null) continue;

                        String lastStatus = sharedPreferences.getString(PREFS_KEY_STATUS_PREFIX + eventId, "");

                        if (!status.equals(lastStatus)) {
                            sharedPreferences.edit()
                                    .putString(PREFS_KEY_STATUS_PREFIX + eventId, status)
                                    .apply();

                            DatabaseReference eventRef = FirebaseDatabase.getInstance()
                                    .getReference("events")
                                    .child(eventId);

                            eventRef.child("title").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot eventTitleSnapshot) {
                                    String eventName = eventTitleSnapshot.getValue(String.class);
                                    showStatusNotification(status, eventName);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    showStatusNotification(status, null);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: handle error
            }
        });
    }

    private void showStatusNotification(String status, String eventName) {
        String channelId = "event_status_channel";
        String title = "Event Application Update";

        String message;
        if ("accepted".equals(status)) {
            message = "Congratulations! You were accepted to the event";
        } else if ("rejected".equals(status)) {
            message = "Sorry, your application was rejected.";
        } else {
            return;
        }

        if (eventName != null) {
            message += ": " + eventName;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Event Status Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
